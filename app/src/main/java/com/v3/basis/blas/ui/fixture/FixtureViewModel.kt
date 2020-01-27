package com.v3.basis.blas.ui.fixture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FixtureViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is EquipmentManagement Fragment"
    }
    val text: LiveData<String> = _text
}