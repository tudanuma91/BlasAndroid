package com.v3.basis.blas.ui.item.common

data class FieldData(
    val Field: FieldDataModel
)

data class FieldDataModel(
    val field_id: Int?,
    val project_id: Int?,
    val col: Int?,
    val name: String?,
    val type: Int?,
    val choice: String??,
    val alnum: Int?,
    val notify: Int?,
    val essential: Int?,
    val input: Int?,
    val export: Int?,
    val other: Int?,
    val map: Int?,
    val address: Int?,
    val filename: Int?,
    val parent_field_id: Int?,
    val summary: Int?,
    val create_date: String?,
    val update_date: String?,
    val unique_chk: Int?,
    val work_day: Int
)
