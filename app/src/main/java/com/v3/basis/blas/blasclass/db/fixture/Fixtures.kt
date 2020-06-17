package com.v3.basis.blas.blasclass.db.fixture

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Fixtures(
    @PrimaryKey
    @ColumnInfo(name = "fixture_id") val fixture_id: Int,
    @ColumnInfo(name = "project_id") val project_id: Int,
    @ColumnInfo(name = "fix_org_id") val fix_org_id: Int,
    @ColumnInfo(name = "fix_user_id") val fix_user_id: Int,
    @ColumnInfo(name = "fix_date") val fix_date: String?,
    @ColumnInfo(name = "takeout_org_id") val takeout_org_id: Int?,
    @ColumnInfo(name = "takeout_user_id") val takeout_user_id: Int?,
    @ColumnInfo(name = "takeout_date") val takeout_date: String?,
    @ColumnInfo(name = "rtn_org_id") val rtn_org_id: Int?,
    @ColumnInfo(name = "rtn_user_id") val rtn_user_id: Int?,
    @ColumnInfo(name = "rtn_date") val rtn_date: String?,
    @ColumnInfo(name = "item_id") val item_id: Int?,
    @ColumnInfo(name = "item_org_id") val item_org_id: Int?,
    @ColumnInfo(name = "item_user_id") val item_user_id: Int?,
    @ColumnInfo(name = "item_date") val item_date: String?,
    @ColumnInfo(name = "serial_number") val serial_number: String,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "create_date") val create_date: String,
    @ColumnInfo(name = "update_date") val update_date: String
)
