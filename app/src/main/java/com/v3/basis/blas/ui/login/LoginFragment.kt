package com.v3.basis.blas.ui.login


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
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.rest.BlasRest
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.v3.basis.blas.blasclass.rest.BlasRestAuth

/**
 * A simple [Fragment] subclass.
 * 表示・遷移などログイン画面にかかわる処理を行う。
 */
class LoginFragment : Fragment() {

    //BLASのURLに変更してから運用すること！！
    val LOGIN_URL = BlasRestAuth().LOGIN_URL
    val CONTEXT_TIME_OUT = BlasRest().CONTEXT_TIME_OUT
    val READ_TIME_OUT = BlasRest().READ_TIME_OUT


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
            Log.d("【LoginFragment】", "Login開始")
            val editUserName = view?.findViewById<EditText>(R.id.userName)
            val name = editUserName?.text.toString()
            val editPassword = view?.findViewById<EditText>(R.id.Password)
            val password = editPassword?.text.toString()
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
            val key = listOf("name","password")
            //レスポンスデータを取得
            //レスポンスデータをJSON文字列にする
            val response = BlasRest().getResponseData(params,key,"POST",LOGIN_URL)
            return response
        }

        /**
         * JSON文字列のデータを解析する
         */
        override fun onPostExecute(response: String) {
            val token = BlasRestAuth().getToken(response)
            //取得したトークンを新しい画面に送信。
            val intent = Intent(activity, TerminalActivity::class.java)
            intent.putExtra("token",token)
            Log.d("【LoginFragment】", "Login完了")
            startActivity(intent)

        }
    }
}
