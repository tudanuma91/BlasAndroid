package com.v3.basis.blas.ui.ext

import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean

object CustomBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["hasNotUrl", "nowDownloading"], requireAll = false)
    fun ImageView.setVisibleTwoCondition(hasNotUrl: Boolean, nowDownloading: Boolean) {

        if (hasNotUrl.not() && nowDownloading.not()) {
            visibility = View.VISIBLE
        } else {
            visibility = View.INVISIBLE
        }
    }
}
