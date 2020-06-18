package com.v3.basis.blas.blasclass.db.fixture

import android.content.Context
import com.v3.basis.blas.blasclass.db.BaseController

class FixtureController(context: Context, projectId: String): BaseController(context, projectId) {

    fun search(): List<Fixtures> {

        val db = openDatabase()
        return db.fixtureDao().selectAll()
    }
}
