package com.v3.basis.blas.blasclass.db.fixture

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Fixtures(
    @PrimaryKey
    val fixture_id: Int,
    val project_id: Int,
    val fix_org_id: Int,
    val fix_user_id: Int,
    val fix_date: String?,
    val takeout_org_id: Int?,
    val takeout_user_id: Int?,
    val takeout_date: String?,
    val rtn_org_id: Int?,
    val rtn_user_id: Int?,
    val rtn_date: String?,
    val item_id: Int?,
    val item_org_id: Int?,
    val item_user_id: Int?,
    val item_date: String?,
    val serial_number: String,
    val status: Int,
    val create_date: String,
    val update_date: String
)
