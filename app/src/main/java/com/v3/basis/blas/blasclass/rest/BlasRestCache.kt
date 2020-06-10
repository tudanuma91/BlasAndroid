package com.v3.basis.blas.blasclass.rest

import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasDef
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode.Companion.NETWORK_ERROR
import com.v3.basis.blas.ui.ext.traceLog
import org.json.JSONObject

open class BlasRestCache(val crud:String = "zip",
                         val payload:Map<String, String?>,
                         val funcSuccess:(JSONObject)->Unit,
                         val funcError:(Int,Int)->Unit) : BlasRest(){


    val method = "GET"
    var aplCode:Int = 0


    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        var json:JSONObject? = null
        var errorCode = 0

        val url = BlasRest.URL + "cache/search_sqlite/"

        try {
            Log.d("method:", method)
            Log.d("url:", url)
            response = super.getResponseData(payload, method, url)

        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)
            traceLog("Failed to search zip files")
        }
        return response
    }


    /**
     * プロジェクトに設定されているフィールドの情報を取得したときにコールされる
     * 取得成功時、orgsSearchSuccessをコールする
     * 失敗時、orgsSearchErrorをコールする
     * @param なし
     *
     */
    override fun onPostExecute(result: String?) {
        if(result == null) {
            funcError(BlasRestErrCode.NETWORK_ERROR, aplCode)
            return
        }

        super.onPostExecute(result)
        val json = JSONObject(result)
        // val rtn:RestfulRtn = cakeToAndroid(result, TABLE_NAME)
        val errorCode = json.getInt("error_code")

        if(errorCode == 0) {
            funcSuccess(json)
        }
        else {
            funcError(errorCode, aplCode)
        }
    }
}
