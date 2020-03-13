package com.v3.basis.blas.blasclass.helper

import android.util.Log
import org.json.JSONObject

class RestHelper {

    /**
     * プロジェクト名を一覧表示する際に使用する関数
     */
    fun createProjectList(result: JSONObject): MutableMap<String, MutableMap<String, String>> {
        Log.d("fun_createProjectList","start")
        //変数宣言
        val rtnMap :MutableMap<String,MutableMap<String, String>> = mutableMapOf()
        val projectList = result.getJSONArray("records")
        for(i in 0 until projectList.length()) {
            //mapの初期化
            val valueMap : MutableMap<String,String> = mutableMapOf()
            //JSoNオブジェクトを行ごとに取得。
            val jsonProject = JSONObject(projectList[i].toString())
            val project = jsonProject.getJSONObject("Project")
            //値の取得と格納
            valueMap.set(key = "project_name" ,value = project["name"].toString())
            valueMap.set(key = "project_id",value = project["project_id"].toString())
            rtnMap.set(key = i.toString() ,value = valueMap)
        }
        Log.d("fun_createProjectList","goal")
        return rtnMap
    }


    /**
     * データ管理にて一覧表示に使用。
     * 項目名や項目の順番を取得
     */
    fun createFieldList(result:JSONObject): List<MutableMap<String, String?>> {
            Log.d("fun_createFieldList","start")
            val rtnMap :MutableMap<String,MutableMap<String, String?>> = mutableMapOf()
            val fieldList = result.getJSONArray("records")

            for (i in 0 until fieldList.length()){
                val valueMap : MutableMap<String,String?> = mutableMapOf()
                //JSoNオブジェクトを行ごとに取得。
                val jsonField = JSONObject(fieldList[i].toString())
                val field = jsonField.getJSONObject("Fields")
                //値の取得と格納
                valueMap.set(key = "field_col" ,value = field["col"].toString())
                valueMap.set(key = "field_name",value = field["name"].toString())
                rtnMap.set(key = i.toString() ,value = valueMap)
                Log.d("aaaa","${jsonField}")
            }
            rtnMap.forEach{
                Log.d("aaaa","${it}")
            }

            val rtnMapSort = rtnMap.values.sortedBy { it["field_col"] !!.toInt()}
            return rtnMapSort
    }

    /**
     * データ管理にて一覧表示に使用。
     * 登録したデータを取得する
     * この処理すごいもったいないからあとで何とかしましょう！！
     */
    //TODO:この処理何とかすること！！
    fun createItemList(result:JSONObject): MutableList<MutableMap<String, String?>> {
        Log.d("fun_createFieldList","start")
        val rtnMap :MutableList<MutableMap<String, String?>> = mutableListOf()
        val itemList = result.getJSONArray("records")

        for (i in 0 until itemList.length()){
            //JSoNオブジェクトを行ごとに取得。
            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val jsonField = JSONObject(itemList[i].toString())
            val item = jsonField.getJSONObject("Item")
            Log.d("ssss","${item}")
            Log.d("ssss","${itemList.length()}")
            valueMap.set(key = "item_id",value = item["item_id"].toString() )
            for(j in 1 ..150 ){
                valueMap.set(key = "fld${j}",value = item["fld${j}"].toString())
            }
            rtnMap.add(i,valueMap)
        }
        return rtnMap
    }

    fun createFormField(result:JSONObject): List<MutableMap<String, String?>> {
        val rtnMap :MutableMap<String,MutableMap<String, String?>> = mutableMapOf()
        val formFieldList = result.getJSONArray("records")

        for (i in 0  until formFieldList.length()){
            //JSoNオブジェクトを行ごとに取得。
            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val jsonFormField = JSONObject(formFieldList[i].toString())
            val formField = jsonFormField.getJSONObject("Fields")
            //値を格納
            valueMap.set(key = "name" ,value = formField["name"].toString())
            valueMap.set(key = "type",value = formField["type"].toString())
            valueMap.set(key = "unique_chk" ,value = formField["unique_chk"].toString())
            valueMap.set(key = "essential",value = formField["essential"].toString())
            valueMap.set(key = "choice" ,value = formField["name"].toString())
            valueMap.set(key = "field_col" ,value = formField["col"].toString())
            //配列に格納
            rtnMap.set(key=i.toString(),value = valueMap)

        }
        val rtnMapSort = rtnMap.values.sortedBy { it["field_col"] !!.toInt()}
        return rtnMapSort
    }
}