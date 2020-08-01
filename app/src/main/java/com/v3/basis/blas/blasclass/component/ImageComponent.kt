package com.v3.basis.blas.blasclass.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.*
import java.security.MessageDigest
import java.util.*

class ImageComponent {

    /**
     * [説明]
     * 画像のフルパスを取得する
     */
    fun getSavedDir(context: Context, projectId:String):String {
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
    fun saveBmp2Local(context: Context, projectId:String, fileName:String, bmp:Bitmap):String {
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
            bmp!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrOutputStream)
            fileOutputStream.write(byteArrOutputStream.toByteArray())
            val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
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
     * ローカルの画像ファイルを読み込む
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
}