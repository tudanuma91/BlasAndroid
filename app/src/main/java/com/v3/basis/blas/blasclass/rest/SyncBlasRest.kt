package com.v3.basis.blas.blasclass.rest

import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.decrypt
import com.v3.basis.blas.blasclass.app.encrypt
import com.v3.basis.blas.blasclass.app.getHash
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.database
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.util.*

/**
 * Restful通信をする際に使用するクラスの親クラス
 */
@Suppress("DEPRECATION")
open class SyncBlasRest() {

    companion object {

        const val URL = BuildConfig.API_URL
//        const val URL = "https://www.basis-service.com/blas70/api/v1/"

        const val CONTEXT_TIME_OUT = 100000
        const val READ_TIME_OUT = 100000
        var queuefuncList = mutableListOf<FuncList>()
        val context = BlasApp.applicationContext()
        var userNameRest:String? = null
        var passwordRest:String? = null

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
        //responseDataがnull?
        //どちらにしても下行 val responseData = con.inputStreamが機能していない可能性が高い

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

        if( (method == "GET") ) {
            response = methodGet(payload, targetUrl)
        } else {

            val url = java.net.URL(targetUrl)
            val con = url.openConnection() as HttpURLConnection

            Log.d("konishi", "connect Ok")
            //POSTするデータの作成

            var postData: String = ""

            for ((k, v) in payload) {
                postData += "${k}=${URLEncoder.encode(v,"UTF-8")}&"
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
            Log.d("【BlasRest】", "Http_status:${resCorde}")


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
     * funSuccess(リストマップ):成功時のコールバック
     * funError(マップ):エラー時のコールバック
     * tableName(テーブル名)
     *
     * [戻り値]
     */
    open fun reqDataSave(payload:Map<String, String?>,method:String,targetUrl:String,funSuccess:(JSONObject)->Unit,funError:(Int,Int)->Unit,tableName:String) {

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
            paramData += "${k}=${URLEncoder.encode(v,"UTF-8")}&"
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

            var reqFunc = FuncList(last_insert.toInt(),funSuccess,funError,tableName)

            queuefuncList.add(reqFunc)

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

    /**
     * 電波測定用関数
     */
    fun isOnline(context: Context): Boolean? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting
    }

    /**
     * キャッシュ保存
     * @param fileName キャッシュの保存ファイル名
     * @param jsonText json形式のテキスト
     */
    fun saveJson(fileName:String, jsonText:String) {


        val disposable = CompositeDisposable()

        Completable
            .fromAction {
                File(fileName).writer().use {
                    val hashSource = userNameRest + passwordRest
                    val hash = getHash(hashSource)
                    val shortHash = hash.substring(0,16)

                    try{
                        val encJsonText = encrypt(jsonText,shortHash)
                        it.write(encJsonText)
                    } catch(e: Exception){
                        Log.d("Encryot Error", e.message)
                    }
                }
            }
            .subscribeOn(Schedulers.newThread())
            .doOnComplete { disposable.dispose() }
            .subscribe()
            .addTo(disposable)
    }



    /**
     * json形式のテキストファイルを読み込み，jsonObjectとして返却する
     * @param fileName 読み込むファイル名
     * @return JSONObject
     */
    fun loadJson(fileName:String):String {
        var jsonText = ""
        lateinit var decJsonText:String

        File(fileName).reader().use {
            jsonText = it.readText()
        }

        val hashSource = userNameRest + passwordRest
        val hash = getHash(hashSource)
        val shortHash = hash.substring(0,16)

        try {
            decJsonText = decrypt(jsonText, shortHash)
        }catch (e:Exception){
            Log.d("Decrypt Error", e.message)
        }

        return decJsonText
    }




}
