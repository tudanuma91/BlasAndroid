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
            val jsonStr = fieldList[i].toString()
            val jsonField = JSONObject(jsonStr)
            val field = jsonField.getJSONObject("Field")
            //値の取得と格納
            valueMap.set(key = "field_col" ,value = field["col"].toString())
            valueMap.set(key = "field_name",value = field["name"].toString())
            rtnMap.set(key = i.toString() ,value = valueMap)

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

    fun createItemList(resultMap:MutableMap<String,JSONObject>,colMax:Int): MutableList<MutableMap<String, String?>> {
        Log.d("fun_createFieldList","start")
        val result = resultMap["1"]
        val rtnMap :MutableList<MutableMap<String, String?>> = mutableListOf()
        val itemList = result!!.getJSONArray("records")


        for (i in 0 until itemList.length()){
            //JSoNオブジェクトを行ごとに取得。
            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val jsonField = JSONObject(itemList[i].toString())
            val item = jsonField.getJSONObject("Item")
            valueMap.set(key = "item_id",value = item["item_id"].toString() )
            for(j in 1 ..colMax ){
                if (item.has("fld${j}")) {
                    valueMap.set(key = "fld${j}", value = item["fld${j}"].toString())
                }
            }
            rtnMap.add(i,valueMap)
        }
        return rtnMap
    }

    /**
     * fieldの設定を取得する処理
     */
    fun createFormField(result:JSONObject): MutableMap<String, MutableMap<String, String?>> {
        val rtnMap :MutableMap<String,MutableMap<String, String?>> = mutableMapOf()
        val formFieldList = result.getJSONArray("records")

        for (i in 0  until formFieldList.length()){
            //JSoNオブジェクトを行ごとに取得。
            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val jsonFormField = JSONObject(formFieldList[i].toString())
            val formField = jsonFormField.getJSONObject("Field")
            //値を格納
            valueMap.set(key = "name" ,value = formField["name"].toString())
            valueMap.set(key = "type",value = formField["type"].toString())
            valueMap.set(key = "unique_chk" ,value = formField["unique_chk"].toString())
            valueMap.set(key = "essential",value = formField["essential"].toString())
            valueMap.set(key = "choice" ,value = formField["choice"].toString())
            valueMap.set(key = "field_col" ,value = formField["col"].toString())
            //配列に格納
            rtnMap.set(key=i.toString(),value = valueMap)

        }
        return rtnMap
    }

    /**
     * 初期値を設定する処理
     */
    fun createDefaultValueList(resultMap:MutableMap<String,JSONObject>,colMax:Int,item_id:String?): MutableList<MutableMap<String, String?>> {
        val result = resultMap["1"]
        Log.d("確認用のログ","${result}")
        val rtnMap :MutableList<MutableMap<String, String?>> = mutableListOf()
        val itemList = result!!.getJSONArray("records")

        for (i in 0 until itemList.length()){
            //JSoNオブジェクトを行ごとに取得。
            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val jsonField = JSONObject(itemList[i].toString())
            val item = jsonField.getJSONObject("Item")
            Log.d("確認用のログ","中身=>${item["item_id"]}")
            if(item["item_id"] == item_id.toString()){
                valueMap.set(key = "item_id",value = item["item_id"].toString() )
                for(j in 1 ..colMax ){
                    valueMap.set(key = "fld${j}",value = item["fld${j}"].toString())
                }
                rtnMap.add(0,valueMap)
                break
            }
        }

        return rtnMap
    }


    fun createInformationList(result: JSONObject): MutableMap<String, MutableMap<String, String?>> {

        val rtnMap :MutableMap<String,MutableMap<String, String?>> = mutableMapOf()
        val infoList = result.getJSONArray("records")

        for (i in 0 until infoList.length()){
            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val jsonInfo = JSONObject(infoList[i].toString())
            val information = jsonInfo.getJSONObject("Information")
            valueMap.set(key = "information_id" ,value = information["information_id"].toString())
            valueMap.set(key = "body" ,value = information["body"].toString())
            valueMap.set(key = "file1" ,value = information["file1"].toString())
            valueMap.set(key = "file2" ,value = information["file2"].toString())
            valueMap.set(key = "file3" ,value = information["file3"].toString())
            rtnMap.set("${i}",valueMap)
        }

        return rtnMap
    }

    /**
     * userの情報を取得する処理
     */
    fun createUserList(result:JSONObject):  MutableMap<String, MutableMap<String, String?>> {

        val rtnMap :MutableMap<String,MutableMap<String, String?>> = mutableMapOf()
        val userList = result.getJSONArray("records")

        for (i in 0  until userList.length()){

            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val userObj = JSONObject(userList[i].toString())
            val userInfo = userObj.getJSONObject("User")
            //値を格納
            valueMap.set(key = "user_id", value = userInfo["user_id"].toString())
            valueMap.set(key = "username", value = userInfo["username"].toString())
            valueMap.set(key = "mail", value = userInfo["mail"].toString())
            valueMap.set(key = "org_id", value = userInfo["org_id"].toString())
            valueMap.set(key = "name", value = userInfo["name"].toString())
            valueMap.set(key = "group_id", value = userInfo["group_id"].toString())
            rtnMap.set("${i}",valueMap)

        }

        return rtnMap
    }

}

