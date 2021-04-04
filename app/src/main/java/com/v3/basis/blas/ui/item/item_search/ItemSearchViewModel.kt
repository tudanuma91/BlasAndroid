package com.v3.basis.blas.ui.item.item_search

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

class ItemSearchViewModel(): ViewModel() {
    val freeWord = ObservableField<String>("")
    val isErrorOnly = ObservableField<Boolean>(false)
}
