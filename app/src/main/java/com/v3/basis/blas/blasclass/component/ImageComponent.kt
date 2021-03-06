package com.v3.basis.blas.blasclass.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.bumptech.glide.load.model.ResourceLoader
import com.v3.basis.blas.ui.item.item_image.FileExtensions
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.text.StringBuilder

open class ImageComponent {

    /**
     * [説明]
     * 画像のフルパスを取得する
     */
    open fun getSavedDir(context: Context, projectId:String):String {
        return context.dataDir.path + "/images/${projectId}/"
    }

    /**
     * [説明]
     * 画像ファイルを削除する。パスは内部で求める
     */
    fun delImgFile(context: Context, projectId:String, imgFileName:String) {
        val dirName = getSavedDir(context, projectId)
        val fullPath = dirName + imgFileName
        if(File(fullPath).exists()) {
            File(fullPath).delete()
        }
    }

    fun createTmpImageDir(context: Context, projectId:String) {
        val dirName = getSavedDir(context, projectId)
        if(!File(dirName).exists()) {
            File(dirName).mkdirs()
        }
    }
    fun createTmpImageFileName():String {
        return  UUID.randomUUID().toString()+".jpg"
    }
    /**
     * [説明]
     * 画像をローカルに保存する
     * 保存先のパスは/data/data/com.v3.basis.blas/436/images/uuid.jpgとなる
     * [戻り値]
     * 画像ファイルのハッシュ値
     *
     */
    open fun saveBmp2Local(context: Context, projectId:String, fileName:String, bmp:Bitmap):String {
        var hash = ""
        if(context == null) {
            Log.d("blas", "contextがnullです")
            return hash
        }
        if(bmp == null) {
            Log.d("blas", "bmpがnullです")
            return hash
        }

        //画像ファイル名のフルパス作成
        try {
            val byteArrOutputStream = ByteArrayOutputStream()
            val fileOutputStream = FileOutputStream(fileName)
            bmp!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrOutputStream)
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

    /**
     * [説明]
     * ローカルの画像ファイルをBitmap形式で取得する
     */
    fun readBmpFromLocal(context: Context, projectId:String, fileName:String):Bitmap?{
        val dirName = getSavedDir(context, projectId)
        val fullPath = dirName + fileName
        var bmp:Bitmap? = null
        if(!File(fullPath).exists()) {
            return null
        }

        try{
            val InputStream = FileInputStream(fullPath)
            bmp = BitmapFactory.decodeStream(InputStream)

        }catch(e:IOException) {
            e.printStackTrace()
        }
        return bmp
    }

    /**
     * [説明]
     * ファイル名から拡張子を取得する
     */
    fun getExt(fileName:String):String {
        val imgFile = File(fileName).absoluteFile
        return imgFile.extension
    }

    /**
     * [説明]
     * bmpをbase64に変換する
     */
     fun encode(bitmap: Bitmap, ext: String?) : String? {
        if(ext == null) {
            return null
        }
        val format = FileExtensions.matchExtension(ext)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(format.compressFormat, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING   // 足し算されて11になる（bit加算）

        return Base64.encodeToString(byteArray, flag)
    }

}