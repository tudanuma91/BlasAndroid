package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONObject


/**
 * BLASの会社情報を取得するクラス
 */
open class BlasRestOrgs(
    payload:Map<String, String?>,
    successFun:(JSONObject)->Unit,
    errorFun:(Int)->Unit
) : BlasRest( payload,successFun,errorFun ) {

    companion object {
        val ORGS_SEARCH_URL = BlasRest.URL + "orgs/search/"
    }

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        try {
            response = super.getResponseData(payload,"GET", BlasRestOrgs.ORGS_SEARCH_URL)
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)
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
            errorFun(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)

        val rtn:RestfulRtn = cakeToAndroid(result, "Orgs")
        if(rtn == null) {
            errorFun(BlasRestErrCode.JSON_PARSE_ERROR)
        }
        else if(rtn.errorCode == 0) {
            //successFun(rtn.records)
            successFun(JSONObject(result))
        }
        else {
            errorFun(rtn.errorCode)
        }
    }
}