package com.v3.basis.blas.ui.item.common

interface FieldModel {
    val fieldNumber: Int
    val col:Int
    val title: String
    val mustInput: Boolean

    fun convertToString(): String?
    fun setValue(value: String?)
}
