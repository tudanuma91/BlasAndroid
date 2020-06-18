package com.v3.basis.blas.blasclass.db.fixture

import androidx.room.*

@Dao
interface FixtureDao {

    @Query("SELECT * FROM Fixtures")
    fun selectAll(): List<Fixtures>

    @Query("SELECT * FROM Fixtures WHERE fixture_id = :fixture_id")
    fun select(fixture_id: Int): List<Fixtures>

    @Insert
    fun insert(fixtures: Fixtures)

    @Update
    fun update(fixtures: Fixtures)

    @Delete
    fun delete(fixtures: Fixtures)
}