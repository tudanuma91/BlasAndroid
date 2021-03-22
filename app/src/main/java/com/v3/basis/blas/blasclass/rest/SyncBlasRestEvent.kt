package com.v3.basis.blas.blasclass.rest

import android.util.Log
import org.json.JSONObject

class SyncBlasRestEvent(val crud:String) : SyncBlasRest() {

    lateinit var method : String
    lateinit var blasUrl : String

    init {
        when(crud) {
            "search"->{
                method = "GET"
                blasUrl = BlasRest.URL + "items/search/"
            }
            "update"->{
                method = "PUT"
                blasUrl = BlasRest.URL + "items/update/"
            }
            else -> {
                //crudが3の場合は持出不可。現時点では対応していない。
                Log.d("konishi", "対応していないcrudです ${crud}")
                throw Exception("対応していないcrudです ${crud}")
            }
        }

    }


    fun request( payload:Map<String,String> ):JSONObject  {
        val response = super.getResponseData(payload,method,blasUrl)
        return JSONObject(response)
    }
}