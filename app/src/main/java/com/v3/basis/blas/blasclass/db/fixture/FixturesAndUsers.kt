package com.v3.basis.blas.blasclass.db.fixture

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import com.v3.basis.blas.blasclass.db.users.Users

class FixturesAndUsers {
    @Embedded
    lateinit var fixtures: Fixtures

    @Embedded
//    lateinit var users: Users
    lateinit var username: String
//    @Relation(parentColumn = "fix_user_id", entityColumn = "user_id")
//    lateinit var users: List<Users>
}
