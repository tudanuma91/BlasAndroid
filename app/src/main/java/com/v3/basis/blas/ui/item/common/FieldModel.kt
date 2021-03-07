package com.v3.basis.blas.ui.item.common

import androidx.databinding.ObservableField
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord

interface FieldModel {
    val fieldNumber: Int
    //val col:Int
    //val title: String
    //val mustInput: Boolean
    val field: LdbFieldRecord
    val validationMsg: ObservableField<String>

    fun convertToString(): String?
    fun setValue(value: String?)
}
