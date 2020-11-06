package com.v3.basis.blas.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.v3.basis.blas.R

class SplashActivity : AppCompatActivity() {
    val handler = Handler()
    val spHandler = SplashHandler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        //2000ミリ秒遅れて、「SpHandler」を実行させる
        handler.postDelayed(spHandler, 2000)
    }
    override fun onStop() {
        super.onStop()
        intent = null
        handler.removeCallbacks(spHandler)
    }
    //スプラッシュ画面からスタート画面に遷移するためのクラス
    inner class SplashHandler : Runnable {
        override fun run() {
            //画面遷移
            intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            //アクティビティを破棄する
            this@SplashActivity.finish()
        }
    }
}