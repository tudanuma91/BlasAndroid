package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONObject

class SyncBlasRestFixture(val crud:String) : SyncBlasRest() {

    lateinit var method : String
    lateinit var blasUrl : String

    init {
        val baseUrl = SyncBlasRest.URL + "fixtures/"

        when(crud) {
            "kenpin"->{
                method = "PUT"
                blasUrl = baseUrl + "kenpin/"
            }
            "0"->{
                method = "PUT"
                blasUrl = baseUrl + "kenpin/"
            }
            "takeout"->{
                method = "PUT"
                blasUrl = baseUrl + "takeout/"
            }
            "1"->{
                method = "PUT"
                blasUrl = baseUrl + "takeout/"
            }
            "rtn"->{
                method = "PUT"
                blasUrl = baseUrl + "rtn/"
            }
            "4"->{
                method = "PUT"
                blasUrl = baseUrl + "rtn/"
            }
            else -> {
                //crudが3の場合は持出不可。現時点では対応していない。
                Log.d("konishi", "対応していないcrudです ${crud}")
                throw Exception("対応していないcrudです ${crud}")
            }
        }

    }


    fun execute( payload:Map<String,String> ):JSONObject?  {
        var json:JSONObject? = null
        try{
            val response = super.getResponseData(payload,method,blasUrl)
            json = JSONObject(response)
        }
        catch(e:Exception) {
            Log.d("blas-log", "通信エラー")
        }
        return json

    }
}