package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.security.MessageDigest

/**
 * restfulの認証APIクラス
 */
open class BlasRestAuth(val payload:Map<String, String?>, val loginSuccess:(JSONObject)->Unit, val loginError:(Int)->Unit) : BlasRest() {
    companion object {
        private val LOGIN_URL = BlasRest.URL + "auth/login/"
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
            Log.d("値のチェック","${json}")
            val records_json = json.getJSONObject("records")

            userNameRest = payload["name"]
            passwordRest = payload["password"]

            if(!records_json.has("salt")) {
                loginError(10001)//バージョンエラー
                return
            }

            // passwordで実現するのは無理だった…
            val saltPassword = records_json.getString("salt") +  payload["password"]
            if(saltPassword != null) {
                BlasApp.key = MessageDigest.getInstance("SHA1")
                    .digest(saltPassword.toByteArray())
                    .joinToString(separator = "") {
                        "%02x".format(it)
                    }
                Log.d("konishi", "password is ${BlasApp.key}")
                loginSuccess(json)
            }
            else {
                loginError(10001)//バージョンエラー
                return
            }
        }
        else {
            loginError(error_code)
        }
    }
}