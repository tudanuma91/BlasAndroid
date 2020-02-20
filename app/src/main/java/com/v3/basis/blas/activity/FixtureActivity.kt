package com.v3.basis.blas.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.v3.basis.blas.R
import kotlinx.android.synthetic.main.activity_fixture.*

class FixtureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fixture)

        val navController = findNavController(R.id.nav_host_fragment_fixture)
        setupWithNavController(bottom_navigation_fixture, navController)
    }
}
