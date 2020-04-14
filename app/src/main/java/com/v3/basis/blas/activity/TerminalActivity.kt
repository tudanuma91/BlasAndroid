package com.v3.basis.blas.activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.controller.LocationController
import com.v3.basis.blas.blasclass.controller.LocationController.getLocation
import com.v3.basis.blas.blasclass.controller.QueueController
import com.v3.basis.blas.ui.terminal.TerminalFragment
import java.io.File
import java.io.FileOutputStream


//ログイン後の画面を表示する処理
class TerminalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)

        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = TerminalFragment()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
            val thr = QueueController
            thr.start()
        }
        // 位置情報のパーミッションを要求
        checkPermission()
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.commit()
        Log.d("title","呼ばれた")
    }

    fun resorce(){

    }

    /* 位置情報の許可を求める関数  */
    public fun checkPermission() {
        // 既に位置情報を許可している
        if (ContextCompat.checkSelfPermission(BlasApp.applicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            getLocation()
        }
        // 位置情報を拒否していた場合
        else{
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)

        }
    }

    /* 位置情報の入力に対するコールバック  */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("位置情報", "位置情報許可されました")
                LocationController.getLocation()
            }
        }
        return
    }


}
