package com.v3.basis.blas.blasclass.db.orgs

import androidx.room.*

@Dao
interface OrgsDao {

    @Query("SELECT * FROM Orgs")
    fun selectAll(): List<Orgs>

    @Insert
    fun insert(orgs: Orgs)

    @Update
    fun update(orgs: Orgs)

    @Delete
    fun delete(orgs: Orgs)
}
