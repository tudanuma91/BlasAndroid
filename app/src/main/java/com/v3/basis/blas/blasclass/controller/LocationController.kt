package com.v3.basis.blas.blasclass.controller

import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasApp.Companion.applicationContext
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.READ_TIME_OUT_POST
import com.v3.basis.blas.blasclass.app.Is2String
import com.v3.basis.blas.blasclass.rest.*
import java.lang.Exception
import java.net.HttpURLConnection
import kotlin.concurrent.thread


/**
 * シングルトン。BLASへの通信を制御する。
 */
object LocationController {

    private var stop_flg:Boolean = false                                  //スレッド停止フラグ
    val lock = java.util.concurrent.locks.ReentrantLock()                   //排他制御
    var param:String = ""

    /**
     * スレッドを開始する
     */
    public fun start() {

        thread{
            stop_flg = false
            mainLoop()
        }
    }

    /**
     * スレッドを停止する
     */
    public fun stop() {
        stop_flg = true
    }

    /**
     * 送信スレッド
     */
    @Synchronized public fun mainLoop() {

        var resCorde : Int
        var response : String
        var param : String = ""

        /* 通信のバックグラウンド処理 */
        while(!stop_flg) {
            try {

                var result  = doConnect(param)

                // 通信が正常の場合
                if(result.first < 300){
                } // 通信エラー
                else{
                }
            }
            catch(e:Exception) {
                Log.e("mainLoopError", e.toString())
            }
            /* キューデータを通信エラーになるまでループする */

            Thread.sleep(600 * 1000)
        }

    }

    /**
     * BLASと通信する関数
     */
    private fun doConnect(param:String) :  Pair <Int,String> {

        //todo 位置情報のURLを足すこと
        val targetUrl = BlasRest.URL
        val url = java.net.URL(targetUrl)
        val con = url.openConnection() as HttpURLConnection

        //タイムアウトとメソッドの設定
        con.requestMethod = "POST"
        con.connectTimeout = BlasRest.CONTEXT_TIME_OUT
        con.readTimeout = READ_TIME_OUT_POST

        //リクエストパラメータの設定
        con.doOutput = true
        val outStream = con.outputStream
        //リクエスト処理
        outStream.write(param.toByteArray())
        outStream.flush()
        //エラーコードなど飛んでくるのでログに出力する
        val resCorde = con.responseCode
        Log.d("【LocationController】", "Http_status:${resCorde}")

        //リクエスト処理処理終了
        outStream.close()

        //レスポンスデータを取得
        val responseData = con.inputStream
        val response = Is2String(responseData)

        con.disconnect()
        return Pair(resCorde,response)

    }

    /**
     * 位置情報を取得する関数
     */
    public fun getLocation() : Location? {

        var local:Location? = null

        try{
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext())

            // どのような取得方法を要求
            val locationRequest = LocationRequest().apply {
                // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
                interval = 10000                                   // 最遅の更新間隔(但し正確ではない。)
                fastestInterval = 5000                             // 最短の更新間隔
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
            }

            // コールバック
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    val location = locationResult?.lastLocation ?: return
                    Log.d("mainLoopError", "緯度:${location.latitude}, 経度:${location.longitude}")

                    Toast.makeText(applicationContext(),
                        "緯度:${location.latitude}, 経度:${location.longitude}", Toast.LENGTH_LONG).show()
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

     //       Log.d("位置", location.toString())
        } catch(e:Exception){
            Log.e("LocationGetError", e.toString())
        }

        return local

    }



}