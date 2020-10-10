package com.v3.basis.blas.blasclass.component

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.security.MessageDigest

class DrawingImageComponent: ImageComponent() {
    override fun getSavedDir(context: Context, projectId:String):String {
        return context.dataDir.path + "/drawings/${projectId}/"
    }

    override fun saveBmp2Local(context: Context, projectId:String, fileName:String, bmp: Bitmap):String {
        var hash = ""
        if(context == null) {
            Log.d("blas", "contextがnullです")
            return hash
        }
        if(bmp == null) {
            Log.d("blas", "bmpがnullです")
            return hash
        }

        //一時ディレクトリ作成
        createTmpImageDir(context, projectId)
        val imgFullPath = getSavedDir(context, projectId) + fileName
        //画像ファイル名のフルパス作成
        try {
            val byteArrOutputStream = ByteArrayOutputStream()
            val fileOutputStream = FileOutputStream(imgFullPath)
            bmp!!.compress(getCompressFormat(fileName), 100, byteArrOutputStream)
            fileOutputStream.write(byteArrOutputStream.toByteArray())
            val digest: MessageDigest = MessageDigest.getInstance("MD5")
            hash = digest.digest(byteArrOutputStream.toByteArray()).joinToString(separator = "") {
                "%02x".format(it)
            }
            fileOutputStream.close()

        }
        catch (e:Exception){
            e.printStackTrace()
        }

        return hash
    }

    private fun getCompressFormat(filename: String): Bitmap.CompressFormat {
        return when(getExt(filename).toUpperCase()){
            "JPEG" -> Bitmap.CompressFormat.JPEG
            "JPG" -> Bitmap.CompressFormat.JPEG
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }
    }
}