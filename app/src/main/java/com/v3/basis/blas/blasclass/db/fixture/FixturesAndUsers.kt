package com.v3.basis.blas.blasclass.db.fixture

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import com.v3.basis.blas.blasclass.db.users.Users

data class FixturesAndUsers(
    @Embedded
    val fixtures: Fixtures,
    @Embedded
    val username: UserName
) {
    @Entity
    class UserName(
        @ColumnInfo(name = "u_username")
        val u_username: String
    )
}
