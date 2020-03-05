package com.v3.basis.blas.blasclass.rest

import android.util.Log
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import kotlin.reflect.KFunction1


/**
 * BLASのプロジェクトに設定されているフィールド情報を取得するクラス
 */
open class BlasRestField(
    val payload:Map<String, String?>,
//    val fieldSearchSuccess: KFunction1<@ParameterName(name = "result") MutableList<MutableMap<String, String?>>?, Unit>,
//    val fieldSearchError: KFunction1<@ParameterName(name = "errorCode") Int, Unit>
    val fieldSearchSuccess: (MutableList<MutableMap<String, String?>>?)->Unit,
    val fieldSearchError: (Int)->Unit


) : BlasRest() {

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

            //TODO テスト用にキューの呼出し追加
         //   super.reqDataSave(payload,"GET",FIELD_SEARCH_URL,fieldSearchSuccess,fieldSearchError,"Fields")

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