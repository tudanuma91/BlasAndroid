package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.util.Base64
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasApp.Companion.token
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SyncBlasRestInquiry : SyncBlasRest() {

    val zipDir =  BlasApp.applicationContext().dataDir.path + "/temp/"

    private fun create_zip( zipFile:String ) {

        val context = BlasApp.applicationContext()
        val preferences = context.getSharedPreferences(DownloadWorker.COMPLETED_DOWNLOAD, Context.MODE_PRIVATE)
        val all = preferences.all

//        val zipDir =  BlasApp.applicationContext().dataDir.path + "/temp/"
        val directory = File(zipDir)
        directory.mkdirs()

//        val zipFile = UUID.randomUUID().toString() + ".zip"
        val outputPath = Paths.get( zipDir + zipFile )
        val zipFileCoding: Charset = Charset.forName("Shift_JIS")
        val zipBufferSize = 1024 * 1024

        // TODO:ここでやるべきかは？？？
        // zipを作る
        FileOutputStream( outputPath.toString()  ).use {fileOutputStream ->
            ZipOutputStream( BufferedOutputStream(fileOutputStream), zipFileCoding).use { zipOutStream ->
                all.forEach { key, filePath ->
                    val zipEntry = ZipEntry( filePath.toString() )
                    zipOutStream.putNextEntry(zipEntry)
                    FileInputStream(filePath.toString()).use {fileInputStream ->
                        val bytes = ByteArray( zipBufferSize )
                        var length : Int
                        while( fileInputStream.read(bytes).also{ length = it }  >= 0 ) {
                            zipOutStream.write(bytes,0,length)
                        }
                    }
                    zipOutStream.closeEntry()
                }
            }
        }

    }

    private fun createBase64( file:File ) : String? {

        var base64 : String? = null
        try {
            val fis = FileInputStream(file)
            val length = file.length()

            val bytes = ByteArray(file.length().toInt())
            fis.read(bytes)
            base64 = Base64.encodeToString(bytes,Base64.DEFAULT)
        }
        catch ( ex : Exception ) {
            throw ex
        }

        return base64
    }

    fun execute() {
        Log.d("SyncBlasRestInquiry.execute()","start")

        // zipファイル生成
        val zipFile = UUID.randomUUID().toString() + ".zip"
        create_zip( zipFile )

        // base64化
        val file = File( zipDir + zipFile )
        val base64Zip = createBase64(file)

        val hash = MessageDigest.getInstance("SHA1")
            .digest(base64Zip?.toByteArray())
            .joinToString(separator = "") {
                "%02x".format(it)
            }

        // 送信
        val payload = mapOf(
            "token" to token,
            "hash" to hash,
            "zip" to base64Zip
        )

        var json:JSONObject? = null
        val method = "POST"
        val blasUrl = BlasRest.URL + "inquiry/upload/"

        try {
            val response = super.getResponseData(payload,method, blasUrl)
            json = JSONObject(response)

        }
        catch(e: Exception) {
            Log.d("blas-log", "通信エラー")
        }

        if (json != null) {
            val errorCode = json.getInt("error_code")

            if (errorCode == 0) {
                Log.d("Inquiry send","成功")
            }
            else {
                Log.d("Inquiry send","error!!!!!!!!!!!!!!")
            }

        }

        Log.d("SyncBlasRestInquiry.execute()","end")
    }






}