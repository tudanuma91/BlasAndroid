package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * restfulのユーザーAPIクラス
 * @param in payload マップ形式のrestfulのrequestに必要なパラメーター
 * @param in funcSuccess ユーザー取得時にコールバックする関数
 * @param in funcError ユーザー取得失敗時にコールバックする関数
 */
open class BlasRestUser(val payload:Map<String, String?>,
                        val funcSuccess:(JSONObject)->Unit,
                        val funcError:(Int,Int)->Unit) : BlasRest() {

    private val SEARCH_USER_URL = BlasRest.URL + "users/search/"
    init{
        cacheFileName = context.cacheDir.toString() + "/user.json"
    }

    /**
     * ユーザー一覧取得要求をBLASに送信する
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        //レスポンスデータを取得
        //レスポンスデータをJSON文字列にする
        var response:String? = null
        try {
            response = super.getResponseData(payload,"GET", SEARCH_USER_URL)

        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            //通信エラーが発生したため、キャッシュを読み込む
            if(File(cacheFileName).exists()) {
                try {
                    response = loadJson(cacheFileName)
                } catch(e: Exception) {
                    //キャッシュの読み込み失敗
                    funcError(BlasRestErrCode.FILE_READ_ERROR,APL_OK)
                }
            } else {
                //キャッシュファイルがないため、エラーにする
                funcError(BlasRestErrCode.NETWORK_ERROR,APL_OK)
            }
        }

        return response
    }

    /**
     * ユーザー一覧を取得する
     * @params in result プロジェクトのjsonコード。エラー時はNULLが入る
     * @return 成功時：funcSuccessをコールバックする。
     * 　　　　　異常時：funcErrorをコールバックする。
     */
    override fun onPostExecute(result: String?) {

        var records: JSONArray? = null

        if(result == null) {
            funcError(BlasRestErrCode.NETWORK_ERROR,APL_OK)
            return
        }

        super.onPostExecute(result)

        //BLASから取得したデータをjson形式に変換する
        var json:JSONObject? = null
        var errorCode:Int = 0

        try {
            json = JSONObject(result)
            //エラーコード取得
            errorCode = json.getInt("error_code")
            records = json.getJSONArray("records")
            if(json.has("records")) {
                records = json.getJSONArray("records")
            }

        } catch (e: JSONException){
            //JSONの展開に失敗
            Toast.makeText(context, "データ取得失敗", Toast.LENGTH_LONG).show()
        }

        //正常時だけキャッシュに保存する
        if(errorCode == 0) {
            //正常のときだけキャッシュにjsonファイルを保存する
            try {
                if(records != null) {
                    saveJson(cacheFileName, result)
                }

                if(json != null) {
                    funcSuccess(json)
                }
            }
            catch(e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        } else {
            funcError(errorCode,APL_OK)
        }

    }

}