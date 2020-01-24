package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

/**
 * restfulの認証関係を記すクラス
 */
open class BlasRestAuth : BlasRest() {
    val LOGIN_URL = BlasRest().URL + "auth/login/"
    val LOGOUT_URL = BlasRest().URL + "auth/logout/"

    /**
     * JSON文字列で渡されたレコードからトークンを取得する。
     */
    open fun getToken(response : String):String{
        val rootJSON = JSONObject(response)
        //JSON文字列からrecordsを取得
        val recordsJSON = rootJSON.getJSONObject("records")
        //取得したrecordsからtokenを取得
        val token = recordsJSON.getString("token")
        Log.d("【rest/BlasRestAuth】", "token:${token}")
        return token
    }

}