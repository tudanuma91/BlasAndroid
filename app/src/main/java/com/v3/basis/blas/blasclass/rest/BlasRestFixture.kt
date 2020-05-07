package com.v3.basis.blas.blasclass.rest

import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasDef
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode.Companion.NETWORK_ERROR

import org.json.JSONObject
import java.io.File

open class BlasRestFixture(val crud:String = "search",
                           val payload:Map<String, String?>,
                           val funcSuccess:(JSONObject)->Unit,
                           val funcError:(Int,Int)->Unit) : BlasRest(){

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


    val cacheFileName = context.cacheDir.toString() + "/fixture_" + payload["project_id"] + ".json"
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


             //通信エラーが発生したため、キャッシュを読み込む
            if (File(cacheFileName).exists()) {
                try {
                    response = loadJson(cacheFileName)
                } catch (e: Exception) {
                    //キャッシュの読み込み失敗
                    if(method == "GET") {
                        funcError(BlasRestErrCode.FILE_READ_ERROR, APL_OK)
                        return response
                    }
                }
            } else {
                //キャッシュファイルがないため、エラーにする
                if(method == "GET") {
                    funcError(BlasRestErrCode.FILE_READ_ERROR, APL_OK)
                    return response
                }
            }

            if ((method == "POST") or (method == "PUT")){
                super.reqDataSave(payload,method,blasUrl,funcSuccess,funcError,"Item")
                aplCode = BlasDef.APL_QUEUE_SAVE

                if(response != null) {
                    json = JSONObject(response)
                    json.put("error_code", NETWORK_ERROR)
                    response = json.toString()
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
            funcError(BlasRestErrCode.NETWORK_ERROR, aplCode)
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
            funcError(errorCode, aplCode)
        }
    }
}