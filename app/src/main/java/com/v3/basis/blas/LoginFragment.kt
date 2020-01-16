package com.v3.basis.blas


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    //BLASのURLに変更してから運用すること！！
    val loginURL = "http://192.168.0.101/blas7/api/v1/auth/login/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login,container,false)
        //ログインボタン押下時の処理設定
        val loginBtn = view.findViewById<Button>(R.id.btnLogin)
        loginBtn.setOnClickListener(ItemClickListener())

        return view
    }

    private inner class ItemClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //入力されたユーザ名とパスワードを取得
            val editUserName = view?.findViewById<EditText>(R.id.userName)
            val name = editUserName?.text.toString()
            val editPassword = view?.findViewById<EditText>(R.id.Password)
            val password = editPassword?.text.toString()
            //ログ出力
            Log.d("【Login】", "UserName:${name}")
            Log.d("【Login】", "Password:${password}")
            //ログイン処理開始
            val receiver = LoginReceiver()
            receiver.execute(name,password)
        }
    }

    /***
     * ログインボタンを押したときの処理を書く
     */
    private inner class  LoginReceiver() : AsyncTask<String, String, String>(){
        override fun doInBackground(vararg params: String?): String? {
            //URL等アクセス先の設定。ここは環境ごとに変更すること！
            //ここを変更した場合、"\app\src\main\res\xml\network_security_config.xml"にURLを書くこと
            val urlstr = loginURL
            val url = URL(urlstr)
            val con = url.openConnection() as HttpURLConnection

            //POSTするデータの作成
            val postData = "name=${params[0]}&password=${params[1]}"

            //タイムアウトとメソッドの設定
            con.requestMethod = "POST"
            con.connectTimeout = 1000
            con.readTimeout = 1000

            //リクエストパラメータの設定
            con.doOutput = true
            val outStream = con.outputStream
            //リクエスト処理
            outStream.write(postData.toByteArray())
            outStream.flush()
            //エラーコードなど飛んでくるのでログに出力する
            val resCorde = con.responseCode
            Log.d("【Login】", "Log:${resCorde}")

            //リクエスト処理処理終了
            outStream.close()

            //レスポンスデータを取得
            val responseData = con.inputStream
            //レスポンスデータをJSON文字列にする
            val response =is2String(responseData)
            //オブジェクト解放
            con.disconnect()
            return response
        }

        /**
         * JSON文字列のデータを解析する
         */
        override fun onPostExecute(response: String) {
            val rootJSON = JSONObject(response)
            //JSON文字列からrecordsを取得
            val recordsJSON = rootJSON.getJSONObject("records")
            Log.d("【Login】", "Log:${recordsJSON}")
            //取得したrecordsからtokenを取得
            val token = recordsJSON.getString("token")
            Log.d("【Login】", "Log:${token}")
            //取得したトークンを共有プリファレンスデータへ保存
            //saveData("token",token)
            //val saveToken = getData("token")

            //取得したトークンを新しい画面に送信。
            val intent = Intent(activity,BulletinBoardActivity::class.java)
            intent.putExtra("token",token)
            startActivity(intent)

        }
    }
    /**
     * オブジェクトをJSON文字列に変換するメソッド
     */
    private fun is2String(stream: InputStream): String{
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream,"UTF-8"))
        var line = reader.readLine()
        if(line != null){
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

}
