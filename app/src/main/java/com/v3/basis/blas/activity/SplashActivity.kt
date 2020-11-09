package com.v3.basis.blas.activity

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ActivitySplashBinding
import com.v3.basis.blas.ui.splash.SplashBinding
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SplashActivity : AppCompatActivity() {

    val handler = Handler()
    val spHandler = SplashHandler()
    //APIの接続先の方が分岐しやすいため、こちらを採用
    val productionURL = "https://www.basis-service.com/blas70/api/v1/"
    val buildHost = BuildConfig.API_URL
    val message = "接続先：${BuildConfig.API_URL}"

    val fileName = "Splash.gif"

    //データバインディングを定義
    private val hostData = SplashBinding(message)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

       // messageSeter()

        val binding: ActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        //gif画像を表示する処理
        animationStart(binding)

        //デバック用の接続を行った時
        if(buildHost != productionURL){
            //接続先を出力する
            binding.hostData = hostData
        }
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

   // @RequiresApi(Build.VERSION_CODES.P)
    @RequiresApi(Build.VERSION_CODES.P)
    // gifを表示する処理
    private fun getGifAnimation_AfterP():AnimatedImageDrawable{
       //画像ソースを取得(assets直下)
       val source = ImageDecoder.createSource(assets,fileName )
       return ImageDecoder.decodeDrawable(source) as? AnimatedImageDrawable
           ?: throw ClassCastException()
    }


    //gifを表示する処理＋動かす処理
    private fun animationStart(binding: ActivitySplashBinding){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //pie以降はこっちの処理を使用する
            val drawable = getGifAnimation_AfterP()
            binding.imageView.setImageDrawable(drawable)
            drawable.start()
        }else {
            //pieより前はこっちの処理を使用する
            //後々ここの処理は削除したい。
            Glide.with(this).load(R.drawable.splash2).into(binding.imageView)
        }
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