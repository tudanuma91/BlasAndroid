package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * restfulのプロジェクトAPIクラス
 * @param in payload マップ形式のrestfulのrequestに必要なパラメーター
 * @param in projectSearchSuccess プロジェクト取得時にコールバックする関数
 * @param in projectSearchError プロジェクト取得失敗時にコールバックする関数
 */
open class BlasRestProject(val payload:Map<String, String?>,
                           val projectSearchSuccess:(JSONObject)->Unit,
                           val projectSearchError:(Int,Int)->Unit) : BlasRest() {

    private val SEARCH_PGOJECT_URL = BlasRest.URL + "projects/search/"
    init{
        cacheFileName = context.cacheDir.toString() + "/project.json"
    }

    /**
     * プロジェクト一覧取得要求をBLASに送信する
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        //レスポンスデータを取得
        //レスポンスデータをJSON文字列にする
        var response:String? = null
        try {
            response = super.getResponseData(payload,"GET", SEARCH_PGOJECT_URL)

        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            //通信エラーが発生したため、キャッシュを読み込む
            if(File(cacheFileName).exists()) {
                try {
                    response = loadJson(cacheFileName)
                } catch(e: Exception) {
                    //キャッシュの読み込み失敗
                    projectSearchError(BlasRestErrCode.FILE_READ_ERROR,APL_OK)
                }
            } else {
                //キャッシュファイルがないため、エラーにする
                projectSearchError(BlasRestErrCode.NETWORK_ERROR,APL_OK)
            }
        }

        return response
    }

    /**
     * プロジェクト一覧を取得する
     * @params in result プロジェクトのjsonコード。エラー時はNULLが入る
     * @return 成功時：projectSearchSuccessをコールバックする。
     * 　　　　　異常時：projectSearchErrorをコールバックする。
     */
    override fun onPostExecute(result: String?) {

        var records: JSONArray? = null

        if(result == null) {
            projectSearchError(BlasRestErrCode.NETWORK_ERROR,APL_OK)
            return
        }

        super.onPostExecute(result)

        //BLASから取得したデータをjson形式に変換する
        var json:JSONObject? = null
        var errorCode:Int = 0

        try {
            json = JSONObject(result)
            //エラーコード取得
            errorCode = json.getInt("error_code")
            records = json.getJSONArray("records")

        } catch (e: JSONException){
            //JSONの展開に失敗
            Toast.makeText(context, "データ取得失敗", Toast.LENGTH_LONG).show()
        }

        //正常時だけキャッシュに保存する
        if(errorCode == 0 && json != null) {
            //正常のときだけキャッシュにjsonファイルを保存する
            try {
                if(records != null) {
                    saveJson(cacheFileName, result)
                }

                if(json != null) {
                    projectSearchSuccess(json)
                }
            }
            catch(e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        } else {
            projectSearchError(errorCode,APL_OK)
        }

    }

    private fun convProjectData(json:JSONObject):MutableMap<String, Int> {
        var projectMap = mutableMapOf<String, Int>()
        val records = json.getJSONArray("records")
        //プロジェクトIDと名前を取得
        for (i in 0 until records.length()) {
            //配列を取得
            val dataArray = records.getJSONObject(i)
            //オブジェトに変換する
            val dataObject = dataArray.getJSONObject("Project")
            // プロジェクトIDを取得
            val projectId:Int = dataObject.getInt("project_id")
            // プロジェクト名を取得
            val projectName = dataObject.getString("name")

            projectMap[projectName] = projectId
        }
        return projectMap
    }
}