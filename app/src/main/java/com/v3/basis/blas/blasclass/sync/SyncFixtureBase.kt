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

        Log.d("OK", "同期完了")
    }

    fun error(errorCode: Int, aplCode :Int){
        Log.d("NG", "作成失敗")
        Log.d("errorCorde", "${errorCode}")

        var errMsg = ""
        when( errorCode ) {
            400 -> {
                errMsg = "持出登録が行われていません"
            }
            401 -> {
                errMsg = "設置者と作業者が異なります"
            }
            402 -> {
                errMsg = "すでに設置済みです"
            }
            403 -> {
                errMsg = "検品済みです"
            }
            404 -> {
                errMsg = "持出しできない機器です"
            }
            405 -> {
                errMsg = fixture.serial_number + "は未登録のシリアルナンバーです"
            }
            406 -> {
                errMsg = "すでに持出中です"
            }
            407 -> {
                errMsg = "指定されたＩＤ(" + fixture.fixture_id +")は登録されていません"
            }
            408 -> {
                errMsg = "すでに返却済みです"
            }
        }

        Log.d("errMsg" ,errMsg)

        // SQLite tableを更新する
        val fixtureController = FixtureController(  model.context, model.project_id.toString())
        fixtureController.setErrorMsg(fixture.fixture_id.toString(),errMsg)
    }

}