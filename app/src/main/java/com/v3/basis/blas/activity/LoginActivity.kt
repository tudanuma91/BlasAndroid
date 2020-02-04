package com.v3.basis.blas.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.test1.Test1Fragment
import kotlinx.android.synthetic.main.fragment_login.*

//ログイン画面を表示する処理
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    }
}


