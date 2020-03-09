package com.v3.basis.blas.blasclass.rest

import android.util.Log

import org.json.JSONObject

open class BlasRestFixture(val crud:String = "search",
                           payload:Map<String, String?>,
                           successFun:(JSONObject)->Unit,
                           errorFun:(Int)->Unit)
    : BlasRest( payload,successFun,errorFun ){


    companion object {
        val TABLE_NAME = "Fixture"
    }
    val CREATE_FIXTURE_URL  = BlasRest.URL +"fixtures/create"
    val GET_FIXTURE_URL = BlasRest.URL +"fixtures/search"
    val UPDATE_FIXTURE_URL = BlasRest.URL +"fixtures/update"
    val DELETE_FIXTURE_URL = BlasRest.URL +"fixtures/delete"
    val KENPIN_FIXTURE_URL = BlasRest.URL +"fixtures/kenpin"
    val TOUROKU_FIXTURE_URL = BlasRest.URL +"fixtures/takeout"
    val RETURN_FIXTURE_URL = BlasRest.URL +"fixtures/trn"



    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        var method = "GET"
        var blasUrl = BlasRest.URL + "fixtures/search/"

        when(crud) {
            "search"->{
                method = "GET"
                blasUrl = BlasRest.URL + "fixtures/search/"
            }
            "create"->{
                method = "POST"
                blasUrl = BlasRest.URL + "fixtures/create/"
            }
            "update"->{
                method = "PUT"
                blasUrl = BlasRest.URL + "fixtures/update/"
            }
            "delete"->{
                method = "DELETE"
                blasUrl = BlasRest.URL + "fixtures/delete/"
            }
            "kenpin"->{
                method = "PUT"
                blasUrl = BlasRest.URL + "fixtures/kenpin/"
            }
            "takeout"->{
                method = "PUT"
                blasUrl = BlasRest.URL + "fixtures/takeout/"
            }
            "rtn"->{
                method = "PUT"
                blasUrl = BlasRest.URL + "fixtures/rtn/"
            }
        }

        try {
            Log.d("konishi", method)
            Log.d("konishi", blasUrl)
            response = super.getResponseData(payload,method, blasUrl)

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
        val json = JSONObject(result)
       // val rtn:RestfulRtn = cakeToAndroid(result, TABLE_NAME)
        val errorCode = json.getInt("error_code")
        //val records = json.getJSONArray("records")
        if(errorCode == 0) {
            successFun(json)
        }
        else {
            errorFun(errorCode)
        }
    }
}