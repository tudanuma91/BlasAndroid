package com.v3.basis.blas.blasclass.rest

import android.util.Log


/**
 * BLASのプロジェクトに設定されているフィールド情報を取得するクラス
 */
open class BlasRestImageField(val payload:Map<String, String?>,
                              val funcSuccess:(MutableList<MutableMap<String, String?>>?)->Unit,
                              val funcError:(Int)->Unit) : BlasRest() {

    companion object {
        val SEARCH_URL = BlasRest.URL + "project_images/search/"
        val TABLE_NAME = "ProjectImages"
    }

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        try {
            response = super.getResponseData(payload,"GET", SEARCH_URL)
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)
        }
        return response
    }


    /**
     * プロジェクトに設定されているフィールドの情報を取得したときにコールされる
     * 取得成功時、fieldSearchSuccessをコールする
     * 失敗時、fieldSearchErrorをコールする
     * @param なし
     *
     */
    override fun onPostExecute(result: String?) {
        if(result == null) {
            funcError(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)

        val rtn:RestfulRtn = cakeToAndroid(result, TABLE_NAME)
        if(rtn == null) {
            funcError(BlasRestErrCode.JSON_PARSE_ERROR)
        }
        else if(rtn.errorCode == 0) {
            funcSuccess(rtn.records)
        }
        else {
            funcError(rtn.errorCode)
        }
    }
}