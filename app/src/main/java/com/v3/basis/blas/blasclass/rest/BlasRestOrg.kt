package com.v3.basis.blas.blasclass.rest

import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File


/**
 * BLASの会社情報を取得するクラス
 */
open class BlasRestOrgs(val payload:Map<String, String?>,
                        val orgsSearchSuccess:(JSONObject)->Unit,
                        val orgsSearchError:(Int,Int)->Unit) : BlasRest() {


    var cacheFileName = context.cacheDir.toString() + "/org.json"


    companion object {
        val ORGS_SEARCH_URL = BlasRest.URL + "orgs/search/"
    }

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        try {
            response = super.getResponseData(payload,"GET", BlasRestOrgs.ORGS_SEARCH_URL)
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            //通信エラーが発生したため、キャッシュを読み込む
            if(File(cacheFileName).exists()) {
                try {
                    response = loadJson(cacheFileName)
                } catch(e: Exception) {
                    //キャッシュの読み込み失敗
                    orgsSearchError(BlasRestErrCode.FILE_READ_ERROR, APL_OK)
                }
            } else {
                //キャッシュファイルがないため、エラーにする
                orgsSearchError(BlasRestErrCode.NETWORK_ERROR ,APL_OK)
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
            orgsSearchError(BlasRestErrCode.NETWORK_ERROR, APL_OK)
            return
        }

        super.onPostExecute(result)

        //val rtn:RestfulRtn = cakeToAndroid(result, "Orgs")
        //BLASから取得したデータをjson形式に変換する
        var json:JSONObject? = null
        var errorCode:Int
        var records: JSONArray? = null
        try {
            json = JSONObject(result)
            //エラーコード取得
            errorCode = json.getInt("error_code")
            records = json.getJSONArray("records")

        } catch (e: JSONException){
            //JSONの展開に失敗
            Toast.makeText(context, "データ取得失敗", Toast.LENGTH_LONG).show()
            return
        }

        if(errorCode == 0) {
            if(records != null) {
                saveJson(cacheFileName, result)
            }

            orgsSearchSuccess(json)
        }
        else {
            orgsSearchError(errorCode ,APL_OK)
        }
    }
}