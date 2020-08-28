package com.v3.basis.blas.blasclass.rest

import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasDef
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode.Companion.NETWORK_ERROR
import com.v3.basis.blas.ui.ext.traceLog
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection

open class SyncBlasRestCache() : SyncBlasRest(){


    val method = "GET"
    var aplCode:Int = 0
    val crud:String = "zip"


    fun getBinaryFromURL(url: String):ByteArray?{
        var inputStream : InputStream? = null
        val url = java.net.URL(url)
        val con = url.openConnection() as HttpURLConnection

        con.setRequestProperty("Content-Type", "application/octet-stream")
        con.requestMethod = "GET"

        con.connect()

        if (con.responseCode === HttpURLConnection.HTTP_OK) {
            inputStream = con.inputStream
        }

        inputStream?.let {
            val buffer = ByteArray(1024 * 1024 * 20)
            val bOut = ByteArrayOutputStream()
            while (true) {
                val len = it.read(buffer)
                if (len < 0) {
                    break
                }
                bOut.write(buffer, 0, len)
            }
            return bOut.toByteArray()
        }
        return null
    }

    fun downloadZipUrl(payload:Map<String, String?>): String?  {

        var response: String? = null

        val url = URL + "cache/search_sqlite/"

        try {
            Log.d("method:", method)
            Log.d("url:", url)
            val data = super.getResponseData(payload, method, url)
            response = data
        }
        catch(e: Exception) {
            Log.d("blas-log", e.message)
            traceLog("Failed to search zip files")
        }
        return response
    }
}
