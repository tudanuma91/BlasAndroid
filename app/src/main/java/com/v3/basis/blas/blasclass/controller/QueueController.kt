package com.v3.basis.blas.blasclass.controller
import android.content.ContentValues
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_QUEUE_ERR
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_RETRY_MAX_ERR
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_SERVER_ERROR
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.FUNC_NAME_FIXTURE
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.FUNC_NAME_ITEM
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_ADD
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_KENPIN
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_RTN
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_TAKEOUT
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_UPDATE
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_UPLOAD
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.READ_TIME_OUT_POST
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.REQUEST_TABLE
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.STS_RETRY_MAX
import com.v3.basis.blas.blasclass.app.Is2String
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import com.v3.basis.blas.blasclass.controller.LocationController.getLocation
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.database
import com.v3.basis.blas.blasclass.rest.*
import com.v3.basis.blas.blasclass.rest.BlasRest.Companion.context
import com.v3.basis.blas.blasclass.rest.BlasRest.Companion.queuefuncList
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode.Companion.NETWORK_ERROR
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread


/**
 * ?????????????????????????????????
 */
data class RestRequestData(
    val request_id:Int,                         /** ???????????? */
    val uri:String,                            /** ??????????????? */
    val method:String,                         /** ???????????????????????????????????? */
    val param_file:String,                    /** ???????????????????????????????????? */
    var retry_count:Int,                      /** ???????????????????????? */
    var error_code:Int,                       /** ?????????????????? */
    var status:Int                             /** ????????????????????? */
)


/**
 * ?????????????????????BLAS??????????????????????????????
 */
object QueueController {
    private var stop_flg:Boolean = false                                  //???????????????????????????
    private var reqList:MutableList<RestRequestData> = mutableListOf()     // ??????????????????
    var param:String = ""

    /**
     * ???????????????????????????
     */
    public fun start() {

        thread{
            stop_flg = false
            mainLoop()
        }
    }

    /**
     * ???????????????????????????
     */
    public fun stop() {
        stop_flg = true
    }

    /**
     * ??????????????????
     */
    @Synchronized public fun mainLoop() {

        var resCorde : Int
        var response : String
        lateinit var result:Pair<Int,String>

        /* ??????????????????????????????????????? */
        while(!stop_flg) {


                reqList = loadQueueFromDB()

                for (i in reqList.indices) {
                    try {
                        result = doConnect(reqList[i])
                    }catch(e:Exception) {
                        Log.e("ConnectError", e.toString())
                    }

                    try {
                        // ????????????????????????
                        if (result.first < 300) {
                            queueSuccess(reqList[i], result.second)
                        }
                        // ???????????????
                        else {
                            queueError(reqList[i], result.second)
                        }
                    }catch(e:Exception) {
                        Log.e("mainLoopError", e.toString())
                    }
                }

            /* ?????????????????????????????????????????????????????????????????? */

            Thread.sleep(10 * 1000)
        }

    }

