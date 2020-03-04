package com.v3.basis.blas.blasclass.rest

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import java.net.HttpURLConnection
import android.os.AsyncTask
import com.v3.basis.blas.blasclass.app.BlasApp
import org.json.JSONException
import org.json.JSONObject
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.database
import java.io.*
import java.util.*




/**
 * 返却用データクラス
 */
data class RestfulRtn(
    val errorCode: Int,
    val message: String?,
    val records: MutableList<MutableMap<String, String?>>?
)




/**
 * Restful通信をする際に使用するクラスの親クラス
 */
open class BlasRest() : AsyncTask<String, String, String>() {

    companion object {
        // const val URL = "http://192.168.0.101/blas7/api/v1/"
        const val URL = "http://192.168.1.8/blas7/api/v1/"
        const val CONTEXT_TIME_OUT = 1000
        const val READ_TIME_OUT = 1000
      //  var submitList = mutableMapOf<Int,(MutableMap<String, Int>)->Unit,Int >()
    }


    override fun doInBackground(vararg params: String?): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * オブジェクトをJSON文字列に変換するメソッド
     * [引数]
     * stream(オブジェクト) :　restful通信にて取得したデータ。
     *
     * [返り値]
     * sb.toString(文字列) : streamを文字列にして返す。
     * */
    open fun is2String(stream: InputStream): String{
       val sb = StringBuilder()
       val reader = BufferedReader(InputStreamReader(stream,"UTF-8"))
        Log.d("[rest/BlasRest]","{$reader}")
       var line = reader.readLine()
       if(line != null){
           sb.append(line)
           line = reader.readLine()
       }
       reader.close()
       return sb.toString()
   }

    private fun methodGet(payload:Map<String, String?>, targetUrl:String):String {

        var urlBuilder = Uri.Builder()
        for((k, v) in payload) {
            urlBuilder.appendQueryParameter(k, v)
        }

        var url = java.net.URL(targetUrl + urlBuilder.toString())

        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.connectTimeout = CONTEXT_TIME_OUT
        con.readTimeout = READ_TIME_OUT
        con.doOutput = false  //GETのときはtrueにしてはいけません

        val responseData = con.inputStream
        val response = this.is2String(responseData)
        con.disconnect()

        return response
    }

    /**
     * restful通信を行いデータのオブジェクトを取得する
     * [引数]
     * params(配列) : トークンや入力されたデータ等、送信するデータの値を格納した配列
     * key(リスト) : 送信するデータの要素を示す配列
     * method(文字列) : 通信方式
     * targetUrl(文字列) : 接続するURL
     *
     * [戻り値]
     * responseData:オブジェクトを返却
     */
    open fun getResponseData(payload:Map<String, String?>,method:String,targetUrl:String): String {
        var response = ""

        if( (method == "GET") or (method == "DELETE") ) {
            response = methodGet(payload, targetUrl)
        } else {

            val url = java.net.URL(targetUrl)
            val con = url.openConnection() as HttpURLConnection

            Log.d("konishi", "connect Ok")
            //POSTするデータの作成

            var postData: String = ""

            for ((k, v) in payload) {
                postData += "${k}=${v}&"
            }

            postData = postData.substring(0, postData.length - 1)

            Log.d("【rest/BlasRest】", "postData:${postData}")

            //タイムアウトとメソッドの設定
            con.requestMethod = method
            con.connectTimeout = CONTEXT_TIME_OUT
            con.readTimeout = READ_TIME_OUT

            //リクエストパラメータの設定
            con.doOutput = true
            val outStream = con.outputStream
            //リクエスト処理
            outStream.write(postData.toByteArray())
            outStream.flush()
            //エラーコードなど飛んでくるのでログに出力する
            val resCorde = con.responseCode
            Log.d("【rest/BlasRestAuth】", "Http_status:${resCorde}")


            //リクエスト処理処理終了
            outStream.close()

            //レスポンスデータを取得
            val responseData = con.inputStream
            response = this.is2String(responseData)
            con.disconnect()



        }
        return response
    }


    /**
     * restful通信を行う為、SqliteDBに処理を保存する
     * [引数]
     * payload(リスト) : トークンや入力されたデータ等、送信するデータの値を格納した配列
     * method(文字列) : 通信方式
     * targetUrl(文字列) : 接続するURL
     *
     * [戻り値]
     */
    open fun reqDataSave(payload:Map<String, String?>,method:String,targetUrl:String,funSuccess:(MutableMap<String,Int>)->Unit,funError:(Int)->Unit) {

        Log.d("【reqDataSave】", "開始")

        //　パラメータのファイルの書込み
        val uid = UUID.randomUUID().toString()

        val fileDir = BlasApp.applicationContext().getFilesDir().getPath()
        val fileName = "param_" + uid + ".txt"

        val filePath = fileDir+ "/" + fileName
        val file = FileWriter(filePath)
        val pw = PrintWriter(BufferedWriter(file))

        var paramData: String = ""
        for ((k, v) in payload) {
            paramData += "${k}=${v}&"
        }
        paramData = paramData.substring(0, paramData.length - 1)

        pw.println(paramData)
        pw.close()

        val values = ContentValues()
        values.put("uri", targetUrl)
        values.put("method", method)
        values.put("param_file", fileName)
        values.put("retry_count", 0)
        values.put("status", 0)

        try {
           val last_insert  = database.insertOrThrow("RequestTable", null, values)
           // submitList.put(last_insert.toInt(),funSuccess,funError)

        }catch(exception: Exception) {
            Log.e("insertError", exception.toString())
        }


    }

    /**
     * cakePHPから返却されたデータをandroidで使用しやすい形式に変換する。
     * @param jsonRecord 文字列形式のjson
     * @param tableName cakePHPのテーブル名
     * @return RestfulRtnクラス(データクラス)
     */
    public fun cakeToAndroid(jsonRecord:String, tableName:String): RestfulRtn{
        //返却用エラーコード
        var errorCode = 0
        //返却用メッセージ
        var message = ""
        //返却用リスト
        var recordList:MutableList<MutableMap<String, String?>>? = mutableListOf<MutableMap<String, String?>>()

        try {
            val root = JSONObject(jsonRecord)
            //エラーコード取得
            errorCode = root.getInt("error_code")
            //メッセージ取得
            message = root.getString("message")

            if(errorCode == 0) {
                //正常時だけレコードがあるため、取得する
                val records = root.getJSONArray("records")

                for (i in 0 until records.length()) {

                    var fields = JSONObject(records[i].toString())

                    for (j in 0 until fields.length()) {
                        var data = JSONObject(fields[tableName].toString())  //指定されたテーブルを取得する
                        var recordMap = mutableMapOf<String, String?>()
                        for (k in data.keys()) {
                            recordMap[k] = data[k].toString()
                        }
                        if(recordList != null) {
                            recordList.add(recordMap)
                        }
                    }
                }
            }
            else {
                recordList = null
            }
        }
        catch(e: JSONException) {
            Log.d("konishi", e.message)
            recordList = null
        }
        return RestfulRtn(errorCode, message, recordList)
    }
}
