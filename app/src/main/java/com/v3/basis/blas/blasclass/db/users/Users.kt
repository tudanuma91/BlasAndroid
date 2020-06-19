package com.v3.basis.blas.blasclass.db.users

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Users(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id") val user_id: Int? = 0,
    @ColumnInfo(name = "username") val username: String? = "",
    @ColumnInfo(name = "org_id") val org_id: Int? = 0,
    @ColumnInfo(name = "name") val name: String? = "",
    @ColumnInfo(name = "group_id") val group_id: Int? = 0,
    @ColumnInfo(name = "image") val image: String? = "",
    @ColumnInfo(name = "sign") val sign: String? = "",
    @ColumnInfo(name = "status") val status: Int? = 0,
    @ColumnInfo(name = "w_count") val w_count: Int? = 0,
    @ColumnInfo(name = "remark") val remark: String? = "",
    @ColumnInfo(name = "en_org_id") val en_org_id: String? = "",
    @ColumnInfo(name = "alive_date") val alive_date: String? = "",
    @ColumnInfo(name = "lat") val lat: String? = "",
    @ColumnInfo(name = "lng") val lng: String? = "",
    @ColumnInfo(name = "use_polemap") val use_polemap: Int? = 0,
    @ColumnInfo(name = "working_item_id") val working_item_id: Int? = 0,
    @ColumnInfo(name = "active_date") val active_date: String? = "",
    @ColumnInfo(name = "create_user_id") val create_user_id: Int? = 0,
    @ColumnInfo(name = "create_date") val create_date: String? = "",
    @ColumnInfo(name = "update_date") val update_date: String? = ""
)
