package com.v3.basis.blas.blasclass.sync

import android.util.Log
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureCellModel
import org.json.JSONObject

abstract class SyncFixtureBase(val model: FixtureCellModel, val fixture : LdbFixtureRecord) {

    abstract open var crud:String

    abstract fun createPayload2() : MutableMap<String,String>

    fun exec() {
        val payload2 = createPayload2()
        BlasRestFixture(crud, payload2, ::success, ::error).execute()
    }

    open fun success(result: JSONObject) {
        Log.d("result",result.toString())

        // SQLite tableを更新する
        val fixtureController = FixtureController(  model.context, model.project_id.toString())
        fixtureController.resetSyncStatus(fixture.fixture_id.toString())

        Log.d("OK", "持ち出し同期完了")
    }

    fun error(errorCode: Int, aplCode :Int){
        Log.d("NG", "作成失敗")
        Log.d("errorCorde", "${errorCode}")
    }

}