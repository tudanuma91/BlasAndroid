package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import org.json.JSONObject

class SyncBlasRestFixture(
    val crud:String
    //,val funcSuccess:(JSONObject)->Unit
    //,val funcError:(Int)->Unit


) : SyncBlasRest() {

    lateinit var method : String
    lateinit var blasUrl : String

    init {
        val baseUrl = SyncBlasRest.URL + "fixtures/"

        when(crud) {
/*
            // 対応外
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
 */
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
            else -> {
                throw Exception("対応していないcrudです")
            }
        }

    }


    fun execute( payload:Map<String,String?> ):Int  {

        try {
            val response = super.getResponseData(payload,method,blasUrl)
            val json = JSONObject(response)

            val errorCode = json.getString("error_code")
            /*
            if( "0" == errorCode  ) {
                funcSuccess( json )
            }
            else {
                funcError( errorCode.toInt() )
            }*/
            return errorCode.toInt()
        }
        catch ( ex:Exception ) {
            Log.d("blas-log(fixture)", ex.message)
        }
        return -1
    }



}