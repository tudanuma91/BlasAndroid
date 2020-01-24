package com.v3.basis.blas.blasclass.rest

import android.util.Log
import com.v3.basis.blas.blasclass.rest.BlasRest
import org.json.JSONObject

/**
 * restfulのプロジェクト関係の処理を記すクラス
 */
open class BlasRestProject : BlasRest() {
    val GET_PGOJECT_URL = URL + "projects/search/"

    /**
     * JSON文字列からデータを取得する処理
     * [引数]
     * response(文字列):restfulから取得したデータ
     *
     * [戻り値]
     *
     */
    open fun getProject(response : String): MutableList<MutableMap<String, String>> {
        val projectList : MutableList<MutableMap<String,String>> = mutableListOf()
        val rootJSON = JSONObject(response)
        //レスポンスデータからrecords配列を取得
        val datas = rootJSON.getJSONArray("records")

        //プロジェクトIDと名前を取得
        for (i in 0 until datas.length()) {
            //配列を取得
            val dataArray = datas.getJSONObject(i)
            //オブジェトに変換する
            val dataObject = dataArray.getJSONObject("Project")
            // プロジェクトIDを取得
            val project_id = dataObject.getString("project_id")
            // プロジェクト名を取得
            val project_name = dataObject.getString("name")
            Log.d("DataManagement", "ID => ${project_id}/NAME => ${project_name}")
            val project = mutableMapOf("name" to project_name , "id" to project_id)
            projectList.add(project)
        }
        return projectList
    }
}