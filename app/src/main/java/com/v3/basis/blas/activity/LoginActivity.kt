package com.v3.basis.blas.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.v3.basis.blas.R
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.v3.basis.blas.blasclass.service.RetryService
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar


//ログイン画面を表示する処理
class LoginActivity : AppCompatActivity(), ServiceConnection {
    private lateinit var messenger: Messenger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTitle(R.string.title_login)
        //リトライサービス起動
        if(savedInstanceState == null) {
            createNotificationChannel()
            val intent = Intent(this, RetryService::class.java)
            stopService(intent)
            startForegroundService(intent)
            connect(intent)
        }


        showBackKeyForActionBar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //Write your logic here
                this.finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    //ServiceConnectionのインターフェース
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        messenger = Messenger(service);
        send()
        Toast.makeText(applicationContext, "サービスに接続しました", Toast.LENGTH_SHORT).show()
    }

    //ServiceConnectionのインターフェース
    override fun onServiceDisconnected(name: ComponentName?) {

        Toast.makeText(applicationContext, "サービスから切断されました", Toast.LENGTH_SHORT).show()
    }


    /**
     * サービスへの接続
     */
    fun connect(intent: Intent) {
        bindService(intent,this,Context.BIND_AUTO_CREATE)
    }

    fun disconnect() {
        unbindService(this)
    }

    fun send() {
        messenger.send(Message.obtain(null, 0, "hoge"))
    }
}


