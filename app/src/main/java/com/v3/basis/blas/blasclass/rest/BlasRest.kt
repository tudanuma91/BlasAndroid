package com.v3.basis.blas.blasclass.rest

import android.content.Intent
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import android.os.AsyncTask
import com.v3.basis.blas.activity.TerminalActivity


/**
 * Restful通信をする際に使用するクラスの親クラス
 */
abstract class BlasRest : AsyncTask<String, String, String>() {
    companion object {
        const val URL = "http://192.168.0.101/blas7/api/v1/"
        const val CONTEXT_TIME_OUT = 1000
        const val READ_TIME_OUT = 1000
        const val SUCCESS = 0
        const val ABNORMAL = 1
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
    open fun getResponseData(params: Array<out String?>, key : List<String?>,method:String,targetUrl:String): String {
        val url = java.net.URL(targetUrl)
        val con = url.openConnection() as HttpURLConnection

        //POSTするデータの作成
        var postData :String = ""
        for (i in params.indices){
            if(i < params.lastIndex)
                postData += "${key[i]}=${params[i]}&"
            else
                postData += "${key[i]}=${params[i]}"
        }
        Log.d("【rest/BlasRest】", "testutesttest:${postData}")

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
        val response = this.is2String(responseData)
        con.disconnect()

        return response
    }
}
