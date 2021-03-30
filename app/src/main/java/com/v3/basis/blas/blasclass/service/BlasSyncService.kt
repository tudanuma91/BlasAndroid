package com.v3.basis.blas.blasclass.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.*
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.LoginActivity
import com.v3.basis.blas.blasclass.app.getHash
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.controller.ImagesController.Companion.SMALL_IMAGE
import com.v3.basis.blas.blasclass.controller.ImagesController.Companion.BIG_IMAGE
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Items
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.field.FieldController
import com.v3.basis.blas.blasclass.extra.trivia.TriviaList
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.blasclass.rest.SyncBlasRestEvent
import com.v3.basis.blas.blasclass.rest.SyncBlasRestFixture
import com.v3.basis.blas.blasclass.rest.SyncBlasRestImage
import com.v3.basis.blas.blasclass.rest.SyncBlasRestItem
import com.v3.basis.blas.blasclass.service.SenderHandler.Companion.EVENT
import com.v3.basis.blas.blasclass.service.SenderHandler.Companion.FIXTURE
import com.v3.basis.blas.blasclass.service.SenderHandler.Companion.IMAGE
import com.v3.basis.blas.blasclass.service.SenderHandler.Companion.ITEM
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class MsgParams(
    val token:String,
    val projectId:String,
    val sendType:Int,
    val itemId:String = "" //イベント発行監視用のitem_idのリスト
)
interface EventListener {
    val itemId:Long
    fun callBack(itemRecord:MutableMap<String, String>)
}

class SenderHandler(val context: Context): Handler() {
    companion object {
        var lock = ReentrantLock()
        var level: Int? = null
        val FIXTURE = 0x00000001
        val ITEM = 0x00000010
        val IMAGE = 0x00000100
        val EVENT = 0x00001000

        //イベントコールバック用関数
        val eventCallbackList = mutableListOf<EventListener>()
    }

