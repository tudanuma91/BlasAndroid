package com.v3.basis.blas.blasclass.rest
import android.util.Base64
import android.util.Log


/**
 * BLASのデータにアクセスするクラス
 */
open class BlasRestImage(val crud:String = "download",
                        val payload:Map<String, String?>,
                        val funcSuccess:(MutableList<MutableMap<String, String?>>?)->Unit,
                        val funcError:(Int)->Unit) : BlasRest() {

    companion object {
        val TABLE_NAME = "Image"
    }

    /**
     * プロジェクトに設定されているフィールドの情報取得要求を行う
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        var response:String? = null
        var method = "GET"
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

        val rtn:RestfulRtn = cakeToAndroid(result, TABLE_NAME)
        if(rtn == null) {
            funcError(BlasRestErrCode.JSON_PARSE_ERROR)
        }
        else if(rtn.errorCode == 0) {
            funcSuccess(rtn.records)
        }
        else {
            funcError(rtn.errorCode)
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