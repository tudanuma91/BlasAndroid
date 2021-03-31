package com.v3.basis.blas.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.controller.LocationController
import com.v3.basis.blas.blasclass.controller.LocationController.getLocation
import com.v3.basis.blas.blasclass.controller.QueueController
import com.v3.basis.blas.blasclass.service.BlasSyncService
import com.v3.basis.blas.ui.ext.setBlasCustomView
import com.v3.basis.blas.ui.terminal.BottomNavButton
import com.v3.basis.blas.ui.terminal.TerminalFragment


//ログイン後の画面を表示する処理
class TerminalActivity : AppCompatActivity() {

    companion object {
        const val BEFORE_FRAGMENT = "before"
    }

    lateinit var beforeSelectedNavButton: BottomNavButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)

        beforeSelectedNavButton = intent.extras?.getSerializable(BEFORE_FRAGMENT) as? BottomNavButton ?: BottomNavButton.DASH_BOARD

        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = TerminalFragment()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
            val thr = QueueController
            thr.start()



            // 位置情報のパーミッションを要求 一旦不要な為コメントアウトとする
            // checkPermission()
        }

        setBlasCustomView(R.layout.view_blas_search_actionbar)


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_fixture)
        when (item.itemId) {
            android.R.id.home -> {

                if (navController.currentDestination?.id == R.id.navi_item_view) {
                    this.finish()
                    return true
                } else {
                    //  ルート以外の画面なら、ルート画面に遷移させる！
                    val menu = PopupMenu(this, null).menu
                    menuInflater.inflate(R.menu.bottom_navigation_menu_fixture, menu)
                    NavigationUI.onNavDestinationSelected(menu.get(0), navController)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return false
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("位置情報", "位置情報許可されました")
                LocationController.getLocation()
            }
        }
        return
    }

    override fun onRestart() {
        super.onRestart()
        reloard()
    }

    fun reloard(){
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        //前回の状態をセーブしているから呼び出す！
        intent.putExtra(BEFORE_FRAGMENT, beforeSelectedNavButton)
        finish()

        overridePendingTransition(0, 0)
        startActivity(intent)
    }
}
