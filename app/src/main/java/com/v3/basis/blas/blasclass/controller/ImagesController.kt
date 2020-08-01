package com.v3.basis.blas.blasclass.controller

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Images
import com.v3.basis.blas.ui.item.item_image.model.ItemImage
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.RuntimeException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class ImageControllerException(val errorCode:Int, val msg:String): Exception(msg)

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
//    fun save2LDB(bmp:Bitmap, projectImageId:String, itemId:String, imageId:String?=null, syncStatus:Int):Boolean{
    fun save2LDB(itemImage: ItemImage, syncStatus:Int):Boolean{
        val image = Images()
        var fileName = ""
        var isExists = false
        //保存用画像ファイル名を生成する
        if(itemImage.image_id == "") {
            //新規追加の場合
            image.image_id = createTempId()
            fileName = ImageComponent().createTmpImageFileName()
        }
        else {
            //更新の場合
            fileName = getImageFileName(itemImage.image_id)
            if(fileName != "") {
                //すでにLDBに保存されている場合はDBのファイル名を使う
                isExists = true
            }
            else {
                //保存されていない場合は、仮アファイル名を使う
                fileName = ImageComponent().createTmpImageFileName()
            }
            image.image_id = itemImage.image_id.toLong()
        }

        image.project_id = projectId.toInt()
        image.project_image_id = itemImage.project_image_id.toInt()
        image.item_id = itemImage.item_id.toLong() //仮IDが入ることがあるため
        //image.filename = fileName
        image.create_date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        image.sync_status = syncStatus;//仮登録

        itemImage.bitmap?.let {
            //画像ファイルを保存する
            image.hash = ImageComponent().saveBmp2Local(context, projectId, fileName, it)
        }
        image.filename = fileName

        return try {
            //SQL作成
            val cv = createConvertValue(image)
            db?.beginTransaction()
            if(itemImage.image_id != "") {
                if(!isExists) {
                    //新規登録 または　ダウンロード直後
                    db?.insert("images", null, cv)
                }
                else {
                    //IDもあって、画像もあるので更新
                    db?.update("images",cv, "image_id =?", arrayOf(itemImage.image_id))
                }
            }
            else {
                db?.insert("images", null, cv)
            }

            db?.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        finally {
            db?.endTransaction()
        }
    }

    /**
     * [説明]
     * 指定されたimageIdのファイル名をimagesテーブルから検索して返却する
     */
    fun getImageFileName(imageId:String):String {
        var fileName = ""
        try {
            db?.beginTransaction()
            val sql = "select filename from images where image_id=?"
            val cursor = db?.rawQuery(sql, arrayOf(imageId))
            if (cursor != null && cursor?.count > 0) {
                cursor?.moveToFirst()
                fileName = cursor?.getString(0)
            }
        }
        catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
        }
        return fileName
    }


    /**
     * [説明]
     * itemIdとproject_image_idをキーにローカルDBから画像を探す
     */
    fun searchFromLocal(context:Context, itemId:String, projectImageId:String): Pair<Bitmap, Long> {
        var fileName:String
        var bmp:Bitmap? = null
        var image_id:Long = -1

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
            if(itemId.toLong() < 0) {
                //ローカルに画像がなく、かつ、データIDが仮IDなので、リモートに画像がないのは明白。
                throw ImageControllerException(1, "リモートに画像はありません")
            }
            else {
                //例外投げる
                throw ImageControllerException(2, "リモートサーバに確認必要")
            }
        }
        else {
            return Pair(bmp, image_id)
        }
    }

}