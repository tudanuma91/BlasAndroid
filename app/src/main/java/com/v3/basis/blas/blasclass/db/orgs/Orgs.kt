package com.v3.basis.blas.blasclass.db.orgs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Orgs(
    @PrimaryKey
    @ColumnInfo(name = "org_id") val org_id : Int?,
    @ColumnInfo(name = "name") val name : String?,
    @ColumnInfo(name = "kana") val kana : String?,
    @ColumnInfo(name = "en_name") val en_name : String?,
    @ColumnInfo(name = "type1_id") val type1_id : Int?,
    @ColumnInfo(name = "type2_id") val type2_id : Int?,
    @ColumnInfo(name = "mail") val mail : String?,
    @ColumnInfo(name = "remark") val remark : String?,
    @ColumnInfo(name = "status_id") val status_id : Int?,
    @ColumnInfo(name = "image") val image : String?,
    @ColumnInfo(name = "create_user_id") val create_user_id : Int?,
    @ColumnInfo(name = "create_date") val create_date : String?,
    @ColumnInfo(name = "update_date") val update_date : String?
)
