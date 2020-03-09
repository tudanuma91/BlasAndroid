package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONObject


/**
 * BLASのデータにアクセスするクラス
 */
open class BlasRestItem(
    val crud:String = "search",
    payload:Map<String, String?>,
    successFun:(JSONObject)->Unit,
    errorFun:(Int)->Unit
) : BlasRest( payload,successFun,errorFun ) {

    companion object {
        val TABLE_NAME = "Item"
    }

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        var method = "GET"
        var blasUrl = BlasRest.URL + "items/search/"

        when(crud) {
            "search"->{
                method = "GET"
                blasUrl = BlasRest.URL + "items/search/"
            }
            "create"->{
                method = "POST"
                blasUrl = BlasRest.URL + "items/create/"
            }
            "update"->{
                method = "PUT"
                blasUrl = BlasRest.URL + "items/update/"
            }
            "delete"->{
                method = "DELETE"
                blasUrl = BlasRest.URL + "items/delete/"
            }
        }

        try {
            Log.d("konishi", method)
            Log.d("konishi", blasUrl)

            response = super.getResponseData(payload,method, blasUrl)

            //TODO テスト用にキューの呼出し追加
         //   super.reqDataSave(payload,method,blasUrl,funcSuccess,funcError,"Item")

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