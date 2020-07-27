package com.v3.basis.blas.blasclass.sync

import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureCellModel
import org.json.JSONObject

class Kenpin( model: FixtureCellModel ,fixture : LdbFixtureRecord ) : SyncFixtureBase( model,fixture ) {

    override var crud:String = "kenpin"

    override fun createPayload2() : MutableMap<String,String> {
        var payload2 = mutableMapOf(
            "token" to model.token,
            "fixture_id" to  fixture.fixture_id.toString(),
            "project_id" to  model.project_id.toString(),
            "fix_org_id" to fixture.fix_org_id.toString(),
            "fix_user_id" to fixture.fix_user_id.toString(),
            "fix_date" to fixture.fix_date,
            "serial_number" to fixture.serial_number,
            "update_date" to fixture.update_date,
            "sync_status" to fixture.sync_status.toString()
        )
        if( BaseController.SYNC_STATUS_NEW  == fixture.sync_status ) {
            payload2["create_date"] = fixture.create_date
        }

        return payload2
    }


    private fun regstNewRecord( result: JSONObject ) {
        val records = result.getJSONObject("records")
        Log.d("records",records.toString())

        val new_fixture_id = records.getString("fixture_id")
        val org_fixture_id = records.getString("temp_fixture_id")
        Log.d("fixture_id","new:" + new_fixture_id + " org:" + org_fixture_id)

        // SQLite tableを更新する
        val fixtureController = FixtureController(  model.context, model.project_id.toString())
        fixtureController.updateFixtureId(org_fixture_id,new_fixture_id)

    }

    override fun success(result: JSONObject){
        Log.d("result",result.toString())

        if( BaseController.SYNC_STATUS_NEW == fixture.sync_status ) {
            regstNewRecord(result)
        }

        Log.d("OK", "検品同期完了")
    }

}