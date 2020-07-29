package com.v3.basis.blas.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.setBlasCustomView
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar
import kotlinx.android.synthetic.main.activity_item.*
import android.content.Intent


/**
 * データ管理画面クラス
 */
class ItemActivity : AppCompatActivity() {

    companion object {
        var reloard: Boolean = false
        var searchFreeWord: String? = null
        fun setRestartFlag() {
            reloard = true
        }
    }

    private val REQUESTCODE_TEST = 1

    data class formType(var type: String?,
                        var title: String?,
                        var field_col: String?,
                        var choiceValue: List<String?>?,
                        var require:String?,
                        var unique:String?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        val navController = findNavController(R.id.nav_host_fragment)
        setupWithNavController(item_list_bottom_navigation, navController)

        //タイトルバーの名称を変更する処理
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navi_item_view, //データ一覧
                R.id.navi_item_create, //データ新規作成
                R.id.navi_item_seach //データ検索
               // R.id.navi_item_back
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        /**
         * 戻るボタンが非表示になる問題の修正
         * [setupActionBarWithNavController]内部で[setDisplayHomeAsUpEnabled]をfalseにする処理が走るため、
         * リスナーにて再度[setDisplayHomeAsUpEnabled]trueとする。
         */
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            showBackKeyForActionBar()
            supportActionBar?.title = destination.label
        }

        setBlasCustomView()
    }

    /**
     * [説明]
     * データ一覧画面で画面上部の←ボタンを押したときにコールされる。
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        when (item.itemId) {
            android.R.id.home -> {

                if (navController.currentDestination?.id == R.id.navi_item_view) {
                    this.finish()
                    return true
                } else {
                    //  ルート以外の画面なら、ルート画面に遷移させる！
                    transitionItemListScreen()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun transitionItemListScreen() {
        val navController = findNavController(R.id.nav_host_fragment)
        val menu = PopupMenu(this, null).menu
        menuInflater.inflate(R.menu.bottom_navigation_menu_item, menu)
        NavigationUI.onNavDestinationSelected(menu.get(0), navController)
    }


    fun deleteFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment)
        fragmentTransaction.commit()
    }

    override fun onRestart() {
        super.onRestart()
        if (reloard) {
            reloard = false
            reloard()
        }
    }

    fun reloard(){
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()

        overridePendingTransition(0, 0)
        startActivity(intent)
    }

}
