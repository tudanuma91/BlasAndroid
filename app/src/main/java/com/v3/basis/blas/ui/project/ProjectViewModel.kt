package com.v3.basis.blas.ui.project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProjectViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications test!!"
    }
    val text: LiveData<String> = _text
}