    //電波の状況を調べる
    private var telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val phoneState = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            level = signalStrength?.level
            //BlasLog.trace("I", "電波強度:$level")
        }
    }

    init {
        telephonyManager.listen(phoneState, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        val msgParam = msg.obj as MsgParams
        val token = msgParam.token
        var projectId = msgParam.projectId
        val sendType = msgParam.sendType

        //wifiの接続状況を調べる
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        //メッセージ受信スレッド

        //ロック中だったら次回の再送間隔待ち
        BlasLog.trace("I", "イベントを受信しました(projectId:${projectId}, event:${sendType}")
        Thread(
            Runnable {
                try {
                    if (level != null) {
                        if ((activeNetwork?.type != ConnectivityManager.TYPE_WIFI) and (level!! < 3)) {
                            BlasLog.trace("W", "電波強度が弱いため、再送しません")
                            return@Runnable
                        }

                        //機器管理のデータを送信する
                        if ((sendType and FIXTURE) == FIXTURE) {
                            lock.withLock {
                                BlasLog.trace("I", "ロックを獲得しました")
                                syncFixture(context, token, projectId)
                                BlasLog.trace("I", "ロックを解除します")
                            }
                        }

                        //データ管理のデータを送信する
                        if ((sendType and ITEM) == ITEM) {
                            //データを送信する
                            lock.withLock {
                                BlasLog.trace("I", "ロックを獲得しました")
                                syncItems(context, token, projectId)
                                BlasLog.trace("I", "ロックを解除しました")
                            }
                        }

                        //画像を送信する
                        if ((sendType and IMAGE) == IMAGE) {
                            //画像を送信する
                            lock.withLock {
                                BlasLog.trace("I", "ロックを獲得しました")
                                syncImage(context, token, projectId)
                                BlasLog.trace("I", "ロックを解除します")
                            }
                        }

                        //イベントを受信する
                        if ((sendType and EVENT) == EVENT) {
                            lock.withLock {
                               // syncEvent(context, token, projectId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    BlasLog.trace("E", "例外が発生しました")
                    BlasLog.trace("E", "${e.message}")
                    e.printStackTrace()
                }
            }).start()
    }

    /**
     * データ管理のデータをBLASに送信する
     * [戻り値]
     * リトライアウトなし:0(BaseController.RETRY_NORMAL)
     * リトライアウトした:-1(BaseController.RETRY_OUT)
     */
    private fun syncItems(context: Context, token: String, projectId: String): Int {
        var ret = BaseController.RETRY_NORMAL
        val controller = ItemsController(context, projectId)
        //未送信データ改修
        val records = controller.search(syncFlg = true)

        BlasLog.trace("I", "itemList size:${records.size}")

        for (i in 0 until records.size) {
            val record = records[i]

            val item_id = record["item_id"]
            var sendCnt = record["send_cnt"]?.toInt()

            //リトライアウトチェック
            if (sendCnt != null) {
                if (sendCnt == BaseController.RETRY_MAX) {
                    //1回だけログに出す
                    BlasLog.trace("E", "リトライアウトしました item_id:${item_id}")
                    ret = BaseController.RETRY_OUT
                    //戻り値が-1のときはコール元でメッセージの通知を行うこと。
                    continue
                } else {
                    sendCnt += 1
                }
            }

            //送信用ペイロード作成
            var payload = mutableMapOf<String, String>()

            record.forEach {
                payload[it.key] = it.value.toString()
            }
            payload["token"] = token

            BlasLog.trace("I", "データを送信します")

            val json = SyncBlasRestItem().upload(payload)

            if (json != null) {
                val errorCode = json.getInt("error_code")
                var errorMsg = json.getString("message")
                if (errorCode == 0) {
                    //送信できた場合
                    val records = json.getJSONObject("records")

                    val new_item_id = records.getString("new_item_id")
                    val org_item_id = records.getString("temp_item_id")

                    controller.updateItemId(org_item_id, new_item_id)
                } else {
                    //BLAS側でエラーが返された場合(論理エラー)
                    if (errorMsg == null) {
                        errorMsg = ""
                    }

                    //論理エラー
                    controller.updateItemRecordStatus(
                        item_id,
                        BaseController.NETWORK_LOGICAL_ERROR,
                        sendCnt,
                        errorMsg
                    )

                    BlasLog.trace("E", "データに誤りがあります　error_code:$errorCode, msg:$errorMsg")
                }
            } else {
                //通信そのものができなかった場合(物理エラー)
                BlasLog.trace("E", "データの送信に失敗しました")
                controller.updateItemRecordStatus(
                    item_id,
                    BaseController.NETWORK_ERROR,
                    sendCnt,
                    "データの送信に失敗しました"
                )
                break
            }
        }

        return ret
    }

    /**
     * 画像データをBLASに送信する
     */
    private fun syncImage(context: Context, token: String, projectId: String): Int {
        var ret = 0
        var imageId = 0L
        var sendCnt = 0
        val controller = ImagesController(context, projectId)

        BlasLog.trace("I", "画像を送信します")

        val imageList = controller.search(true)

        BlasLog.trace("I", "imageList size:${imageList.size}")

        for (i in 0 until imageList.size) {
            var image_record = imageList[i]
            image_record.image_id?.let {
                imageId = it
            }
            image_record.send_cnt?.let {
                //リトライ送信回数チェック
                sendCnt = it
            }

            //リトライアウトチェック
            if (sendCnt != null) {
                if (sendCnt == BaseController.RETRY_MAX) {
                    //1回だけログに出す
                    BlasLog.trace("E", "リトライアウトしました image_id:${imageId}")
                    ret = BaseController.RETRY_OUT
                    //戻り値が-1のときはコール元でメッセージの通知を行うこと。
                    continue
                } else {
                    sendCnt += 1
                }
            }

            BlasLog.trace("I", image_record.toString())

            //送信パラメーターの作成
            var payload = image_record.toPayLoad().also {
                it["token"] = token

                val base64Img = controller.getBase64File(
                    image_record.item_id.toString(),
                    image_record.project_image_id.toString(),
                    BIG_IMAGE
                )

                val extNum = controller.getExtNumber(
                    image_record.item_id.toString(),
                    image_record.project_image_id.toString()
                )
                it["image_type"] = extNum.toString()
                it["image"] = base64Img
                it["hash"] = getHash(base64Img)
            }

            var json: JSONObject? = null

            //BLASに送信する
            BlasLog.trace("I", "画像を送信します")
            json = SyncBlasRestImage().upload(payload)
            if (json != null) {
                val errorCode = json.getInt("error_code")
                val msg = json.getString("message")
                if (errorCode == 0) {
                    BlasLog.trace("I", "画像の送信に成功しました")
                    if (imageId < 0) {
                        val records = json.getJSONObject("records")
                        val newImageId = records.getString("new_image_id")
                        val oldImageId = records.getString("temp_image_id")

                        BlasLog.trace("I", "IDを同期します ${oldImageId} ${newImageId}")
                        controller.updateImageId(oldImageId, newImageId)
                    } else {
                        BlasLog.trace("I", "IDを同期します ${imageId}")
                        controller.resetSyncStatus(imageId.toString())
                    }
                } else {
                    //論理エラーが発生
                    BlasLog.trace("E", "BLASからエラーが返されました　error_code:$errorCode, msg:$msg")
                    controller.updateImageRecordStatus(
                        imageId.toString(),
                        BaseController.NETWORK_LOGICAL_ERROR,
                        sendCnt,
                        "データの送信に失敗しました"
                    )
                }
            } else {
                //通信エラーが発生しているので、このターンでのリトライは打ち切り
                BlasLog.trace("E", "送信に失敗しました。1分後にリトライを行います")
                controller.updateImageRecordStatus(
                    imageId.toString(),
                    BaseController.NETWORK_ERROR,
                    sendCnt,
                    "データの送信に失敗しました"
                )
                break
            }
        }

        return ret
    }

    /**
     * 機器管理の未送信データをBLASに送信する
     */
    private fun syncFixture(context: Context, token: String, projectId: String): Int {
        var ret = 0
        var sendCnt = 0
        val controller = FixtureController(
            context,
            projectId
        )
        val fixtures = controller.search(null, true)

        for (i in 0 until fixtures.size) {
            val fixtureRecord = fixtures[i]
            val fixtureId = fixtureRecord.fixture_id
            fixtureRecord.send_cnt?.let {
                //リトライ送信回数チェック
                sendCnt = it
            }

            //リトライアウトチェック
            if (sendCnt != null) {
                if (sendCnt == BaseController.RETRY_MAX) {
                    //1回だけログに出す
                    BlasLog.trace("E", "リトライアウトしました fixtureId:${fixtureId}")
                    ret = BaseController.RETRY_OUT
                    //戻り値が-1のときはコール元でメッセージの通知を行うこと。
                    continue
                } else {
                    sendCnt += 1
                }
            }

            BlasLog.trace("I", "シリアルナンバーを送信します ${fixtureRecord.serial_number}")
            //ここに自力で送信する処理を作るしかない

            var payload = fixtureRecord.toPayLoad().also {
                it["token"] = token
            }

            var json: JSONObject? = null
            val crud = fixtureRecord.status.toString()
            //BLASに送信する
            BlasLog.trace("I", "シリアルナンバーを送信します ${payload}")

            json = SyncBlasRestFixture(crud).upload(payload)
            if (json != null) {
                val errorCode = json.getInt("error_code")
                if (errorCode == 0) {
                    if (fixtureId < 0) {
                        val records = json.getJSONObject("records")
                        val newFixtureId = records.getString("fixture_id")
                        val oldFixtureId = records.getString("temp_fixture_id")
                        controller.updateFixtureId(oldFixtureId, newFixtureId)
                        BlasLog.trace("I", "シリアルナンバーを新規追加しました ${oldFixtureId} ${newFixtureId}")
                    } else {
                        controller.resetSyncStatus(fixtureId.toString())
                        BlasLog.trace("I", "シリアルナンバーを更新しました ${fixtureId}")
                    }
                } else {
                    val msg = json.getString("message")
                    BlasLog.trace("E", "BLASからエラーが返されました errorCode:${errorCode}")
                    controller.updateFixtureRecordStatus(
                        fixtureId.toString(),
                        BaseController.NETWORK_LOGICAL_ERROR,
                        sendCnt,
                        msg
                    )
                }
            } else {
                //通信エラーが発生しているので、このターンでのリトライは打ち切り
                BlasLog.trace("E", "送信に失敗しました。1分後にリトライを行います")
                controller.updateFixtureRecordStatus(
                    fixtureId.toString(),
                    BaseController.NETWORK_ERROR,
                    sendCnt,
                    "データの送信に失敗しました"
                )
                break
            }
        }

        return ret
    }

    /**
     * イベントを定期的に取得する
     */
    private fun syncEvent(context: Context, token: String, projectId: String): Int {

        val itemController = ItemsController(context, projectId)
        val fields = FieldController(context, projectId).getFieldRecords()
        val conditions = mutableMapOf<String, String>()

        //イベント型で処理中のレコードを探す
        fields.forEach {
            if(it.type.toString() == FieldType.EVENT_FIELD) {
                val fldCol = "fld${it.col}"
                conditions[fldCol] = "処理中"
            }
        }

        eventCallbackList.forEach {
            val itemId = it.itemId

        }

        if(conditions.isEmpty()) {
            return -1
        }

        val eventRecords = itemController.find(conditions)

        eventRecords.forEach {
            //イベントを取得して更新する
            //データ管理の更新用データ作成
            val payload = mutableMapOf<String, String>()
            val valueMap = mutableMapOf<String, String>()
            payload["token"] = token
            payload["project_id"] = projectId
            valueMap["project_id"] = projectId
            if(it.contains("item_id")) {
                val itemId = it["item_id"]
                if(itemId != null) {
                    payload["item_id"] = itemId
                    valueMap["item_id"] = itemId
                }
            }
            //BLASのデータをダウンロードする
            val jsonObj = SyncBlasRestEvent("search").request(payload)
            val errorCode = jsonObj.getInt("error_code")
            if(errorCode == 0) {
                //BLASのイベント状態を取得できた
                val item = jsonObj.getJSONArray("records")
                    .getJSONObject(0)
                    .getJSONObject("Item")

                //処理の都合上、mapに変換する
                item.keys().forEach {key->
                    if(conditions.containsKey(key)) {
                        valueMap[key] = item.getString(key)
                    }
                }
                //LDBをアップデートする。BLASからのデータを反映するだけなので
                //ステータスは同期済みにする
                itemController.updateToLDB(valueMap, status=BaseController.SYNC_STATUS_SYNC)

                /* ここで例外が発生している
                val node = eventCallbackList.first{listener->
                    listener.itemId.toString() == it["item_id"]
                }
                //コールバックを呼び出す
                //val guiHandler = Handler()
               // guiHandler.post{
                    node.callBack(valueMap)
               // }*/

            }
            else {
                //BLASに通信できたけど、レコードの更新ができなかったとき
                val message = jsonObj.getString("message")
            }
        }

        return 0
    }
}

class BlasSyncService() : Service() {
    private var messenger: Messenger? = null
    private lateinit var token:String
    private lateinit var telephonyManager: TelephonyManager
    private var level:Int? = null
    private var openIntent:PendingIntent? = null
    private var notificationManager:NotificationManager? = null

    override fun onBind(intent: Intent?): IBinder? {
        return messenger?.binder
    }

    override fun onCreate() {
        super.onCreate()
        messenger = Messenger(SenderHandler(applicationContext))
    }

    fun notifyMsg(title:String, msg:String){
        var notification = NotificationCompat.Builder(this, 1234.toString())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("BlasJ " + title)
            .setContentText(msg)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager?.notify(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            token = intent.getStringExtra("token")
        }


        openIntent = Intent(this, LoginActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        val trivia = greetingMsg()
        var title = ""
        var msg = ""
        trivia.forEach { t, u ->
            title = t
            msg = u  }

        var notification = NotificationCompat.Builder(this, 1234.toString())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("BlasJ " + title)
            .setContentText(msg)
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //タイマースレッド
        Thread(
            Runnable {
                while(true){
                    Thread.sleep(60*1000)

                    BlasLog.trace("I", "再送開始")
                    val dbPath = applicationContext.dataDir.path + "/databases/"
                    //ディレクトリ名からプロジェクトIDを取得する
                    val fileList = File(dbPath).listFiles()
                    fileList.forEach {
                        if (it.isDirectory()) {
                            val projectId = it.name
                            if (token != null) {
                                messenger?.send(Message.obtain(null, 0, MsgParams(token, projectId, FIXTURE or ITEM or IMAGE or EVENT)))
                            }

                            //DBからエラーになっているレコードを探す
                            errorCheck(applicationContext, token, projectId)
                        }
                    }



                }
                stopSelf()
            }).start()


        startForeground(1, notification)
        return START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }

    fun greetingMsg(): MutableMap<String, String> {
        val trivia = TriviaList()
        val num = trivia.size()
        val r = (0..num-1).random()

        val map = trivia.getTrivia(r)

        return map
    }

    /**
     * データ管理のデータをBLASに送信する
     */
    private fun errorCheck(context:Context, token:String, projectId:String):Int {
        var ret = 0
        val itemController = ItemsController(context, projectId)
        val imageController = ImagesController(context, projectId)
        val fixtureController = FixtureController(context, projectId)

        //未送信データ改修
        val itemRecords = itemController.search(syncFlg = true)
        val fixtureRecords = fixtureController.search(syncFlg = true)
        val imageRecords = imageController.search(syncFlg = true)

        //論理エラーだけ通知したい
        //通信エラーは自動回復するので、いちいち通知しない
        //一回通知したら、もう表示したくないのだが…。別タイマーで10分間隔で表示するというのも一つの手かな…。
        if(itemRecords.size > 0) {
            notifyMsg("BLASJ ", "データ管理に未送信データがあります")
        }

        if(fixtureRecords.size > 0) {
            notifyMsg("BLASJ ", "機器管理に未送信データがあります")
        }

        if(imageRecords.size > 0) {
            notifyMsg("BLASJ ", "画像データに未送信データがあります")
        }


        return ret
    }

    /**
     * 画像データをBLASに送信する
     */
    private fun syncImage(context:Context, token:String, projectId:String):Int{
        var ret = 0
        var imageId = 0L
        val controller = ImagesController(context, projectId)

        BlasLog.trace("I","画像を送信します")

        val imageList = controller.search(true)

        BlasLog.trace("I","imageList size:${imageList.size}")

        for(i in 0 until imageList.size){

        }

        return ret
    }

    /**
     * 機器管理の未送信データをBLASに送信する
     */
    private fun syncFixture(context:Context, token:String, projectId:String):Int{
        var ret = 0
        val controller = FixtureController(
            context,
            projectId
        )
        val fixtures = controller.search(null, true)

        for(i in 0 until fixtures.size) {

        }

        return ret
    }
}