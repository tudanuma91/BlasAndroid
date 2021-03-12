package com.v3.basis.blas.ui.item.common

import android.view.LayoutInflater
import androidx.databinding.ObservableField
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.databinding.InputField5Binding

interface FieldModel {
    val layoutInflater:LayoutInflater
    val fieldNumber: Int
    //val col:Int
    //val title: String
    //val mustInput: Boolean
    val field: LdbFieldRecord
    val validationMsg: ObservableField<String>

    fun convertToString(): String?
    fun setValue(value: String?)
}

// カレンダーを表示するmodel
interface FieldDateModel : FieldModel {
    val text: ObservableField<String>
}

