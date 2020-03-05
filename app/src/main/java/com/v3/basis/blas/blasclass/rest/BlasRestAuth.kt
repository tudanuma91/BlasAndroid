package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.v3.basis.blas.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

/**
 * restfulの認証APIクラス
 */
open class BlasRestAuth(val payload:Map<String, String?>, val loginSuccess:(String)->Unit, val loginError:(Int)->Unit) : BlasRest() {
    companion object {
        private val LOGIN_URL = BlasRest.URL + "auth/login/"
        private val cacheFileName = context.filesDir.toString() + "/token.json"
    }

    /**
     * BLASと通信を行う
     * @params post
     */
    override fun doInBackground(vararg params: String?): String? {
        //レスポンスデータを取得
        //レスポンスデータをJSON文字列にする
        var response:String? = null
        try {
            response = super.getResponseData(payload,"POST",LOGIN_URL)
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)
        }

        return response
    }


    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: String?) {
        if(result == null) {
            //val message = context.resources.getString(R.string.network_error)
            loginError(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)
        //トークン取得

        val json = JSONObject(result)

        val error_code = json.getInt("error_code")

        if(error_code == 0) {
            val records_json = json.getJSONObject("records")
            val token = records_json.getString("token")
            loginSuccess(token)
        }
        else {
            loginError(error_code)
        }
    }
}