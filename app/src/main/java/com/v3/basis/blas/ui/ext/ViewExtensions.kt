package com.v3.basis.blas.ui.ext

import android.widget.ScrollView
import androidx.fragment.app.Fragment

fun ScrollView.hideKeyboardWhenTouch(fragment: Fragment) {
    setOnTouchListener { v, event ->
        fragment.closeSoftKeyboard()
        false
    }
}
