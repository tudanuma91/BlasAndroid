package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.util.Base64
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasApp.Companion.token
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import org.json.JSONObject
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * ログ回収クラス。
 * 障害発生時に、BLASに調査に必要な資料(ログやDBなど）をアップロードする
 */
class SyncBlasRestInquiry : SyncBlasRest() {

    val zipDir =  BlasApp.applicationContext().dataDir.path + "/temp/"

    val zipBufferSize = 1024 * 1024


    private fun entryZip( filePath:String,zipOutStream: ZipOutputStream ) {
        Log.d("entryZip()","filePath:" + filePath)

        FileInputStream(filePath.toString()).use {fileInputStream ->
            val bytes = ByteArray( zipBufferSize )
            val zipEntry = ZipEntry( filePath )
            zipOutStream.putNextEntry(zipEntry)

            var length : Int
            while( fileInputStream.read(bytes).also{ length = it }  >= 0 ) {
                zipOutStream.write(bytes,0,length)
            }
        }

        zipOutStream.closeEntry()
    }


    private fun create_zip( zipFile:String ) {

        val context = BlasApp.applicationContext()
        val preferences = context.getSharedPreferences(DownloadWorker.COMPLETED_DOWNLOAD, Context.MODE_PRIVATE)
        val all = preferences.all

        val blasDir = BlasApp.applicationContext().dataDir
        val directory = File(zipDir)
        directory.mkdirs()

        val outputPath = Paths.get( zipDir + zipFile )
        val zipFileCoding: Charset = Charset.forName("Shift_JIS")

        // zipを作る
        FileOutputStream( outputPath.toString()  ).use {fileOutputStream ->
            ZipOutputStream( BufferedOutputStream(fileOutputStream), zipFileCoding).use { zipOutStream ->

                entryZip(blasDir.toString() + "/databases/BLAS_DB",zipOutStream)

                all.forEach { key, filePath ->
                    entryZip(filePath.toString(),zipOutStream)
                }

            }
        }

    }

    private fun createBase64( file:File ) : String? {

        var base64 : String? = null
        try {
            val fis = FileInputStream(file)

            val bytes = ByteArray(file.length().toInt())
            fis.read(bytes)

            val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING   // 足し算されて11になる（bit加算）
            base64 = Base64.encodeToString(bytes,flag)
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