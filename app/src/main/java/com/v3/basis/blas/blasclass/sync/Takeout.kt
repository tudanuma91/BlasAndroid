package com.v3.basis.blas.blasclass.sync

import android.util.Log
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureCellModel
import org.json.JSONObject

class Takeout(model: FixtureCellModel, fixture : LdbFixtureRecord ) : SyncFixtureBase( model,fixture ) {

    override var crud: String = "takeout"

    override fun createPayload(): MutableMap<String, String> {

        var payload2 = mutableMapOf(
            "token" to model.token,
            "fixture_id" to fixture.fixture_id.toString(),
            "project_id" to model.project_id.toString(),
            "takeout_org_id" to fixture.takeout_org_id.toString(),
            "takeout_user_id" to fixture.takeout_user_id.toString(),
            "takeout_date" to fixture.takeout_date,
            "serial_number" to fixture.serial_number,
            "update_date" to fixture.update_date,
            "sync_status" to fixture.sync_status.toString()
        )

        return payload2
    }

}
