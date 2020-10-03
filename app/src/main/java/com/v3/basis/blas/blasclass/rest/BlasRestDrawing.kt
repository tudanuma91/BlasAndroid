package com.v3.basis.blas.blasclass.rest
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.v3.basis.blas.blasclass.app.BlasDef
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_QUEUE_SAVE
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File


/**
 * BLASのデータにアクセスするクラス
 */
open class BlasRestDrawing(val crud:String = "index",
                           val payload:Map<String, String?>,
                           val funcSuccess:(DrawingResponse)->Unit,
                           val funcError:(Int,Int)->Unit) : BlasRest() {

    companion object {
        val TABLE_NAME = "Drawing"
    }

    var method = "GET"
    var aplCode:Int = 0

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params drawing_id
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        var json:JSONObject? = null

        var blasUrl = BlasRest.URL + "drawing_images"

        when(crud) {
            "index"->{
                method = "GET"
                blasUrl = BlasRest.URL + "drawing_images"
            }
            "view"->{
                method = "GET"
                blasUrl = BlasRest.URL + "drawing_images/${payload["drawing_id"]}"
            }
        }

        try {
            response = super.getResponseData(payload,method, blasUrl)
        }
        catch(e: Exception) {
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
            val response = Gson().fromJson(json.toString(), DrawingResponse::class.java)
            funcSuccess(response)
        } else {
            funcError(errorCode , aplCode)
        }
    }

    /**
     * Base64を画像(バイナリに変換する)
     */
    public fun decodeBase64(base64_data:String): ByteArray{
        val byteCode = Base64.decode(base64_data, Base64.DEFAULT)
        return byteCode
    }

    /**
     * 画像をBase64に変換する
     */
    public fun encodeBase64(bin_data:ByteArray): String {
        val strCode = Base64.encode(bin_data, Base64.DEFAULT).toString()
        return strCode
    }
}

class DrawingResponse(
    val error_code: Int, // 0
    val message: String, // None
    val records: List<DrawingRecord>) {

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
