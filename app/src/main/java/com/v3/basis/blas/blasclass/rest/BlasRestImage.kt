package com.v3.basis.blas.blasclass.rest
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File


/**
 * BLASのデータにアクセスするクラス
 */
open class BlasRestImage(val crud:String = "download",
                         val payload:Map<String, String?>,
                         val funcSuccess:(JSONObject)->Unit,
                         val funcError:(Int)->Unit) : BlasRest() {

    companion object {
        val TABLE_NAME = "Image"
    }

    init{
        cacheFileName = context.filesDir.toString() +  "/image_" + payload["item_id"] + ".json"
    }
    var method = "GET"

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null

        var blasUrl = BlasRest.URL + "images/download/"

        when(crud) {
            "download"->{
                method = "GET"
                blasUrl = BlasRest.URL + "images/download/"
            }
            "upload"->{
                method = "POST"
                blasUrl = BlasRest.URL + "images/create/"
            }
            "delete"->{
                method = "DELETE"
                blasUrl = BlasRest.URL + "images/delete/"
            }
        }

        try {
            response = super.getResponseData(payload,method, blasUrl)
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            if(method == "GET") {

                //通信エラーが発生したため、キャッシュを読み込む
                if (File(cacheFileName).exists()) {
                    try {
                        response = loadJson(cacheFileName)
                    } catch (e: Exception) {
                        //キャッシュの読み込み失敗
                        funcError(BlasRestErrCode.FILE_READ_ERROR)
                    }
                } else {
                    //キャッシュファイルがないため、エラーにする
                    funcError(BlasRestErrCode.NETWORK_ERROR)
                }
            }else if (method == "POST"){

                // 失敗した場合、キュー処理を呼び出す
                super.reqDataSave(payload,"GET",blasUrl,funcSuccess,funcError,"Images")

            }
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
            funcError(BlasRestErrCode.NETWORK_ERROR)
            return
        }

        super.onPostExecute(result)

        //BLASから取得したデータをjson形式に変換する
        var json:JSONObject? = null
        var errorCode:Int
        var records: JSONArray? = null

        try {
            json = JSONObject(result)
            //エラーコード取得
            errorCode = json.getInt("error_code")

        } catch (e: JSONException){
            //JSONの展開に失敗
            Toast.makeText(context, "データ取得失敗", Toast.LENGTH_LONG).show()
            return
        }

        if(method == "GET" && errorCode == 0) {

            records = json.getJSONArray("records")
            if(records != null){
                saveJson(cacheFileName, result)
            }
        }

        if(json == null) {
            funcError(BlasRestErrCode.JSON_PARSE_ERROR)
        }
        else if(errorCode == 0) {

            funcSuccess(json)
        }
        else {
            funcError(errorCode)
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