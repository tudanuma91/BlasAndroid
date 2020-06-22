package com.v3.basis.blas.blasclass.db.users

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import com.google.gson.annotations.SerializedName
import com.v3.basis.blas.blasclass.db.orgs.Orgs
import com.v3.basis.blas.blasclass.db.users.Users

data class UsersAndOrgs(
    @Embedded
    val user: Users,
    @Embedded
    val org: OrgId
) {
    @Entity
    class OrgId (
        @ColumnInfo(name = "o_id")
        val o_id: Int
    )
}
