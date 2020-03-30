package com.v3.basis.blas.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.ui.ext.showBackKeyForActionBar

import kotlinx.android.synthetic.main.activity_item.*
import com.v3.basis.blas.blasclass.formaction.FormActionDataBasic


class ItemActivity : AppCompatActivity() {
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
                R.id.navi_item_view,
                R.id.navi_item_create,
                R.id.navi_item_seach
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


    fun deleteFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment)
        fragmentTransaction.commit()
    }

}
