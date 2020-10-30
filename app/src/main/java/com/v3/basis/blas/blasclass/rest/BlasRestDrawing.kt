package com.v3.basis.blas.blasclass.rest
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.InputStreamReader


/**
 * BLASのデータにアクセスするクラス
 */
open class BlasRestDrawing(
    val crud: String = "index",
    val payload: Map<String, String?>,
    val funcSuccess: (DrawingResponse) -> Unit,
    val funcError: (Int, Int) -> Unit
) : BlasRest() {

    var method = "GET"
    var aplCode:Int = 0

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param params drawing_id
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null

        var blasUrl = URL + "drawing_images"

        when(crud) {
            "index" -> {
                method = "GET"
                blasUrl = URL + "drawing_images"
            }
            "view" -> {
                method = "GET"
                blasUrl = URL + "drawing_images/${payload["drawing_id"]}"
            }
        }

        try {
            response = super.getResponseData(payload, method, blasUrl)
        }
        catch (e: Exception) {
            Log.d("blas-log", e.message)
            funcError(BlasRestErrCode.NETWORK_ERROR, APL_OK)
        }
        return response
    }


    /**
     * プロジェクトに設定されているフィールドの情報を取得したときにコールされる
     * 取得成功時、orgsSearchSuccessをコールする
     * 失敗時、orgsSearchErrorをコールする
     * @param なし
     *
     */
    override fun onPostExecute(result: String?) {
        if(result == null) {
            funcError(BlasRestErrCode.NETWORK_ERROR, aplCode)
            return
        }

        super.onPostExecute(result)

        //BLASから取得したデータをjson形式に変換する
        var json:JSONObject? = null
        var errorCode:Int

        try {
            json = JSONObject(result)
            //エラーコード取得
            errorCode = json.getInt("error_code")

        } catch (e: JSONException){
            //JSONの展開に失敗
            Toast.makeText(context, "データ取得失敗", Toast.LENGTH_LONG).show()
            funcError(BlasRestErrCode.JSON_PARSE_ERROR, aplCode)
            return
        }

        if(method == "GET" && errorCode == 0) {
            var records = json.getJSONArray("records")
            if(records != null){
                //saveJson(cacheFileName, result)
            }
        }

        if(json == null) {
            funcError(BlasRestErrCode.JSON_PARSE_ERROR, aplCode)
        } else if(errorCode == 0) {
            ByteArrayInputStream(result.toByteArray()).use {
                val reader = InputStreamReader(it)
                val response = Gson().fromJson(reader, DrawingResponse::class.java)
                funcSuccess(response)
            }
        } else {
            funcError(errorCode, aplCode)
        }
    }
}

class DrawingResponse(
    val error_code: Int, // 0
    val message: String, // None
    val records: List<DrawingRecord>
) {

    inner class DrawingRecord(
        val Drawing: DrawingObject
    )

    inner class DrawingObject(
        val drawing_id: String,
        val project_id: String,
        val name: String,
        val filename: String,
        val image: String
    )
}
