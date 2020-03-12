package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.v3.basis.blas.R
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * restfulのプロジェクトAPIクラス
 * @param in crud APIへの機能を選択するパラメータ
 * @param in payload マップ形式のrestfulのrequestに必要なパラメーター
 * @param in funcSuccess 掲示板取得時にコールバックする関数
 * @param in funcError 掲示板取得失敗時にコールバックする関数
 */
open class BlasRestInformation(val crud:String = "search",
                               val payload:Map<String, String?>,
                               val funcSuccess:(JSONObject)->Unit,
                               val funcError:(Int)->Unit) : BlasRest() {

    init{
        cacheFileName = context.filesDir.toString() + "/information.json"
    }

    /**
     * 掲示板取得要求をBLASに送信する
     * @param in params 指定なし
     */
    override fun doInBackground(vararg params: String?): String? {
        //レスポンスデータを取得
        var response:String? = null

        var blasUrl = BlasRest.URL + "informations/search/"

        when(crud) {
            "search" -> {
                blasUrl = BlasRest.URL + "informations/search/"
            }
            "download" -> {
                blasUrl = BlasRest.URL + "informations/download/"
            }
        }

        try {
            response = super.getResponseData(payload,"GET", blasUrl)

        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)

            //通信エラーが発生したため、キャッシュを読み込む
            if(File(cacheFileName).exists()) {
                try {
                    response = loadJson(cacheFileName)
                } catch(e: Exception) {
                    //キャッシュの読み込み失敗
                    funcError(BlasRestErrCode.NETWORK_ERROR)
                    return response
                }
            } else {
                //キャッシュファイルがないため、エラーにする
                funcError(BlasRestErrCode.NETWORK_ERROR)
                return response
            }
        }

        return response
    }

    /**
     * 掲示板情報を取得する
     * @params in result プロジェクトのjsonコード。エラー時はNULLが入る
     * @return 成功時：funcSuccessをコールバックする。
     * 　　　　　異常時：funcErrorをコールバックする。
     */
    override fun onPostExecute(result: String?) {

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
            return
        }

        //正常時だけキャッシュに保存する
        if(errorCode == 0 && result != null) {
            //正常のときだけキャッシュにjsonファイルを保存する
            try {
                saveJson(cacheFileName, result)
                if(json != null) {
                    //コールバック
                    funcSuccess(json)
                }
            }
            catch(e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        } else {
            funcError(errorCode)
        }

    }

}