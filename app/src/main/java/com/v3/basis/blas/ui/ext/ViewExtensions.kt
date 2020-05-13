package com.v3.basis.blas.ui.ext

import android.widget.ScrollView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_item_create.*

fun ScrollView.hideKeyboardWhenTouch(fragment: Fragment) {
    setOnTouchListener { v, event ->
        fragment.closeSoftKeyboard()
        false
    }
}
