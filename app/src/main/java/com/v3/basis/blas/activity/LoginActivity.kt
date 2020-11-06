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
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar


//ログイン画面を表示する処理
class LoginActivity : AppCompatActivity(), ServiceConnection {
    private var messenger: Messenger? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTitle(R.string.title_login)
        //リトライサービス起動

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

    //ServiceConnectionのインターフェース
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    }

    //ServiceConnectionのインターフェース
    override fun onServiceDisconnected(name: ComponentName?) {
    }
}


