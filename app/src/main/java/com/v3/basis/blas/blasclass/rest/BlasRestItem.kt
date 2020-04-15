package com.v3.basis.blas.blasclass.rest

import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_QUEUE_SAVE
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File


/**
 * BLASのデータにアクセスするクラス
 */
open class BlasRestItem(val crud:String = "search",
                        val payload:Map<String, String?>,
                        val funcSuccess:(JSONObject)->Unit,
                        val funcError:(Int,Int)->Unit) : BlasRest() {

    companion object {
        val TABLE_NAME = "Item"
    }

    init{
        cacheFileName = context.filesDir.toString() + "/item_" + payload["project_id"] + ".json"
    //    uniqueCheckFile = context.filesDir.toString() + "/uniqueCheck_" + payload["project_id"] + ".json"
    }
    var method = "GET"

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        Log.d("BlasRestItem","doInBackground() start")


        var response:String? = null

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
            super.reqDataSave(payload,method,blasUrl,funcSuccess,funcError,"Item")
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            if (File(cacheFileName).exists()) {
                try {
                    response = loadJson(cacheFileName)
                } catch (e: Exception) {
                    //キャッシュの読み込み失敗
                    funcError(BlasRestErrCode.FILE_READ_ERROR,APL_OK)
                }
            }else{
                    if(method == "GET") {
                        //キャッシュファイルがないため、エラーにする
                        funcError(BlasRestErrCode.NETWORK_ERROR,APL_OK)
                    }
            }

            if ((method == "POST") or (method == "PUT")){
                // 重複エラーのチェック
                val resultList = dupliCheck(payload,response)

                if (resultList.size == 0){
                    super.reqDataSave(payload,method,blasUrl,funcSuccess,funcError,"Item")
                    val json = JSONObject(response)
                    json.put("aplCode",APL_QUEUE_SAVE)
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
            funcError(BlasRestErrCode.NETWORK_ERROR,APL_OK)
            return
        }

        super.onPostExecute(result)

        //BLASから取得したデータをjson形式に変換する
        var json:JSONObject? = null
        var errorCode:Int
        var records:JSONArray? = null
        var aplCode:Int = 0

        try {
            json = JSONObject(result)
            //エラーコード取得
            errorCode = json.getInt("error_code")
            if (json.has("aplCode")){
                aplCode = json.getInt("aplCode")
            }


        } catch (e: JSONException){
            //JSONの展開に失敗
            Toast.makeText(context, "データ取得失敗", Toast.LENGTH_LONG).show()
            return
        }

        if(method == "GET" && errorCode == 0) {
            records = json.getJSONArray("records")

            if(records != null){
                saveJson(cacheFileName, result)
            }
        }

        if(json == null) {
            funcError(BlasRestErrCode.JSON_PARSE_ERROR,aplCode)
        }
        else if(errorCode == 0) {
            funcSuccess(json)
        }
        else {
            funcError(errorCode,aplCode)
        }
    }

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in payload 画面からの入力
     * @param in params キャッシュファイルから取得したデータ
     */
    fun dupliCheck(payload : Map<String, String?>, response : String?) : MutableList<String> {

        var idex:Int
        val responseJson = JSONObject(response)
        val check = responseJson.getJSONArray("checkField")
        val resultList: MutableList<String> = mutableListOf()

        for (idex in 0 until check.length()){
            val field = check.get(idex)
            val fieldJson = JSONObject(field.toString())
            val valLIst = fieldJson.getJSONArray(check[idex].toString())

            for ((payKey, payValue) in payload) {
                for (j in 0 until valLIst.length()) {
                    if(payKey == field.toString()){
                        if(payValue == valLIst[j].toString()){
                            resultList.add(payKey)
                            break
                        }
                    }
                }
            }
        }

        return resultList

    }

}