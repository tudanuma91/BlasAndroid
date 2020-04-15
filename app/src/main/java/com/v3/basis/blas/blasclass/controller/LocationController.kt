package com.v3.basis.blas.blasclass.controller

import android.Manifest
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.v3.basis.blas.blasclass.app.BlasApp.Companion.applicationContext
import java.lang.Exception

/**
 * 位置情報を扱うクラス
 */
object LocationController {

    /**
     * 位置情報を取得する関数
     */
    public fun getLocation()  {

        var local:Location? = null

        try{
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext())

            val locationRequest = LocationRequest().apply {
                // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
                interval = 100000                                   // 最遅の更新間隔  一旦100秒毎
                fastestInterval = 100000                             // 最短の更新間隔　 一旦100秒毎
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
            }

            // コールバック
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    val location = locationResult?.lastLocation ?: return
                    Log.d("Location", "緯度:${location.latitude}, 経度:${location.longitude}")
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)


            /*
            // 最後に取得した位置情報を取得するロジック 今回の開発では使わない
            fusedLocationClient.getLastLocation().addOnCompleteListener(OnCompleteListener<Location>(){
                if(it.isSuccessful){
                    val res = it.result
                    if (res != null) {
                        res.latitude
                        Log.d("Location", "緯度:${res.latitude} 経度:${res.longitude}")
                        Toast.makeText(applicationContext(),
                            "緯度:${res.latitude}, 経度:${res.longitude}", Toast.LENGTH_LONG).show()
                    }
                }
            })
            */

        } catch(e:Exception){
            Log.e("LocationGetError", e.toString())
        }

        return

    }

}