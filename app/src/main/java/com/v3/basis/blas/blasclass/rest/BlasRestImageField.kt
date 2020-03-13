package com.v3.basis.blas.blasclass.rest

import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import org.json.JSONException
import org.json.JSONObject


/**
 * BLASのプロジェクトに設定されているフィールド情報を取得するクラス
 */
open class BlasRestImageField(val payload:Map<String, String?>,
                              val funcSuccess:(JSONObject)->Unit,
                              val funcError:(Int)->Unit) : BlasRest() {

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