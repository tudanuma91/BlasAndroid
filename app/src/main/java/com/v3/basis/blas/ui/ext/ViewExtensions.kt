package com.v3.basis.blas.ui.ext

import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.setActionBarTitle(resId: Int) {
    this.supportActionBar?.title = getString(resId)
}
