package com.v3.basis.blas.ui.ext

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.analytics.BlasLogger

fun AppCompatActivity.setActionBarTitle(resId: Int) {
    this.supportActionBar?.title = getString(resId)
}

fun AppCompatActivity.showBackKeyForActionBar() {
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setHomeButtonEnabled(true)
    }
}

fun AppCompatActivity.setBlasCustomView(layout: Int = R.layout.view_blas_actionbar) {

    supportActionBar?.apply {
        setCustomView(layout)
        setDisplayShowCustomEnabled(true)
        setDisplayShowTitleEnabled(true)
        setDisplayShowHomeEnabled(true)
    }
}
