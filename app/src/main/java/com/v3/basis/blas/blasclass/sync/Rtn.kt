package com.v3.basis.blas.blasclass.sync

import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureCellModel

class Rtn(model: FixtureCellModel, fixture : LdbFixtureRecord ) : SyncFixtureBase( model,fixture ) {

    override var crud:String = "rtn"

    override fun createPayload(): MutableMap<String, String> {

        var payload2 = mutableMapOf(
            "token" to model.token,
            "fixture_id" to  fixture.fixture_id.toString(),
            "project_id" to  model.project_id.toString(),
            "rtn_org_id" to fixture.rtn_org_id.toString(),
            "rtn_user_id" to fixture.rtn_user_id.toString(),
            "rtn_date" to fixture.rtn_date,
            "serial_number" to fixture.serial_number,
            "update_date" to fixture.update_date,
            "sync_status" to fixture.sync_status.toString()
        )

        return payload2
    }

}