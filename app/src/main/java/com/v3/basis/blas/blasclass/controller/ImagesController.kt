package com.v3.basis.blas.blasclass.controller

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Images
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImagesController (context: Context, projectId: String): BaseController(context, projectId) {
    //ここから下は画像関係
    /**
     * [説明]
     * 画像をLDBに新規追加する
     * [引数]
     * projectId:プロジェクトID 文字列で渡す
     * projectImageId: projectImagesテーブルのPK
     * itemId: データ管理のPK。仮IDの場合もある
     * fileName: 画像ファイルのフルパス名。
     * imageId: imagesテーブルの主キー。nullの場合は新規追加、null以外の場合は更新する
     */
    fun upload2LDB(projectImageId:String, itemId:String, fileName:String, imageId:String?=null):Boolean{
        val image = Images()
        //保存用レコード作成
        if(imageId == null) {
            image.image_id = createTempId()
        }
        else {
            image.image_id = imageId.toLong()
        }

        image.project_id = projectId.toInt()
        image.project_image_id = projectImageId.toInt()
        image.item_id = itemId.toLong() //仮IDが入ることがあるため
        image.filename = fileName
        image.create_date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        image.sync_status = SYNC_STATUS_NEW;//仮登録
        //画像ファイルを開いてmd5のハッシュ値を取得する
        /* val digest: MessageDigest = MessageDigest.getInstance("MD-5")
         var hash = digest.digest(FileInputStream(File(fileName)).readBytes()).joinToString(separator = "") {
             "%02x".format(it)
         }
         image.hash = hash //あってるのか？
 */
        //LDBに保存する
        val cv = createConvertValue(image)

        return try {
            db?.beginTransaction()

            // itemテーブルに追加
            db?.insert("images",null,cv)

            db?.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
        finally {
            db?.endTransaction()
        }
    }

    /**
     * ここでローカルの画像を読み込めばよいのでは？
     */
    fun searchFromLocal(context:Context, itemId:String, projectImageId:String): Pair<Bitmap, Long> {
        //この関数は
        //ローカルDBから画像を探す
        //IDは何で検索するか
        //itemIdとproject_image_idで検索
        var fileName:String
        var bmp:Bitmap? = null
        var image_id:Long = 0
        try {
            db?.beginTransaction()
            val sql = "select image_id, filename from images where project_id=? and item_id=? and project_image_id=?"
            val cursor = db?.rawQuery(sql, arrayOf(projectId, itemId, projectImageId))
            if(cursor != null && cursor?.count > 0) {
                //画像レコードがあった場合
                cursor?.moveToFirst()
                image_id = cursor?.getLong(0)
                fileName = cursor?.getString(1) ?: ""
                bmp = ImageComponent().readBmpFromLocal(context, projectId, fileName)
            }
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
            db?.close()
        }

        if(bmp == null) {
            //例外投げる
            throw IOException("image is not exists")
            //return Pair(bmp, image_id)
        }
        else {
            return Pair(bmp, image_id)
        }
    }
}