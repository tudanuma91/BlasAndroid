package com.v3.basis.blas.blasclass.rest

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SyncBlasRestInquiry : SyncBlasRest() {


    fun execute() {
        Log.d("SyncBlasRestInquiry.execute()","start")

        val context = BlasApp.applicationContext()
        val preferences = context.getSharedPreferences(DownloadWorker.COMPLETED_DOWNLOAD, Context.MODE_PRIVATE)
        val all = preferences.all

        val zipDir =  BlasApp.applicationContext().dataDir.path + "/temp/"
        val directory = File(zipDir)
        directory.mkdirs()

        val zipFile = UUID.randomUUID().toString() + ".zip"
        val outputPath = Paths.get( zipDir + zipFile )
        val zipFileCoding: Charset = Charset.forName("Shift_JIS")
        val zipBufferSize = 1024 * 1024

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

        Log.d("SyncBlasRestInquiry.execute()","end")
    }






}