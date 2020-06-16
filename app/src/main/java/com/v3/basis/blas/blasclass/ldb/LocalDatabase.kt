package com.v3.basis.blas.blasclass.ldb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Fixture::class], version = 1)
abstract class DataBase: RoomDatabase() {

    abstract fun LDBFixtureAccesor(): LDBFixtureDataAccesor
}