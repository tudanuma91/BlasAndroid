package com.v3.basis.blas.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.v3.basis.blas.R
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {

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
                R.id.navi_item_seach,
                R.id.navi_item_back
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        //setupWithNavController

    }

   /* fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.commit()
        Log.d("title","呼ばれた")
    }*/

}
