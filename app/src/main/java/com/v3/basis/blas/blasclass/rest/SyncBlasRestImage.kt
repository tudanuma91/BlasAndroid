package com.v3.basis.blas.blasclass.rest
import android.util.Base64
import android.util.Log
import android.widget.Toast
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
open class SyncBlasRestImage() : SyncBlasRest() {

    companion object {
        val TABLE_NAME = "Image"
    }

    var method = "GET"
    var aplCode:Int = 0

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    fun upload(payload:Map<String, String?>): JSONObject? {
        var response:String? = null
        var json:JSONObject? = null
        var blasUrl = BlasRest.URL + "images/download/"
        method = "POST"
        blasUrl = BlasRest.URL + "images/upload/"

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
