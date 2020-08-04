package com.v3.basis.blas.blasclass.rest

import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_QUEUE_SAVE
import com.v3.basis.blas.ui.item.item_edit.ItemEditFragment.Companion.formDefaultValueList
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File


/**
 * BLASのデータにアクセスするクラス
 */
open class SyncBlasRestItem() : SyncBlasRest() {

    companion object {
        val TABLE_NAME = "Item"
    }

    var method = "GET"
    var aplCode:Int = 0

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    fun create_sync(payload:Map<String, String?>): JSONObject? {
        Log.d("SyncBlasRestItem","post() start")
        var response:String? = null
        var blasUrl = BlasRest.URL + "items/search/"
        var json:JSONObject? = null
        method = "POST"
        blasUrl = BlasRest.URL + "items/create_sync/"

        try {
            response = super.getResponseData(payload,method, blasUrl)
            json = JSONObject(response)
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)
        }

        return json
    }




    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in payload 画面からの入力
     * @param in params キャッシュファイルから取得したデータ
     */
    fun dupliCheck(payload : Map<String, String?>, response : String?) : MutableList<String> {

        var idex:Int
        val responseJson = JSONObject(response)
        val resultList: MutableList<String> = mutableListOf()
        var check:JSONObject
        var fldStr:String

        if (responseJson.has("unique_chk")) {
            check = responseJson.getJSONObject("unique_chk")
        }else{
            return resultList
        }

        for (idex in 1 until check.length() + 1){

            if (check.has("fld${idex}")) {
                fldStr = check["fld${idex}"].toString()
            }else{
                continue
            }

            val valLIst = JSONArray(fldStr)

            for ((payKey, payValue) in payload) {

                if ((method == "PUT") and (formDefaultValueList.size != 0)){
                    val beforeVal = formDefaultValueList[0]?.get("fld${idex}")
                    // 値に変更がない場合は、判定対象としない
                    if (payValue == beforeVal) {
                        continue
                    }
                }

                for (j in 0 until valLIst.length()) {
                    if(payKey == "fld${idex}"){
                        if(payValue == valLIst[j].toString()){
                            resultList.add(payKey)
                        }
                    }else{
                        break
                    }
                }
            }
        }

        return resultList

    }

}