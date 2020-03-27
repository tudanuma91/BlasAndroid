package com.v3.basis.blas.blasclass.rest

import android.util.Log

import org.json.JSONObject
import java.io.File

open class BlasRestFixture(val crud:String = "search",
                           val payload:Map<String, String?>,
                           val funcSuccess:(JSONObject)->Unit,
                           val funcError:(Int)->Unit) : BlasRest(){

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

    init{
        cacheFileName = context.filesDir.toString() + "/fixture_" + payload["project_id"] + ".json"
    }
    var method = "GET"

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null

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
            Log.d("method:", method)
            Log.d("url:", blasUrl)
            response = super.getResponseData(payload,method, blasUrl)

        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            if (method == "GET") {

                //通信エラーが発生したため、キャッシュを読み込む
                if (File(cacheFileName).exists()) {
                    try {
                        response = loadJson(cacheFileName)
                    } catch (e: Exception) {
                        //キャッシュの読み込み失敗
                        funcError(BlasRestErrCode.FILE_READ_ERROR)
                    }
                } else {
                    //キャッシュファイルがないため、エラーにする
                    funcError(BlasRestErrCode.NETWORK_ERROR)
                }
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
            funcError(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)
        val json = JSONObject(result)
        // val rtn:RestfulRtn = cakeToAndroid(result, TABLE_NAME)
        val errorCode = json.getInt("error_code")


        if(method == "GET" && errorCode == 0) {

            val records = json.getJSONArray("records")
            if(records != null){
                saveJson(cacheFileName, result)
            }
        }
        if(errorCode == 0) {
            funcSuccess(json)
        }
        else {
            funcError(errorCode)
        }
    }
}