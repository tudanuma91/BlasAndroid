package com.v3.basis.blas.blasclass.helper

import android.util.Log
import com.google.gson.JsonObject
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

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
        Log.d("fun_createFieldList1","start")
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
            valueMap.set(key = "type",value = field["type"].toString())
            rtnMap.set(key = i.toString() ,value = valueMap)

        }
        val rtnMapSort = rtnMap.values.sortedBy { it["field_col"] !!.toInt()}

        return rtnMapSort
    }

    // TODO:これ必要？？
    fun createFieldList2(fields:List<LdbFieldRecord>): List<MutableMap<String, String?>> {
        Log.d("fun_createFieldList2","start")
        val rtnMap :MutableMap<String,MutableMap<String, String?>> = mutableMapOf()

        var cnt = 0
        fields.forEach{
            val valueMap : MutableMap<String,String?> = mutableMapOf()

            valueMap.set(key = "field_col",value = it.col.toString())
            valueMap.set(key = "field_name",value = it.name)
            valueMap.set(key = "type",value = it.type.toString())

            rtnMap.set(key = cnt.toString(),value = valueMap)
            cnt += 1
        }
        val rtnMapSort = rtnMap.values.sortedBy { it["field_col"] !!.toInt()}

        return rtnMapSort
    }


    /**
     * データ管理にて、一覧表示に使用。
     * jsonを○○件ずつパースして取得する処理
     */
    fun createSeparateItemList(resultMap:JSONArray?,
                               colMax:Int,
                               parseStartNum:Int,
                               parseFinNum:Int): MutableList<MutableMap<String, String?>> {

        val rtnMap :MutableList<MutableMap<String, String?>> = mutableListOf()

        Log.d("jsonParse","resultMap => ${resultMap!!.length()}")

        //スタートとゴールのidxが同じ時の処理
        if(parseFinNum == parseStartNum){
            if (resultMap != null) {
                val valueMap: MutableMap<String, String?> = mutableMapOf()
                val jsonField = JSONObject(resultMap[parseFinNum].toString())

                val item = jsonField.getJSONObject("Item")
                val endFlg = item["end_flg"].toString()
                valueMap.set(key = "item_id", value = item["item_id"].toString())
                valueMap.set(key = "endFlg", value = endFlg)

                for (j in 1..colMax) {
                    val value = item["fld${j}"].toString()
                    //Nullの時true。それ以外でfalseが入る
                    val chk = item.isNull("fld${j}")
                    val inputValue = if (chk) "" else value
                    valueMap.set(key = "fld${j}", value = inputValue)
                }

                rtnMap.add(parseStartNum, valueMap)
            }
        }else {
            //スタートとゴールの値が異なる場合
            if (resultMap != null) {
                try {
                    var count = 0
                    for (idx in parseStartNum until parseFinNum) {
                        Log.d("jsonParse", "idx => ${idx}")
                        val valueMap: MutableMap<String, String?> = mutableMapOf()
                        val jsonField = JSONObject(resultMap.get(idx).toString())
                        val item = jsonField.getJSONObject("Item")
                        val endFlg = item["end_flg"].toString()
                        valueMap.set(key = "item_id", value = item["item_id"].toString())
                        valueMap.set(key = "endFlg", value = endFlg)

                        for (j in 1..colMax) {
                            val value = item["fld${j}"].toString()
                            //Nullの時true。それ以外でfalseが入る
                            val chk = item.isNull("fld${j}")
                            val inputValue = if (chk) "" else value
                            valueMap.set(key = "fld${j}", value = inputValue)
                        }

                        Log.d("jsonParse", "ここまで生きている")
                        rtnMap.add(count, valueMap)
                        count += 1

                    }
                }catch (e:Exception){
                    Log.d("jsonParse","エラー=>${e}")
                }
            }
        }
       // Log.d("jsonParse","rtnMap => ${rtnMap}")
        return rtnMap
    }


    fun createJsonArray(resultMap:JSONObject?): JSONArray? {
        var rtnList:JSONArray? = null
        if(resultMap != null) {
            rtnList = resultMap.getJSONArray("records")
        }
        return rtnList
    }


    /**
     * データ管理にて一覧表示に使用。
     * 登録したデータを取得する
     * この処理すごいもったいないからあとで何とかしましょう！！
     */
    //TODO:この処理何とかすること！！

    fun createItemList(resultMap:MutableMap<String,JSONObject>,colMax:Int): MutableList<MutableMap<String, String?>> {
        Log.d("fun_createItemList","start")
        val result = resultMap["1"]
        val rtnMap :MutableList<MutableMap<String, String?>> = mutableListOf()
        val itemList = result!!.getJSONArray("records")
        Log.d("配列の取得","itemList =>${itemList}")

        //for (i in 0 until 100件ずつ行う処理の続き){
        for (i in 0 until itemList.length()){
            //JSoNオブジェクトを行ごとに取得。
            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val jsonField = JSONObject(itemList[i].toString())

            val item = jsonField.getJSONObject("Item")
            val endFlg = item["end_flg"].toString()
            valueMap.set(key = "item_id",value = item["item_id"].toString() )
            valueMap.set(key = "endFlg", value = endFlg)

            for(j in 1 ..colMax ) {
                val value = item["fld${j}"].toString()
                //Nullの時true。それ以外でfalseが入る
                val chk = item.isNull("fld${j}")
                val inputValue = if(chk) "" else value
                valueMap.set(key = "fld${j}", value = inputValue)
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
            Log.d("デバック用ログ","RestHelper_値の取得${formField}")
            valueMap.set(key = "name" ,value = formField["name"].toString())
            valueMap.set(key = "type",value = formField["type"].toString())
            valueMap.set(key = "unique_chk" ,value = formField["unique_chk"].toString())
            valueMap.set(key = "essential",value = formField["essential"].toString())
            valueMap.set(key = "parent_field_id",value = formField["parent_field_id"].toString())
            valueMap.set(key = "choice" ,value = formField["choice"].toString())
            valueMap.set(key = "field_col" ,value = formField["col"].toString())
            valueMap.set(key = "field_id",value = formField["field_id"].toString())
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
        val rtnMap :MutableList<MutableMap<String, String?>> = mutableListOf()
        val itemList = result!!.getJSONArray("records")

        for (i in 0 until itemList.length()){
            //JSoNオブジェクトを行ごとに取得。
            val valueMap : MutableMap<String,String?> = mutableMapOf()
            val jsonField = JSONObject(itemList[i].toString())
            val item = jsonField.getJSONObject("Item")
            if(item["item_id"] == item_id.toString()){
                valueMap.set(key = "item_id",value = item["item_id"].toString() )
                for(j in 1 ..colMax ){
                    val value = item["fld${j}"].toString()
                    //Nullの時true。それ以外でfalseが入る
                    val chk = item.isNull("fld${j}")
                    val inputValue = if(chk) "" else value
                    valueMap.set(key = "fld${j}", value = inputValue)
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

    fun isBlank(text:String?):Boolean {
        var bRet:Boolean = false
        if(text != null && text != "null" && text != "") {
            bRet = true
        }
        return bRet
    }

    fun createCheckValue(value: String?) :String?{
        //		この形式で来る
        //		{\"value\":\"aaa\",\"memo\":\"aaaaa\"}
        var newValue = ""
        val json = value?.replace("\\", "")
        if (json != null && json.isNotBlank()) {
            try {
                val obj = JSONObject(json)
                val text = obj.get("value")
                val memo = obj.get("memo")
                newValue = text.toString()
                if (this.isBlank(memo.toString())) {
                    newValue += "(備考)" + memo
                }
            }
            catch(e:Exception) {
                BlasLog.trace("E", "${json}のパースに失敗しました")
                newValue = json.toString()
            }
        }

        return newValue
    }

    /*
    fun createCheckValue(value:String?): String {
        var newValue = ""
        if(value != null && value != "") {
            val tmp = if (value.contains("\\")) {
                value.replace("\\", "")
            } else value
            val jsonValue = JSONObject(tmp)
            newValue += "${jsonValue["value"]}"
            if (jsonValue["memo"].toString() != "" && !jsonValue.isNull("memo") ) {
                newValue += "(備考)${jsonValue["memo"]}"
            }
        }
        return newValue
    }
     */


    fun createCheckValueText(value: String?,type:String):String{
        var newValue = ""
        if(value != null) {
            val jsonValue = JSONObject(value)
            if (jsonValue[type].toString() != "" && !jsonValue.isNull(type)) {
                newValue = jsonValue[type].toString()
            }
        }
        return newValue
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

    fun createJsonFix(resultMap:JSONArray,
                              valueMap:MutableMap<Int, MutableMap<String, String?>>,
                              parseStartNum:Int,
                              parseFinNum:Int): MutableMap<Int, MutableMap<String, String?>> {

        //resultMap.Sort

        for (i in parseStartNum until parseFinNum) {
            val fields = JSONObject(resultMap[i].toString())
            val fixture = fields.getJSONObject("Fixture")
            val fixtureId = fixture.getInt("fixture_id")


            valueMap[fixtureId] = mutableMapOf(
                "serial_number" to fixture.getString("serial_number"),
                "status" to fixture.getString("status"),
                "fix_user" to fields.getJSONObject("FixUser").getString("name"),
                "takeout_user" to fields.getJSONObject("TakeOutUser").getString("name"),
                "rtn_user" to fields.getJSONObject("RtnUser").getString("name"),
                "item_user" to fields.getJSONObject("ItemUser").getString("name"),
                "fix_org" to fields.getJSONObject("FixOrg").getString("name"),
                "takeout_org" to fields.getJSONObject("TakeOutOrg").getString("name"),
                "rtn_org" to fields.getJSONObject("RtnOrg").getString("name"),
                "item_org" to fields.getJSONObject("ItemOrg").getString("name"),
                "fix_date" to fixture.getString("fix_date"),
                "takeout_date" to fixture.getString("takeout_date"),
                "rtn_date" to fixture.getString("rtn_date"),
                "item_date" to fixture.getString("item_date")
            )
        }

        return valueMap
    }


}

