package com.v3.basis.blas.blasclass.db.users

import android.content.Context
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.ldb.LdbUserRecord

class UsersController(context: Context, projectId: String): BaseController(context, projectId) {

        fun search() : LdbUserRecord? {
            return getUserInfo()
        }


}
