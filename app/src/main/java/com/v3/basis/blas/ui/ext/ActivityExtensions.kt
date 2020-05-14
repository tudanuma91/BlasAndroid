package com.v3.basis.blas.ui.ext

import androidx.appcompat.app.AppCompatActivity
import com.v3.basis.blas.R

fun AppCompatActivity.setActionBarTitle(resId: Int) {
    this.supportActionBar?.title = getString(resId)
}

fun AppCompatActivity.showBackKeyForActionBar() {
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setHomeButtonEnabled(true)
    }
}

fun AppCompatActivity.setBlasCustomView() {

    supportActionBar?.apply {
        setCustomView(R.layout.view_blas_actionbar)
        setDisplayShowCustomEnabled(true)
        setDisplayShowTitleEnabled(true)
        setDisplayShowHomeEnabled(true)
    }
}
