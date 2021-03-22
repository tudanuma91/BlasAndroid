package com.v3.basis.blas.blasclass.rest
/**
 * restfulのイベントクラス。データ型23に対応したクラス
 */
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasDef
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode.Companion.NETWORK_ERROR

import org.json.JSONObject
import java.io.File

open class BlasRestEvent(val crud:String = "search",
                           val payload:Map<String, String?>,
                           val funcSuccess:(JSONObject)->Unit,
                           val funcError:(Int,Int)->Unit) : BlasRest(){

    companion object {
        val TABLE_NAME = "Items"
    }
    val GET_FIXTURE_URL = BlasRest.URL +"items/search"
    val UPDATE_FIXTURE_URL = BlasRest.URL +"items/update"

    var method = "GET"
    var aplCode:Int = 0


    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        var json:JSONObject? = null
        var errorCode = 0

        var blasUrl = BlasRest.URL + "items/search/"


        try {
            Log.d("method:", method)
            Log.d("url:", blasUrl)
            response = super.getResponseData(payload,method, blasUrl)
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            if(response != null) {
                json = JSONObject(response)
                json.put("error_code", NETWORK_ERROR)
                response = json.toString()
            }
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

        val errorCode = json.getInt("error_code")

        if(errorCode == 0) {
            funcSuccess(json)
        }
        else {
            funcError(errorCode, aplCode)
        }
    }
}