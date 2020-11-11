package com.v3.basis.blas.blasclass.service

import android.content.Context
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.LoginActivity
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.extra.trivia.TriviaList
import com.v3.basis.blas.blasclass.rest.SyncBlasRestFixture
import com.v3.basis.blas.blasclass.service.SenderHandler.Companion.FIXTURE
import com.v3.basis.blas.blasclass.service.SenderHandler.Companion.IMAGE
import com.v3.basis.blas.blasclass.service.SenderHandler.Companion.ITEM
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MsgParams(
    val token:String,
    val projectId:String,
    val sendType:Int
)

class SenderHandler(val context: Context): Handler() {
    companion object {
        var lock:Object = Object()
        val FIXTURE = 0x00000001
        val ITEM    = 0x00000010
        val IMAGE   = 0x00000100
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        val msgParam = msg.obj as MsgParams
        val token = msgParam.token
        var projectId = msgParam.projectId
        val sendType = msgParam.sendType
        Log.d("konishi", "handleMessage start")
        //メッセージ受信スレッド
        Thread(
            Runnable {
                synchronized(lock) {
                    //ここに送信処理を入れる
                    if((sendType and FIXTURE) == FIXTURE) {
                        Log.d("konishi", "syncFixture start")
                        syncFixture(context, token, projectId)
                    }

                    if((sendType and ITEM) == ITEM) {
                        //データを送信する

                    }

                    if((sendType and IMAGE) == IMAGE) {
                        //画像を送信する

                    }
                }
            }).start()
    }

    /**
     * 画像の未送信データをBLASに送信する
     */
    private fun syncImage(context:Context, token:String, projectId:String):Int{
        var ret = 0
        val controller = ImagesController(context, projectId)

        val imageList = controller.search(true)

        imageList.forEach {
            var payload = it.toPayLoad().also{
                it["token"] = token
            }
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

        fixtures.forEach {
            val fixtureId = it.fixture_id
            Log.d("konishi",it.serial_number)
            //ここに自力で送信する処理を作るしかない

            var payload = it.toPayLoad().also{
                it["token"] = token
            }

            var json: JSONObject? = null
            val crud = it.status.toString()
            //BLASに送信する
            json = SyncBlasRestFixture(crud).execute(payload)
            if(json != null) {
                val errorCode = json.getInt("error_code")
                if(errorCode == 0) {
                    if(fixtureId < 0) {
                        val records = json.getJSONObject("records")
                        val newFixtureId = records.getString("fixture_id")
                        val oldFixtureId = records.getString("temp_fixture_id")
                        controller.updateFixtureId(oldFixtureId, newFixtureId)
                    }
                    else {
                        controller.resetSyncStatus(fixtureId.toString())
                    }
                }
                else {
                    controller.setErrorMsg(fixtureId.toString(), errorCode)
                }
            }
            else {
                controller.setErrorMsg(fixtureId.toString(), -1)
            }
        }

        return ret
    }
}

class BlasSyncService() : Service() {
    private var messenger: Messenger? = null
    private lateinit var token:String

    override fun onBind(intent: Intent?): IBinder? {
        return messenger?.binder
    }

    override fun onCreate() {
        super.onCreate()
        messenger = Messenger(SenderHandler(applicationContext))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            token = intent.getStringExtra("token")
        }
        val openIntent = Intent(this, LoginActivity::class.java).let {
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

        //タイマースレッド
        Thread(
            Runnable {
                while(true){
                    Log.d("konishi", "Timer start")
                    Thread.sleep(10*60*1000)

                    val dbPath = applicationContext.dataDir.path + "/databases/"
                    //ディレクトリ名からプロジェクトIDを取得する
                    val fileList = File(dbPath).listFiles()
                    fileList.forEach {
                        if (it.isDirectory()) {
                            val projectId = it.name
                            if (token != null) {
                                messenger?.send(Message.obtain(null, 0, MsgParams(token, projectId, FIXTURE or ITEM or IMAGE))
                                )
                            }
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
}