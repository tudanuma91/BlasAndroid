package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import org.json.JSONObject

class SyncBlasRestFixture(
    val context: Context
    ,val fixture : LdbFixtureRecord
    , val crud:String
) : SyncBlasRest() {


    lateinit var method : String
    lateinit var blasUrl : String

    init {
        val baseUrl = SyncBlasRest.URL + "fixtures/"

        when(crud) {
            "search"->{
                method = "GET"
                blasUrl = baseUrl + "search/"
            }
            "create"->{
                method = "POST"
                blasUrl = baseUrl + "create/"
            }
            "update"->{
                method = "PUT"
                blasUrl = baseUrl + "update/"
            }
            "delete"->{
                method = "DELETE"
                blasUrl = baseUrl + "delete/"
            }
            "kenpin"->{
                method = "PUT"
                blasUrl = baseUrl + "kenpin/"
            }
            "takeout"->{
                method = "PUT"
                blasUrl = baseUrl + "takeout/"
            }
            "rtn"->{
                method = "PUT"
                blasUrl = baseUrl + "rtn/"
            }
        }

    }


    fun execute( payload:Map<String,String?> )  {

        var response:String? = null
        var json:JSONObject? = null

        try {
            response = super.getResponseData(payload,method,blasUrl)
            json = JSONObject(response)

            if( "kennpin" == crud ) {
                kenpinSuccess(json)
            }
            else {
                success(json)
            }
        }
        catch ( ex:Exception ) {
            Log.d("blas-log(fixture)", ex.message)
        }
    }

    fun success(result: JSONObject) {
        Log.d("result",result.toString())

        // SQLite tableを更新する
        val fixtureController = FixtureController( context, fixture.project_id.toString())
        fixtureController.resetSyncStatus( fixture.fixture_id.toString())

//        eventCompleted.onNext(true)

        Log.d("OK", "同期完了")
    }


    private fun regstNewRecord( result: JSONObject ) {
        val records = result.getJSONObject("records")
        Log.d("records",records.toString())

        val new_fixture_id = records.getString("fixture_id")
        val org_fixture_id = records.getString("temp_fixture_id")
        Log.d("fixture_id","new:" + new_fixture_id + " org:" + org_fixture_id)

        // SQLite tableを更新する
        val fixtureController = FixtureController(  context,  fixture.project_id.toString() )
        fixtureController.updateFixtureId(org_fixture_id,new_fixture_id)

    }

    fun kenpinSuccess(result: JSONObject) {

        Log.d("result",result.toString())

        if( BaseController.SYNC_STATUS_NEW == fixture.sync_status ) {
            regstNewRecord(result)
        }
//        eventCompleted.onNext(true)

        Log.d("OK", "検品同期完了")

    }


}