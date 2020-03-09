package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONObject

/**
 * restfulの認証APIクラス
 */
open class BlasRestAuth(
    payload:Map<String, String?>
    , successFun:(JSONObject)->Unit
    , errorFun:(Int)->Unit
) : BlasRest( payload, successFun, errorFun ) {

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
            errorFun(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)
        //トークン取得

        val json = JSONObject(result)

        val error_code = json.getInt("error_code")

        if(error_code == 0) {
            //val records_json = json.getJSONObject("records")
            //val token = records_json.getString("token")
            //successFun(result)
            successFun(json)
        }
        else {
            errorFun(error_code)
        }
    }
}