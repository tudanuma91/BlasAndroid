package com.v3.basis.blas.blasclass.sync

import android.util.Log
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.blasclass.rest.SyncBlasRestFixture
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureCellModel
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.lang.Exception

abstract class SyncFixtureBase(val model: FixtureCellModel, val fixture : LdbFixtureRecord) {

    abstract open var crud:String

    abstract fun createPayload() : MutableMap<String,String>

    val eventCompleted: PublishSubject<Boolean> = PublishSubject.create()

    fun exec() {
        val payload = createPayload()
        val fixtureId = payload["fixture_id"]
        val json = SyncBlasRestFixture(crud).upload(payload)
        val ctl = FixtureController(
            model.context,
            model.project_id.toString()
        )
        if(json != null) {
            val errorCode = json.getInt("error_code")
            if(errorCode == 0) {
                if (fixtureId != null) {
                    if(fixtureId.toLong() < 0) {
                        val records = json.getJSONObject("records")
                        val new_fixture_id = records.getString("fixture_id")
                        val old_fixture_id = records.getString("temp_fixture_id")
                        ctl.updateFixtureId(old_fixture_id, new_fixture_id)
                    }
                    else {
                        ctl.resetSyncStatus(fixtureId)
                    }
                }
            }
            else {
                val errMsg = error(errorCode)

                if(fixtureId !=null){
                    ctl.setErrorMsg(fixtureId, errMsg)
                }
                throw Exception(errMsg)
            }
        }
        else {
            val errMsg = error(-1)
            if(fixtureId !=null){
                ctl.setErrorMsg(fixtureId, errMsg)
            }
            throw Exception(errMsg)
        }
    }

    open fun success(result: JSONObject) {
        Log.d("result",result.toString())

        // SQLite table???????????????
        val fixtureController =
            FixtureController(
                model.context,
                model.project_id.toString()
            )
        fixtureController.resetSyncStatus(fixture.fixture_id.toString())

        eventCompleted.onNext(true)

        Log.d("OK", "????????????")
    }

    fun error(errorCode: Int):String{
        Log.d("NG", "????????????")
        Log.d("errorCorde", "${errorCode}")

        var errMsg = ""
        when( errorCode ) {
            400 -> {
                errMsg = "???????????????????????????????????????"
            }
            401 -> {
                errMsg = "???????????????????????????????????????"
            }
            402 -> {
                errMsg = "???????????????????????????"
            }
            403 -> {
                errMsg = "??????????????????"
            }
            404 -> {
                errMsg = "?????????????????????????????????"
            }
            405 -> {
                errMsg = fixture.serial_number + "?????????????????????????????????????????????"
            }
            406 -> {
                errMsg = "????????????????????????"
            }
            407 -> {
                errMsg = "?????????????????????(" + fixture.fixture_id +")??????????????????????????????"
            }
            408 -> {
                errMsg = "???????????????????????????"
            }
            else -> {
                errMsg = "???????????????????????????"
            }
        }

        Log.d("errMsg" ,errMsg)

        // SQLite table???????????????
        val fixtureController =
            FixtureController(
                model.context,
                model.project_id.toString()
            )
        fixtureController.setErrorMsg(fixture.fixture_id.toString(),errMsg)

        //eventCompleted.onNext(false)
        //model.errorMessage.set(errMsg)
        return errMsg
    }

}
