package com.v3.basis.blas.blasclass.db.users

import android.content.Context
import com.v3.basis.blas.blasclass.db.BaseController

class UsersController(context: Context, projectId: String): BaseController(context, projectId) {

    fun joinTest(): List<UsersAndOrgs> {
        val db = openDatabase()
        return db.usersDao().joinTest()
    }
}
