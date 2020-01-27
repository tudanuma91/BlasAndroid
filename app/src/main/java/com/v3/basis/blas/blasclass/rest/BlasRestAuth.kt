package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

/**
 * restfulの認証関係を記すクラス
 */
open class BlasRestAuth(val loginSuccess:(String)->Unit) : BlasRest() {
    companion object {
        val LOGIN_URL = BlasRest.URL + "auth/login/"
    }
    override fun doInBackground(vararg params: String?): String {
        val key = listOf("name","password")
        //レスポンスデータを取得
        //レスポンスデータをJSON文字列にする
        val response = super.getResponseData(params,key,"POST",LOGIN_URL)
        return response
    }

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        //トークン取得
        Log.d("konshi", result)
        /* TODO:エラー時の処理を追加すること */

        val token = this.getToken(result)
        Log.d("konshi", "${token}")
        Log.d("konshi", "${loginSuccess}")
        if(token != null && loginSuccess != null) {
            loginSuccess(token)
        }
    }

    /**
     * JSON文字列で渡されたレコードからトークンを取得する。
     */
    open fun getToken(response : String?):String?{
        val rootJSON = JSONObject(response)
        val error_code = rootJSON.getInt("error_code")
        var token = null
        if(error_code == 0) {
            //JSON文字列からrecordsを取得
            val recordsJSON = rootJSON.getJSONObject("records")
            //取得したrecordsからtokenを取得
            var token = recordsJSON.getString("token")
            return token
        }
        return token
    }

}