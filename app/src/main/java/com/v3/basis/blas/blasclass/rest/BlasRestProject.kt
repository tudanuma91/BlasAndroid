package com.v3.basis.blas.blasclass.rest

import android.util.Log
import com.v3.basis.blas.ui.project.project_list_view.RowModel
import org.json.JSONObject

/**
 * restfulのプロジェクトAPIクラス
 * @param in payload マップ形式のrestfulのrequestに必要なパラメーター
 * @param in projectSearchSuccess プロジェクト取得時にコールバックする関数
 * @param in projectSearchError プロジェクト取得失敗時にコールバックする関数
 */
open class BlasRestProject(val payload:Map<String, String?>,
                           val projectSearchSuccess:(MutableMap<String,Int>)->Unit,
                           val projectSearchError:(Int)->Unit) : BlasRest() {
    val SEARCH_PGOJECT_URL = BlasRest.URL + "projects/search/"


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

            // TODO テスト用に呼出し
          //  super.reqDataSave(payload,"GET",SEARCH_PGOJECT_URL,projectSearchSuccess,projectSearchError)

        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)
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
        if(result == null) {
            //サーバとの接続エラー
            projectSearchError(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)

        var projectMap = mutableMapOf<String, Int>()
        val json = JSONObject(result)
        val errorCode = json.getInt("error_code")
        if(errorCode == 0) {
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
            //コールバック
            projectSearchSuccess(projectMap)
        }
        else {
            //エラーが発生したため、Error関数をコールバック
            projectSearchError(errorCode)
        }
    }
}