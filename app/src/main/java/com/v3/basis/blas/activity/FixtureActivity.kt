package com.v3.basis.blas.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.v3.basis.blas.R
import kotlinx.android.synthetic.main.activity_fixture.*
import androidx.navigation.ui.setupActionBarWithNavController

class FixtureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fixture)

        val navController = findNavController(R.id.nav_host_fragment_fixture)
        setupWithNavController(bottom_navigation_fixture, navController)


        //タイトルバーの名称を変更する処理
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navi_fixture_view,
                R.id.navi_fixture_kenpin,
                R.id.navi_fixture_motidasi,
                R.id.navi_fixture_return,
                R.id.navi_fixture_search
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        /**
         * 戻るボタンが非表示になる問題の修正
         * [setupActionBarWithNavController]内部で[setDisplayHomeAsUpEnabled]をfalseにする処理が走るため、
         * リスナーにて再度[setDisplayHomeAsUpEnabled]trueとする。
         */
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            supportActionBar?.apply {
                title = destination.label
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            }
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

}
