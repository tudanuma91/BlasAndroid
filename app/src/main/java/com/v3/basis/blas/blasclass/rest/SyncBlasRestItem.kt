package com.v3.basis.blas.blasclass.rest

import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_QUEUE_SAVE
import com.v3.basis.blas.blasclass.log.BlasLog
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
    fun upload(payload:Map<String, String?>): JSONObject? {
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
            //Log.d("blas-log", e.message)
            BlasLog.trace("E", "通信エラーが発生しました", e)
        }

        return json
    }

}