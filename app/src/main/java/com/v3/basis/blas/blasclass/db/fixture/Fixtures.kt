package com.v3.basis.blas.blasclass.db.fixture

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Fixtures(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "fixture_id") var fixture_id: Int? = 0,
    @ColumnInfo(name = "project_id") var project_id: Int? =  0,
    @ColumnInfo(name = "fix_org_id") var fix_org_id: Int? =  0,
    @ColumnInfo(name = "fix_user_id") var fix_user_id: Int? =  0,
    @ColumnInfo(name = "fix_date") var fix_date: String? = "",
    @ColumnInfo(name = "takeout_org_id") var takeout_org_id: Int? =  0,
    @ColumnInfo(name = "takeout_user_id") var takeout_user_id: Int? =  0,
    @ColumnInfo(name = "takeout_date") var takeout_date: String? = "",
    @ColumnInfo(name = "rtn_org_id") var rtn_org_id: Int? =  0,
    @ColumnInfo(name = "rtn_user_id") var rtn_user_id: Int? =  0,
    @ColumnInfo(name = "rtn_date") var rtn_date: String? = "",
    @ColumnInfo(name = "item_id") var item_id: Int? =  0,
    @ColumnInfo(name = "item_org_id") var item_org_id: Int? =  0,
    @ColumnInfo(name = "item_user_id") var item_user_id: Int? =  0,
    @ColumnInfo(name = "item_date") var item_date: String? = "",
    @ColumnInfo(name = "serial_number") var serial_number: String? = "",
    @ColumnInfo(name = "status") var status: Int? =  0,
    @ColumnInfo(name = "sync_status") var sync_status: Int? = 0,
    @ColumnInfo(name = "create_date") var create_date: String? = "",
    @ColumnInfo(name = "update_date") var update_date: String? = ""
)
