package com.v3.basis.blas.blasclass.rest

import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import org.json.JSONException
import org.json.JSONObject


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