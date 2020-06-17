package com.v3.basis.blas.blasclass.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.v3.basis.blas.blasclass.db.fixture.FixtureDao
import com.v3.basis.blas.blasclass.db.fixture.Fixtures

@Database(entities = [Fixtures::class], version = 1)
abstract class BlasDatabase: RoomDatabase() {

    abstract fun fixtureDao(): FixtureDao
}
