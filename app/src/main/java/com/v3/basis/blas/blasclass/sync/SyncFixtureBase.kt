package com.v3.basis.blas.blasclass.sync

import android.util.Log
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.blasclass.rest.SyncBlasRestFixture
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureCellModel
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject

abstract class SyncFixtureBase(val model: FixtureCellModel, val fixture : LdbFixtureRecord) {

    abstract open var crud:String

    abstract fun createPayload() : MutableMap<String,String>

    val eventCompleted: PublishSubject<Boolean> = PublishSubject.create()

    fun exec() {
        val payload = createPayload()
        //BlasRestFixture(crud, payload2, ::success, ::error).execute()

        SyncBlasRestFixture(context = model.context,fixture = fixture, crud = crud).execute(payload)

        eventCompleted.onNext(true)
    }

    open fun success(result: JSONObject) {
        Log.d("result",result.toString())

        // SQLite tableを更新する
        val fixtureController = FixtureController(  model.context, model.project_id.toString())
        fixtureController.resetSyncStatus(fixture.fixture_id.toString())

        eventCompleted.onNext(true)

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
            else -> {
                errMsg = "サーバー同期エラー"
            }
        }

        Log.d("errMsg" ,errMsg)

        eventCompleted.onNext(false)

        // SQLite tableを更新する
        val fixtureController = FixtureController(  model.context, model.project_id.toString())
        fixtureController.setErrorMsg(fixture.fixture_id.toString(),errMsg)

//        model.errorMessage.set(errMsg)
    }

}