    /**
     * DB???????????????????????????????????????
     */
    private fun loadQueueFromDB():MutableList<RestRequestData>{
        /**
         * DB??????????????????????????????????????????
         * ????????????????????????????????????????????????????????????????????????????????????????????????
         */

        var dataList:MutableList<RestRequestData> = mutableListOf()

        val sql = "select * from RequestTable"

        try {
            val cursor = database.rawQuery(sql, null)

            if (cursor.count > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast) {

                    Log.d("???DB???????????????","??????")
                    dataList.add(RestRequestData(
                        cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),
                        cursor.getInt(4),cursor.getInt(5),cursor.getInt(6)
                    ))

                    cursor.moveToNext()
                }
            }
            cursor.close()
        }catch(e: Exception) {
            Log.e("DbSelectError", e.toString())
        }

        return dataList
    }

    /**
     * DB?????????????????????????????????????????????????????????
     */

    private fun doConnect(reqArray:RestRequestData) :  Pair <Int,String> {

        val fileDir = BlasApp.applicationContext().getFilesDir().getPath()
        val filePath: String = fileDir + "/" + reqArray.param_file
        var response:String = ""
        var resCorde:Int = 0

        //???????????????????????????????????????
        try{
             param = File(filePath).readText(Charsets.UTF_8)
       }catch (e: FileNotFoundException){
            Log.e("FileReadError", e.toString())
        }
        param = param.removeSuffix("\n")

        // TODO ???????????????GET???????????????????????????
        if( (reqArray.method == "GET") or (reqArray.method == "DELETE") ) {
            response = doConnectGet(param, reqArray.uri,reqArray)
            return Pair(resCorde,response)
        }

        val url = java.net.URL(reqArray.uri)
        val con = url.openConnection() as HttpURLConnection

        //??????????????????????????????
        Log.d("???Queue???", "param:${param}")

        //??????????????????????????????????????????
        con.requestMethod = reqArray.method
        con.connectTimeout = BlasRest.CONTEXT_TIME_OUT
        con.readTimeout = READ_TIME_OUT_POST

        //???????????????????????????????????????
        con.doOutput = true
        val outStream = con.outputStream
        //?????????????????????
        try {
            outStream.write(param.toByteArray())
            outStream.flush()
            //??????????????????????????????????????????????????????????????????
            resCorde = con.responseCode
            Log.d("???Queue???", "Http_status:${resCorde}")

            //?????????????????????????????????
            outStream.close()

            //?????????????????????????????????
            val responseData = con.inputStream
            response = Is2String(responseData)

            con.disconnect()
        }catch(e:Exception){
            Log.d("ConnectionError", e.message)
            return Pair(NETWORK_ERROR,response)
        }
        return Pair(resCorde,response)

    }

    private fun doConnectGet(param:String, targetUrl:String,reqArray:RestRequestData) :  String {


        var url = java.net.URL(targetUrl + "?" + param)

        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = reqArray.method
        con.connectTimeout = BlasRest.CONTEXT_TIME_OUT
        con.readTimeout = BlasRest.READ_TIME_OUT
        con.doOutput = false  //GET????????????true???????????????????????????

        val responseData = con.inputStream
        val response = Is2String(responseData)
        val resCorde = con.responseCode
        con.disconnect()

        return response

    }

    private fun queueSuccess(reqArray:RestRequestData,response :String) {

        val whereClauses = "queue_id = ?"
        val whereArgs = arrayOf(reqArray.request_id.toString())
        lateinit var queueFunc:FuncList
        var tableName:String

        for(i in queuefuncList){
            if (i.id == reqArray.request_id){
                queueFunc = FuncList(i.id,i.successFun,i.errorFun,i.tableName)
            }
        }

        //DB??????????????????????????????
        try {
            database.delete(REQUEST_TABLE, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("deleteData " + reqArray.request_id, exception.toString())
        }

        try {
            tableName = queueFunc.tableName
        }catch(exception: Exception) {
            return
        }

        val rtn: RestfulRtn = cakeToAndroid(response, tableName)
        if(rtn == null) {
            noticeAdd(reqArray,APL_QUEUE_ERR)
        }
        else if(rtn.errorCode == 0) {
            noticeAdd(reqArray,APL_OK)
        }
        else {
            Log.e("???queue/error???", "errorCode:${rtn.errorCode}")
            noticeAdd(reqArray,rtn.errorCode)
        }

    }

    private fun queueError(reqArray:RestRequestData,response :String) {

        //???????????????????????????????????????????????????
        val values = ContentValues()
        val retry_count = reqArray.retry_count + 1
        val whereClauses = "queue_id = ?"
        val whereArgs = arrayOf(reqArray.request_id.toString())

        if(retry_count >= 100) {
            values.put("status", STS_RETRY_MAX)
            noticeAdd(reqArray,APL_RETRY_MAX_ERR)
            database.delete(REQUEST_TABLE, whereClauses, whereArgs)
        }

        values.put("retry_count", retry_count)


        try {
            database.update(REQUEST_TABLE, values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData " + reqArray.request_id, exception.toString())
        }

    }

    private fun noticeAdd(reqArray:RestRequestData,aplCode :Int) {
        val values = ContentValues()
        Log.d("reqArray?????????","${reqArray}")

        // ??????????????????
        val datetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val cur_date = datetime.format(LocalDateTime.now(ZoneId.of("Asia/Tokyo")))

        var funcName:String = ""
        var operation:String = ""
        var dataKey:String?  = ""

        // ????????????????????????
        var uriStr = reqArray.uri.toString() + "?" + param
        val validUri = Uri.parse(uriStr)

        var projectId:String? = validUri.getQueryParameter("project_id")

        if(reqArray.uri.contains("items")){
            funcName = FUNC_NAME_ITEM
            if(reqArray.method == "POST"){
                operation = OPE_NAME_ADD
            }else if(reqArray.method == "PUT"){
                operation = OPE_NAME_UPDATE
            }
            dataKey = validUri.getQueryParameter("item_id")

        }else if (reqArray.uri.contains("fixture")){
            funcName = FUNC_NAME_FIXTURE
            if(reqArray.uri.contains("update")){
                operation = OPE_NAME_UPDATE
            }else if (reqArray.uri.contains("kenpin")){
                operation = OPE_NAME_KENPIN
            }else if (reqArray.uri.contains("takeout")){
                operation = OPE_NAME_TAKEOUT
            }else if (reqArray.uri.contains("rtn")){
                operation = OPE_NAME_RTN
            }
            dataKey = validUri.getQueryParameter("fixture_id")

        }else if (reqArray.uri.contains("images")){
            funcName = FUNC_NAME_ITEM
            operation = OPE_NAME_UPLOAD
            dataKey = validUri.getQueryParameter("item_id")
        }

        values.put("apl_code", aplCode)
        values.put("read_status", 0)
        values.put("func_name", funcName)
        values.put("operation", operation)
        values.put("project_id", projectId)
        values.put("data_key", dataKey)

        values.put("update_date", cur_date)

        try {
            database.insertOrThrow("NoticeTable", null, values)
        }catch(exception: Exception) {
            Log.e("NoticeTable insertError", exception.toString())
        }

    }

    private fun queueRefresh(reqArray:RestRequestData,response :String) {
        val sql = "delete from RequestTable"
        try {
            database.delete("RequestTable",null,null)
        }catch(exception: Exception) {
            Log.e("deleteError", exception.toString())
        }

    }

}