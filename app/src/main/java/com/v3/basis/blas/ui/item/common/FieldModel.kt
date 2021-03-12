package com.v3.basis.blas.ui.item.common

import android.view.LayoutInflater
import androidx.databinding.ObservableField
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.databinding.InputField5Binding


open class FieldModel(
    val layoutInflater:LayoutInflater,
    val fieldNumber: Int,
    //val col:Int
    //val title: String
    //val mustInput: Boolean
    val field: LdbFieldRecord,
    val validationMsg: ObservableField<String>,
    val text: ObservableField<String>
) {

    open fun convertToString(): String? {
        return text.get()

    }

    open fun setValue(value: String?){
        value?.also { text.set(it) }
    }
}


