package com.v3.basis.blas.blasclass.rest

import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import org.json.JSONException
import org.json.JSONObject
import java.io.File


/**
 * BLASのデータにアクセスするクラス
 */
open class BlasRestItem(val crud:String = "search",
                        val payload:Map<String, String?>,
                        val funcSuccess:(JSONObject)->Unit,
                        val funcError:(Int)->Unit) : BlasRest() {

    companion object {
        val TABLE_NAME = "Item"
    }

    init{
        cacheFileName = context.filesDir.toString() + "/item_" + payload["project_id"] + ".json"
    }

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        Log.d("BlasRestItem","doInBackground() start")


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

        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            if(method == "GET") {

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
            }else if ((method == "POST") or (method == "PUT")){
                super.reqDataSave(payload,method,blasUrl,funcSuccess,funcError,"Item")
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

        //BLASから取得したデータをjson形式に変換する
        var json:JSONObject? = null
        var errorCode:Int
        try {
            json = JSONObject(result)
            //エラーコード取得
            errorCode = json.getInt("error_code")

        } catch (e: JSONException){
            //JSONの展開に失敗
            Toast.makeText(context, "データ取得失敗", Toast.LENGTH_LONG).show()
            return
        }

        if(json == null) {
            funcError(BlasRestErrCode.JSON_PARSE_ERROR)
        }
        else if(errorCode == 0) {
            funcSuccess(json)
        }
        else {
            funcError(errorCode)
        }
    }
}