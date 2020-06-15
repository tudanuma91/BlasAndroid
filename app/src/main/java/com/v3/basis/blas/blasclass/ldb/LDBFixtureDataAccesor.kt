package com.v3.basis.blas.blasclass.ldb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface LDBFixtureDataAccesor {
    @Query("SELECT * FROM Fixture")
    fun getAll():List<Fixture>

    @Insert
    fun insert(entity:Fixture)
}