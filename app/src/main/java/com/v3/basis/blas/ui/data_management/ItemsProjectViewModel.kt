package com.v3.basis.blas.ui.data_management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ItemsProjectViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "プロジェクト一覧"
    }
    val text: LiveData<String> = _text
}