package com.v3.basis.blas.ui.login


import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasApp.Companion.applicationContext
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.config.Params
import com.v3.basis.blas.blasclass.rest.BlasRestAuth
import com.v3.basis.blas.blasclass.service.BlasSyncService
import com.v3.basis.blas.blasclass.service.BlasSyncMessenger
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 * ログイン画面にかかわる処理を行う。
 * @param なし
 */
class LoginFragment : Fragment(), ServiceConnection {

    var username = ""
    var pass = ""
    var authRestFlg = false
    private var messenger: Messenger? = null
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
        userName.setText("konishiadmin")
        password.setText("afd0279c")
        setListener(btnLogin, ::loginSuccess, ::loginError)
    }

    /**
     * ログインボタン押下時、BLASに対してログイン要求を非同期で行う。
     * 成功時はloginSuccessメソッドを、失敗時はloginErrorメソッドをコールバックする。
     */
    private fun setListener(view: View, login: (JSONObject)->Unit, error: (Int)->Unit) {

        view.setOnClickListener{
            Log.d("【LoginFragment】", "Login開始")
            username = userName.text.toString()
            pass = password.text.toString()

            if(validation()) {
                val payload = mapOf("name" to username, "password" to pass)
                BlasRestAuth(payload, login, error).execute()
            }
        }
    }

    /**
     * ユーザ名とパスワードの値をチェックする。
     * @param なし
     * @return 正常時true, 異常時falseを返す。
     */
    private fun validation():Boolean {
        var ret = true

        if(username.isEmpty()) {
            /* ユーザ名、またはパスワードがnullの場合はエラー */
            Toast.makeText(getActivity(), R.string.username_null, Toast.LENGTH_LONG).show()
            ret = false
        }

        if(pass.isEmpty()) {
            Toast.makeText(getActivity(), R.string.password_null, Toast.LENGTH_LONG).show()
            ret = false
        }

        if(username.length > Params.USER_NAME_MAX_LEN) {
            /* ユーザ名が64文字より長い場合はエラー */
            Toast.makeText(getActivity(), R.string.user_name_too_long, Toast.LENGTH_LONG).show()
            ret = false
        }
        if(pass.length > Params.PASSWORD_MAX_LEN) {
            /* パスワードが64文字より長い場合はエラー */
            Toast.makeText(getActivity(), R.string.password_too_long, Toast.LENGTH_LONG).show()
            ret = false
        }

        return ret
    }

    /** ログインに成功したときにコールバックされ、
     * 掲示板の画面をキックする
     * @param in token ログインに成功したときのトークン
     * @param in userId ログインユーザーのID
     */
    private fun loginSuccess(json: JSONObject) {
        Log.d("BLAS", "Login成功")
        val records_json = json.getJSONObject("records")
        val auth_type = records_json.getString("auth_type")

        // 2段階認証処理
        if (auth_type == "1") {
            val authStatus = records_json.getString("auth_status")

            val procStop = twoAuth(authStatus)

            if (procStop){
                return
            }
        }

        val token = records_json.getString("token")
        val userId = records_json.getInt("user_id")

        FirebaseCrashlytics.getInstance().setCustomKey("token", token)
        BlasApp.token = token
        BlasApp.userId = userId

        /* BLASへのDB反映サービスを起動 */
        createNotificationChannel()
        val svintent = Intent(context, BlasSyncService::class.java)
        svintent.putExtra("token", token)
        requireActivity().stopService(svintent)
        requireActivity().startForegroundService(svintent)
        connect(svintent)

        /* プロジェクト選択画面起動 */
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

    /**
     * 2段階認証の処理を行う
     * @param in authStatus 2段階認証のステータス
     * @return true:処理中断　false:後続処理を続ける
     */
    private fun twoAuth(authStatus: String) : Boolean {

        if (authStatus == "1") {

            if (authRestFlg == true){
                Toast.makeText(getActivity(), R.string.twoauth_wrong, Toast.LENGTH_LONG).show()
                authRestFlg = false
            }

            val editText = EditText(applicationContext());

            AlertDialog.Builder(getActivity())
                .setTitle("SMS認証")
                .setMessage("認証コードを入力してください")
                .setView(editText)
                .setPositiveButton("送信") { dialog, which ->

                    if (editText.getText().toString().equals( "" )) {
                        Toast.makeText(getActivity(), R.string.twoauth_null, Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    val payload = mapOf("name" to username, "password" to pass, "sms" to editText.getText().toString())

                    authRestFlg = true;
                    BlasRestAuth(payload, ::loginSuccess, ::error).execute()

                }

                .setNegativeButton("閉じる") { dialog, which -> }
                .show()

            return true

        }else if(authStatus == "2"){
            authRestFlg = false;
            return false
        }else if(authStatus == "3"){
            Toast.makeText(getActivity(), R.string.twoauth_time_over, Toast.LENGTH_LONG).show()
            authRestFlg = false;
            return true
        }

        return false

    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        messenger = service?.let {
            BlasSyncMessenger.getInstance(it)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                1234.toString(),
                "お知らせ",
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "お知らせを通知します。"
            }
            val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * サービスへの接続
     */
    fun connect(intent: Intent) {
        if(!requireActivity().bindService(intent,this,Context.BIND_AUTO_CREATE)) {
            Log.d("konishi", "bindService Error")
        }
    }

    fun disconnect() {
        requireActivity().unbindService(this)
    }
}
