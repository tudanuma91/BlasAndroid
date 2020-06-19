package com.v3.basis.blas.blasclass.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.v3.basis.blas.blasclass.db.fixture.FixtureDao
import com.v3.basis.blas.blasclass.db.fixture.Fixtures
import com.v3.basis.blas.blasclass.db.users.Users
import com.v3.basis.blas.blasclass.db.users.UsersDao

@Database(entities = [Fixtures::class, Users::class], version = 1)
abstract class BlasDatabase: RoomDatabase() {

    abstract fun fixtureDao(): FixtureDao
    abstract fun usersDao(): UsersDao
}
