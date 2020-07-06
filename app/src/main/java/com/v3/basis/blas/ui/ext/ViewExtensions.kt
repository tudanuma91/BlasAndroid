package com.v3.basis.blas.ui.ext

import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ViewItemsRadioBinding
import com.v3.basis.blas.databinding.ViewRadiobuttonBinding
import com.v3.basis.blas.databinding.ViewRadiobuttonBindingImpl
import kotlinx.android.synthetic.main.fragment_item_create.*

fun ScrollView.hideKeyboardWhenTouch(fragment: Fragment) {
    setOnTouchListener { v, event ->
        fragment.closeSoftKeyboard()
        false
    }
}
