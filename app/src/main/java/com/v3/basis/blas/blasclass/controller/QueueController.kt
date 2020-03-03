package com.v3.basis.blas.blasclass.controller
import android.net.Uri
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.PARAM_FILE_DIR
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.READ_TIME_OUT_POST
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.REQUEST_TABLE
import com.v3.basis.blas.blasclass.app.comIs2String
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.database
import com.v3.basis.blas.blasclass.rest.BlasRest
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.net.HttpURLConnection
import kotlin.concurrent.thread


/**
 * データ形式を決めること
 */
data class RestRequestData(
    val request_id:Int,                         /** 通信通番 */
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
     * 送信スレッド
     */
    @Synchronized public fun mainLoop() {

        var resCorde : Int
        var response : String

        /* 通信のバックグラウンド処理 */
        while(!stop_flg) {
            try {

                reqList = loadQueueFromDB()

                for (i in reqList.indices) {
                    var result  = doConnect(reqList[i])

                    // 正常の場合
                    if(result.first < 300){
                        doSuccess(reqList[i],result.second)
                    }

                }

            }
            catch(e:Exception) {
                Log.e("mainLoopError", e.toString())
            }
            /* キューデータを通信エラーになるまでループする */

            Thread.sleep(10* 1000)
        }

    }

    /**
     * DBからキューリストを作成する
     */
    private fun loadQueueFromDB():MutableList<RestRequestData>{
        /**
         * DBからキューリストを復元する。
         * ダンプファイルでやるなよ、データが吹っ飛んだらシャレになりません
         */

        var dataList:MutableList<RestRequestData> = mutableListOf()

        val sql = "select * from RequestTable"

        try {
            val cursor = database.rawQuery(sql, null)

            if (cursor.count > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast) {

                    Log.d("【DBセレクト】","開始")
                    dataList.add(RestRequestData(
                        cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),
                        cursor.getInt(4),cursor.getInt(5),cursor.getInt(6)
                    ))

                    cursor.moveToNext()
                }
            }
            cursor.close()
        }catch(e: Exception) {
            Log.e("DbSelectError", e.toString())
        }

        return dataList
    }

    /**
     * DBから取得したコマンドをブラスに送信する
     */

    private fun doConnect(reqArray:RestRequestData) :  Pair <Int,String> {
        var param:String = ""
        val fileDir = BlasSQLDataBase.context.getFilesDir().getPath()
        val filePath: String = fileDir + "/" + reqArray.param_file
        val response:String
        var resCorde:Int = 0

        //ファイルからパラメータ取得
        try{
            // val reader: BufferedReader = File(filePath).bufferedReader()
            // val param = reader.use { it.readText() }
             param = File(filePath).readText(Charsets.UTF_8)
       }catch (e: FileNotFoundException){
            Log.e("FileReadError", e.toString())
        }

        // TODO テスト用にGETのキュー処理も追加
        if( (reqArray.method == "GET") or (reqArray.method == "DELETE") ) {
            response = doConnectGet(param, reqArray.uri,reqArray)
            return Pair(resCorde,response)
        }

        // TODO メソッドがポストなっていて、暫定で設定したプロジェクトコントろらーはゲットの為、エラーが起きてると思われる。
        val url = java.net.URL(reqArray.uri)
        val con = url.openConnection() as HttpURLConnection

        Log.d("【Queue】", "param:${param}")

        //タイムアウトとメソッドの設定
        con.requestMethod = reqArray.method
        con.connectTimeout = BlasRest.CONTEXT_TIME_OUT
        con.readTimeout = READ_TIME_OUT_POST

        //リクエストパラメータの設定
        con.doOutput = true
        val outStream = con.outputStream
        //リクエスト処理
        outStream.write(param.toByteArray())
        outStream.flush()
        //エラーコードなど飛んでくるのでログに出力する
        resCorde = con.responseCode
        Log.d("【rest/BlasRestAuth】", "Http_status:${resCorde}")

        //リクエスト処理処理終了
        outStream.close()

        //レスポンスデータを取得
        val responseData = con.inputStream
        response = comIs2String(responseData)

        con.disconnect()
        return Pair(resCorde,response)

    }

    private fun doConnectGet(param:String, targetUrl:String,reqArray:RestRequestData) :  String {


        var url = java.net.URL(targetUrl + "?" + param)

        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = reqArray.method
        con.connectTimeout = BlasRest.CONTEXT_TIME_OUT
        con.readTimeout = BlasRest.READ_TIME_OUT
        con.doOutput = false  //GETのときはtrueにしてはいけません

        val responseData = con.inputStream
        val response = comIs2String(responseData)
        val resCorde = con.responseCode
        con.disconnect()

        return response

    }

    private fun doSuccess(reqArray:RestRequestData,response :String) {

        //DBからデータを削除する
        val whereClauses = "queue_id = ?"
        val whereArgs = arrayOf(reqArray.request_id.toString())

        try {
            database.delete(REQUEST_TABLE, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("deleteData " + reqArray.request_id, exception.toString())
        }

    }

    private fun queueRefresh(reqArray:RestRequestData,response :String) {
        val sql = "delete from RequestTable"
        try {
            database.delete("RequestTable",null,null)
        }catch(exception: Exception) {
            Log.e("deleteError", exception.toString())
        }

    }

}