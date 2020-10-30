package com.v3.basis.blas.blasclass.component

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * 図面の画像データを扱うコンポーネントクラス。
 */
class DrawingImageComponent: ImageComponent() {
    private val TAG="DrawingImageComponent"

    /**
     * 画像データの保存先のファイルパスを取得する。
     * @param context コンテキスト
     * @param projectId プロジェクトID
     * @return 保存先のファイルパス
     */
    override fun getSavedDir(context: Context, projectId:String):String {
        return context.dataDir.path + "/drawings/${projectId}/"
    }

    /**
     * ローカルストレージに画像データを保存する。
     * @param context コンテキスト
     * @param projectId プロジェクトID
     * @param fileName 保存するファイル名
     * @param bmp 保存する画像データ
     * @return 画像データのMD5ハッシュ文字列。エラーの場合は空文字列が設定される。
     */
    override fun saveBmp2Local(context: Context, projectId:String, fileName:String, bmp: Bitmap):String {
        var hash = ""
        if(context == null) {
            Log.d(TAG, "saveBmp2Local: contextがnullです")
            return hash
        }
        if(bmp == null) {
            Log.d(TAG, "saveBmp2Local : bmpがnullです")
            return hash
        }

        //一時ディレクトリ作成
        createTmpImageDir(context, projectId)
        val imgFullPath = getSavedDir(context, projectId) + fileName
        //画像ファイル名のフルパス作成
        try {
            ByteArrayOutputStream().use { baos ->
                FileOutputStream(imgFullPath).use { fos ->
                    bmp!!.compress(getCompressFormat(fileName), 100, baos)
                    fos.write(baos.toByteArray())
                    val digest: MessageDigest = MessageDigest.getInstance("MD5")
                    hash = digest.digest(baos.toByteArray()).joinToString(separator = "") {
                        "%02x".format(it)
                    }
                }
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }

        return hash
    }

    /**
     * ファイル名から拡張子を判定し、対応するBitmap.CompressFormatタイプを返す。
     * 対応フォーマットは、jpeg,pngおよびwebpとし、それ以外はjpegとして扱う。
     * @param filename ファイル名
     * @return 対応するBitmap.CompressFormatタイプ
     */
    private fun getCompressFormat(filename: String): Bitmap.CompressFormat {
        return when(getExt(filename).toUpperCase()){
            "JPEG" -> Bitmap.CompressFormat.JPEG
            "JPG" -> Bitmap.CompressFormat.JPEG
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> Bitmap.CompressFormat.WEBP
            else -> {
                Log.d(TAG, "This extension is not supported: filename=${getExt(filename)}")
                Bitmap.CompressFormat.JPEG
            }
        }
    }
}