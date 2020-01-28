package com.v3.basis.blas.ui.login


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.rest.BlasRestAuth
import com.v3.basis.blas.blasclass.config.Params
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode

/**
 * A simple [Fragment] subclass.
 * ログイン画面にかかわる処理を行う。
 * @param なし
 */
class LoginFragment : Fragment() {

    /**
     * フラグメントにログインビューをインスタンス化させるために呼び出されます。
     * @param inflater フラグメント内のビューを拡張させるために使用する
     * @param container 親ビュー
     * @param savedInstanceState 以前に保存された状態から再構築される
     * @return フラグメントのUIビューを返す
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login,container,false)
        val loginBtn = view.findViewById<Button>(R.id.btnLogin)

        /**
         * ログインボタン押下時、BLASに対してログイン要求を非同期で行う。
         * 成功時はloginSuccessメソッドを、失敗時はloginErrorメソッドをコールバックする。
         */
        loginBtn.setOnClickListener{
            Log.d("【LoginFragment】", "Login開始")
            /* パラメータ―取得 */
            val username = view?.findViewById<EditText>(R.id.userName)?.text.toString()
            val password = view?.findViewById<EditText>(R.id.Password)?.text.toString()

            /* パラメータ―チェック */
            if(validation(username, password)) {
                /* ログインを非同期で実行 */
                var payload = mapOf("name" to username, "password" to password)
                    BlasRestAuth(payload, ::loginSuccess, ::loginError).execute()
            }
        }

        return view
    }

    /**
     * ユーザ名とパスワードの値をチェックする。
     * @param username ユーザ名
     * @param password パスワード
     * @return 正常時true, 異常時falseを返す。
     */
    private fun validation(username:String, password:String):Boolean {
        var ret = true

        if(username.isEmpty()) {
            /* ユーザ名、またはパスワードがnullの場合はエラー */
            Toast.makeText(getActivity(), R.string.username_null, Toast.LENGTH_LONG).show()
            ret = false
        }

        if(password.isEmpty()) {
            Toast.makeText(getActivity(), R.string.password_null, Toast.LENGTH_LONG).show()
            ret = false
        }

        if(username.length > Params.USER_NAME_MAX_LEN) {
            /* ユーザ名が64文字より長い場合はエラー */
            Toast.makeText(getActivity(), R.string.user_name_too_long, Toast.LENGTH_LONG).show()
            ret = false
        }
        if(password.length > Params.PASSWORD_MAX_LEN) {
            /* パスワードが64文字より長い場合はエラー */
            Toast.makeText(getActivity(), R.string.password_too_long, Toast.LENGTH_LONG).show()
            ret = false
        }

        return ret
    }


    /** ログインに成功したときにコールバックされ、
     * 掲示板の画面をキックする
     * @param in token ログインに成功したときのトークン
     */
    private fun loginSuccess(token:String) {
        Log.d("BLAS", "Login成功")
        val intent = Intent(activity, TerminalActivity::class.java)
        intent.putExtra("token",token)
        startActivity(intent)
    }


    /**
     * ログインに失敗した場合にコールバックされる
     * @param in error_code ログイン失敗時のエラーコード
     * @param in message ログインに失敗したときのメッセージ
     * @return なし
     */
    private fun loginError(error_code:Int) {
        var message:String? = null

        when(error_code) {
            BlasRestErrCode.NETWORK_ERROR->{
                //サーバと通信できません
                message = getString(R.string.network_error)
            }
            BlasRestErrCode.AUTH_ACCOUNT_ERROR-> {
                //ユーザ名，またはパスワードに誤りがあります
                message = getString(R.string.account_error)
            }
            else-> {
                //サーバでエラーが発生しました(要因コード)
                message = getString(R.string.server_error, error_code)
            }

        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
    }

}
