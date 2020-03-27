package com.v3.basis.blas.blasclass.app

import android.util.Log
import com.v3.basis.blas.blasclass.rest.RestfulRtn
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class BlasCom {

}

    /**
     * オブジェクトをJSON文字列に変換するメソッド
     * [引数]
     * stream(オブジェクト) :　restful通信にて取得したデータ。
     *
     * [返り値]
     * sb.toString(文字列) : streamを文字列にして返す。
     * */
    fun Is2String(stream: InputStream): String{
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream,"UTF-8"))
        Log.d("[rest/BlasRest]","{$reader}")
        var line = reader.readLine()
        if(line != null){
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

    /**
     * cakePHPから返却されたデータをandroidで使用しやすい形式に変換する。
     * @param jsonRecord 文字列形式のjson
     * @param tableName cakePHPのテーブル名
     * @return RestfulRtnクラス(データクラス)
     */
    public fun cakeToAndroid(jsonRecord:String, tableName:String): RestfulRtn {
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

    /**
     * 検索を行う関数
     * @param cond 検索条件のmap(key:項目名 value:検索ワード)
     * @param search 検索する対象
     * @return result 検索結果
     */
    public fun searchAndroid(cond:MutableMap<String, String?>, search:MutableList<MutableMap<String, String?>>): MutableList<MutableMap<String, String?>> {
        //TODO:検索の条件をしっかり把握すること(例：大文字・小文字・半角・全角)
        //大文字小文字は別文字として判別している。

        val result = search.toMutableList()
        val chkList :MutableList<Int> = mutableListOf()

        for ((condKey, condValue) in cond) {
            //検索条件がフリーワードかつ文字が入力されているとき
            if(condKey =="freeWord" && condValue!=""){
                Log.d("検索処理","分岐完了")
                for (idx in 0..result.size - 1) {
                    var removeFlg = false
                    //ここエラー発生した。
                    // 予想だけどfor文回しているときに配列操作するとsizeが違うってエラーあり。
                    for ((resKey, resValue) in result[idx]) {
                        //item_idは検索に含まない
                        if(resKey != "item_id"){
                            val search = Regex(condValue.toString())
                            //検索ワードと値が部分一致した場合、flgをtrueにする。
                            if(search.containsMatchIn(resValue.toString())){
                                removeFlg = true
                            }
                        }
                    }
                    if(!removeFlg){
                        //検索結果、一致しなかったから、このレコードを除外たレコードを返す
                        chkList.add(idx)
                    }
                }

            }

        }
        //降順に並び替え。これしないとエラー。
        chkList.sortBy{ it * -1 }
        chkList.forEach{
            //削除処理
            result.removeAt(it)
        }
        return result
    }



