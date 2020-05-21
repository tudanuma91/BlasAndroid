package com.v3.basis.blas.ui.login


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.work.WorkInfo
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.*
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.Params
import com.v3.basis.blas.blasclass.rest.BlasRestAuth
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.worker.SampleWorker
import com.v3.basis.blas.blasclass.worker.WorkerHelper
import kotlinx.android.synthetic.main.fragment_login.*

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

        return inflater.inflate(R.layout.fragment_login,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        setListener(btnLogin, ::loginSuccess, ::loginError)
    }

    /**
     * ログインボタン押下時、BLASに対してログイン要求を非同期で行う。
     * 成功時はloginSuccessメソッドを、失敗時はloginErrorメソッドをコールバックする。
     */
    private fun setListener(view: View, login: (String)->Unit, error: (Int)->Unit) {

        view.setOnClickListener{
            Log.d("【LoginFragment】", "Login開始")
            val username = userName.text.toString()
            val pass = password.text.toString()

            if(validation(username, pass)) {
                val payload = mapOf("name" to username, "password" to pass)
                BlasRestAuth(payload, login, error).execute()
            }
        }

        //sample worker
//        workerSample.setOnClickListener {
//            WorkerHelper.startDownload<SampleWorker>(this, "", "") {state, progress ->
//
//                when (state) {
//                    WorkInfo.State.ENQUEUED -> {
//                        Log.d("foreground worker", "enqueued")
//                    }
//                    WorkInfo.State.RUNNING -> {
//                        Log.d("foreground worker", "running, progress: $progress")
//                    }
//                }
//            }
//        }
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

        message = BlasMsg().getMessage(error_code,APL_OK)
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
    }

}
