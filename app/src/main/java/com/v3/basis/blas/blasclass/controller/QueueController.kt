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
                    var result  = doPost(reqList[i])

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

    private fun doPost(reqArray:RestRequestData) :  Pair <Int,String> {
        val param : String = ""

        val fileDir = BlasSQLDataBase.context.getFilesDir().getPath()
        val filePath: String = fileDir + "/" + reqArray.param_file
        val test = "なんでみえない"

        //ファイルからパラメータ取得
        try{
            val reader: BufferedReader = File(filePath).bufferedReader()
            val param = reader.use { it.readText() }
        }catch (e: FileNotFoundException){
            Log.e("FileReadError", e.toString())
        }

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
        val resCorde = con.responseCode
        Log.d("【rest/BlasRestAuth】", "Http_status:${resCorde}")

        //リクエスト処理処理終了
        outStream.close()

        //レスポンスデータを取得
        val responseData = con.inputStream
        val response = comIs2String(responseData)

        con.disconnect()
        return Pair(resCorde,response)

    }

    private fun doSuccess(reqArray:RestRequestData,response :String) {

        //DBからデータを削除する
        val whereClauses = "id = ?"
        val whereArgs = arrayOf(reqArray.request_id.toString())

        try {
            database.delete(REQUEST_TABLE, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("deleteData " + reqArray.request_id, exception.toString())
        }


    }

}