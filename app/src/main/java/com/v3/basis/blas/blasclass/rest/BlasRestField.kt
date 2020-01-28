package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

/**
 *
 */
open class BlasRestField(val payload:Map<String, String?>,
                         val fieldSearchSuccess:(MutableList<MutableMap<String, String?>>?)->Unit,
                         val fieldSearchError:(Int)->Unit) : BlasRest() {

    companion object {
        val FIELD_SEARCH_URL = BlasRest.URL + "project_fields/search/"
    }

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        try {
            response = super.getResponseData(payload,"GET", BlasRestField.FIELD_SEARCH_URL)
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
            fieldSearchError(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)
        val rtn:RestfulRtn = cakeToAndroid(result, "Fields")
        if(rtn == null) {
            fieldSearchError(BlasRestErrCode.JSON_PARSE_ERROR)
        }
        else if(rtn.errorCode == 0) {
            fieldSearchSuccess(rtn.records)
        }
        else {
            fieldSearchError(rtn.errorCode)
        }
    }
}