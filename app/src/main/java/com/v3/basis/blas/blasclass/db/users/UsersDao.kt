package com.v3.basis.blas.blasclass.db.users

import androidx.room.*

@Dao
interface UsersDao {

    @Query("SELECT * FROM Users")
    fun selectAll(): List<Users>

    @Query("SELECT * FROM Users WHERE user_id = :user_id")
    fun select(user_id: Int): List<Users>

    @Insert
    fun insert(user: Users)

    @Update
    fun update(user: Users)

    @Delete
    fun delete(user: Users)
}
