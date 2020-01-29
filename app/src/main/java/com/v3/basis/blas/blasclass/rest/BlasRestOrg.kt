package com.v3.basis.blas.blasclass.rest

import android.util.Log


/**
 * BLASの会社情報を取得するクラス
 */
open class BlasRestOrgs(val payload:Map<String, String?>,
                         val orgsSearchSuccess:(MutableList<MutableMap<String, String?>>?)->Unit,
                         val orgsSearchError:(Int)->Unit) : BlasRest() {

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
            orgsSearchError(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)

        val rtn:RestfulRtn = cakeToAndroid(result, "Orgs")
        if(rtn == null) {
            orgsSearchError(BlasRestErrCode.JSON_PARSE_ERROR)
        }
        else if(rtn.errorCode == 0) {
            orgsSearchSuccess(rtn.records)
        }
        else {
            orgsSearchError(rtn.errorCode)
        }
    }
}