package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class BlasRestUtil {

    /**
     * cakePHPから返却されたデータをandroidで使用しやすい形式に変換する。
     * @param jsonRecord 文字列形式のjson
     * @param tableName cakePHPのテーブル名
     * @return RestfulRtnクラス(データクラス)
     */
     public fun cakeToAndroid(jsonRecord:String, tableName:String): RestfulRtn{
        //返却用エラーコード
        var errorCode = 0
        //返却用メッセージ
        var message = ""
        //返却用リスト
        var recordList:MutableList<MutableMap<String, String?>>? = mutableListOf<MutableMap<String, String?>>()

        try {
            val root = JSONObject(jsonRecord)
            //エラーコード取得
            errorCode = root.getInt("error_code")
            //メッセージ取得
            message = root.getString("message")

            if(errorCode == 0) {
                //正常時だけレコードがあるため、取得する
                val records = root.getJSONArray("records")

                for (i in 0 until records.length()) {

                    var fields = JSONObject(records[i].toString())

                    for (j in 0 until fields.length()) {
                        var data = JSONObject(fields[tableName].toString())  //指定されたテーブルを取得する
                        var recordMap = mutableMapOf<String, String?>()
                        for (k in data.keys()) {
                            recordMap[k] = data[k].toString()
                        }
                        if(recordList != null) {
                            recordList.add(recordMap)
                        }
                    }
                }
            }
            else {
                recordList = null
            }
        }
        catch(e: JSONException) {
            Log.d("konishi", e.message)
            recordList = null
        }
        return RestfulRtn(errorCode, message, recordList)
    }


    fun convProjectData(json:JSONObject):MutableMap<String, Int> {
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