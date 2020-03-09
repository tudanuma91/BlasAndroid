package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONObject


/**
 * BLASのプロジェクトに設定されているフィールド情報を取得するクラス
 */
open class BlasRestImageField(
    payload:Map<String, String?>,
    successFun:(JSONObject)->Unit,
    errorFun:(Int)->Unit
) : BlasRest( payload,successFun,errorFun ) {

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
            errorFun(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)

        val rtn:RestfulRtn = cakeToAndroid(result, TABLE_NAME)
        if(rtn == null) {
            errorFun(BlasRestErrCode.JSON_PARSE_ERROR)
        }
        else if(rtn.errorCode == 0) {
            //successFun(rtn.records)
            successFun( JSONObject(result) )
        }
        else {
            errorFun(rtn.errorCode)
        }
    }
}