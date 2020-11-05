package com.v3.basis.blas.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.v3.basis.blas.R
import android.view.MenuItem
import android.os.Build
import com.v3.basis.blas.blasclass.service.RetryService
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar


//ログイン画面を表示する処理
class LoginActivity : AppCompatActivity() {

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
}


