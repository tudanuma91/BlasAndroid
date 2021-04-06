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
     * 画像をサーバーにアップロードする
     * @param in params 指定なし
     */
    fun upload(payload:Map<String, String?>): JSONObject? {
        var response:String? = null
        var json:JSONObject? = null
        method = "POST"
        val blasUrl = BlasRest.URL + "images/upload/"

        try {
            response = super.getResponseData(payload,method, blasUrl)
            json = JSONObject(response)
        }
        catch(e: Exception) {
            Log.d("blas-log", "通信エラー")
        }
        return json
    }

    fun download(payload:Map<String, String?>): JSONObject? {
        var response:String? = null
        var json:JSONObject? = null
        method = "GET"
        val blasUrl = BlasRest.URL + "images/download/"

        try {
            response = super.getResponseData(payload,method, blasUrl)
            json = JSONObject(response)
        }
        catch(e: Exception) {
            Log.d("blas-log", "通信エラー")
        }
        return json
    }


    fun download230(payload:Map<String, String?>): JSONObject? {
        var response:String? = null
        var json:JSONObject? = null
        method = "GET"
        val blasUrl = BlasRest.URL + "images/download230/"

        try {
            response = super.getResponseData(payload, method, blasUrl)
            json = JSONObject(response)
        }
        catch(e: Exception) {
            Log.d("blas-log", "通信エラー")
        }
        return json
    }

    fun getUrl(payload:Map<String, String?>): JSONObject? {
        var response:String? = null
        var json:JSONObject? = null
        method = "GET"
        val blasUrl = BlasRest.URL + "images/url/"

        try {
            response = super.getResponseData(payload,method, blasUrl)
            json = JSONObject(response)
        }
        catch(e: Exception) {
            Log.d("blas-log", "通信エラー")
        }
        return json
    }


    fun delete(payload:Map<String, String?>): JSONObject? {
        var response:String? = null
        var json:JSONObject? = null
        method = "DELETE"
        val blasUrl = BlasRest.URL + "images/delete/"

        try {
            response = super.getResponseData(payload,method, blasUrl)
            json = JSONObject(response)
        }
        catch(e: Exception) {
            Log.d("blas-log", "通信エラー")
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
