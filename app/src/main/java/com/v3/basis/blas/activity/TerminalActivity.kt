package com.v3.basis.blas.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.v3.basis.blas.R
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
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.commit()
        Log.d("title","呼ばれた")
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

    fun resorce(){

    }

}
