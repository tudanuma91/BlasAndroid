package com.v3.basis.blas.blasclass.rest

import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.KFunction1


/**
 * BLASのプロジェクトに設定されているフィールド情報を取得するクラス
 */
open class BlasRestField(
    val payload:Map<String, String?>,
    val fieldSearchSuccess:(JSONObject)->Unit,
    val fieldSearchError:(Int)->Unit
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

        // val rtn:RestfulRtn = cakeToAndroid(result, "Fields")

        //BLASから取得したデータをjson形式に変換する
        var json: JSONObject? = null
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

        if(errorCode == 0) {
            fieldSearchSuccess(json)
        }
        else {
            fieldSearchError(errorCode)
        }
    }
}