package com.v3.basis.blas.blasclass.controller
import android.net.Uri
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.PARAM_FILE_DIR
import com.v3.basis.blas.blasclass.app.is2String
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.database
import com.v3.basis.blas.blasclass.rest.BlasRest
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.lang.Exception
import java.net.HttpURLConnection
import kotlin.concurrent.thread
import kotlin.concurrent.withLock


/**
 * データ形式を決めること
 */
data class RestRequestData(
    val queue_id:Int,                         /** 通信通番 */
    val uri:String,                            /** 要求コード */
    val method:String,                         /** リクエストのパラメーター */
    val param_file:String,                    /** リクエストのパラメーター */
    var retry_count:Int,                      /** リトライした回数 */
    var error_code:Int,                       /** エラーコード */
    var status:Int                             /** 通信ステータス */
)


/**
 * シングルトン。BLASへの通信を制御する。
 */
object QueueController {
    private var stop_flg:Boolean = false                                  //スレッド停止フラグ
    private var reqList:MutableList<RestRequestData> = mutableListOf()     // キューリスト
    val lock = java.util.concurrent.locks.ReentrantLock()                   //排他制御

    /**
     * スレッドを開始する
     */
    public fun start() {
        try {
            /**
             * 停止前の未送信データを復元する
             */
            reqList = loadQueueFromDB()
        }
        catch(e: Exception) {
            Log.e("DbLoadError", e.toString())
        }

        thread{
            stop_flg = false
            mainLoop()
        }
    }

    /**
     * スレッドを停止する
     */
    public fun stop() {
        stop_flg = true
    }

    /**
     * DBからキューリストを復元する
     */
    private fun loadQueueFromDB():MutableList<RestRequestData>{
        /**
         * DBからキューリストを復元する。
         * ダンプファイルでやるなよ、データが吹っ飛んだらシャレになりません
         */

        var dataList:MutableList<RestRequestData> = mutableListOf()

        val sql = "select * from RequestTable"

        try {
            val c = database.rawQuery(sql, null)

            if (c.count > 0) {
                c.moveToFirst();
                while (!c.isAfterLast) {

                    Log.d("【DBセレクト】","開始")
                    dataList.add(RestRequestData(
                        c.getInt(0),c.getString(1),c.getString(2),c.getString(3),
                                 c.getInt(4),c.getInt(5),c.getInt(6)
                    ))

                    c.moveToNext()
                }
             }
            c.close()
        }catch(e: Exception) {
            Log.e("DbSelectError", e.toString())
        }

        return dataList
    }


    /**
     * 送信を要求する
     */
    public fun submit(payLoad:MutableMap<String, String>, viewCallBack:(String)->Unit) {
        lock.withLock {
            /**
             * DBにレコードを追加する。
             * DBはアプリケーションの不意のシャットダウンや再起動で復活させるため。
             */
        }
    }

    /**
     * 未送信データのキューを取得する
     */
    private fun getQueueList():Int {
        var queueId = 1
        return queueId
    }


    /**
     * 指定したIDのノードを削除する。削除するのは送信できた
     * リクエストののノード。
     */
    private fun delQueueNode(queueId:Int) {
        lock.withLock {
            /**
             * キューからノードを削除する
             */
        }
    }

    private fun doConnection(reqArray:RestRequestData) : String {
        val param : String = ""

        //ファイルからパラメータ取得
        try{
            val reader: BufferedReader = File(PARAM_FILE_DIR + reqArray.param_file).bufferedReader()
            val param = reader.use { it.readText() }
        }catch (e: FileNotFoundException){
            Log.e("FileReadError", e.toString())
        }

        var url = java.net.URL(reqArray.uri + param )
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = reqArray.method
        con.connectTimeout = BlasRest.CONTEXT_TIME_OUT
        con.readTimeout = BlasRest.READ_TIME_OUT

        if(reqArray.method == "GET") {
            con.doOutput = false  //GETのときはtrueにしてはいけません
        }else{
            con.doOutput = true
        }
        // TODO 関数移すかも
        /*　
        val responseData = con.inputStream
        val response = is2String(responseData)
        con.disconnect()

        return response
        */
    }

    /**
     * 送信スレッド
     */
    @Synchronized public fun mainLoop() {
        /* 通信のバックグラウンド処理 */
        while(!stop_flg) {
            try {
                /* キューからデータを取り出す */
                for (i in reqList.indices) {
                    doConnection(reqList[i])
                }

                /* 送信 */

                /* 正常 */
//                キュー消す

                /* 異常 */

            }
            catch(e:Exception) {

            }
            /* キューデータを通信エラーになるまでループする */

            Thread.sleep(10* 1000)
        }

    }


}