package com.v3.basis.blas.blasclass.controller

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Images
import com.v3.basis.blas.blasclass.db.data.Items
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
            //画像データの登録
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
            //データ管理のsync_statusを変更する
            if(syncStatus == SYNC_STATUS_NEW) {
                val item = Items()
                item.item_id = itemImage.item_id.toLong()
                item.sync_status = syncStatus
                //val cvItem = createConvertValue(item)
                //ここがバグ。project_idとかが全部すっ飛んでしまう
                val cvItem = ContentValues()
                cvItem.put("sync_status", SYNC_STATUS_NEW)
                db?.update("items",cvItem, "item_id =?", arrayOf(itemImage.item_id))
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
        var syncStatus = SYNC_STATUS_SYNC
        try {
            db?.beginTransaction()
            val sql = "select image_id, filename, sync_status from images where project_id=? and item_id=? and project_image_id=?"
            val cursor = db?.rawQuery(sql, arrayOf(projectId, itemId, projectImageId))
            if(cursor != null && cursor?.count > 0) {
                //画像レコードがあった場合
                cursor?.moveToFirst()
                image_id = cursor?.getLong(0)
                fileName = cursor?.getString(1) ?: ""
                syncStatus = cursor?.getInt(2)
                if(syncStatus == SYNC_STATUS_DEL) {
                    bmp = null
                }
                else {
                    bmp = ImageComponent().readBmpFromLocal(context, projectId, fileName)
                }
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
            if(syncStatus == SYNC_STATUS_DEL) {
                throw ImageControllerException(3, "削除中です")
            }
            else if(itemId.toLong() < 0) {
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

    /**
     * [説明]
     * 指定されたデータIDのレコードのうち、サーバと同期していない画像レコードを返却する
     */
    fun searchNosyncRecord(itemId:Long):MutableList<Images>{
        //item_idとsync_statusが0以外の画像レコードを探す
        var resultList:MutableList<Images> = mutableListOf()
        try {
            db?.beginTransaction()
            val sql = "select image_id, project_id, project_image_id, item_id, filename, hash, moved, create_date from images where item_id=? and sync_status!=?"
            val cursor = db?.rawQuery(sql, arrayOf(itemId.toString(), SYNC_STATUS_SYNC.toString()))
            cursor?.also { c->
                var notLast = cursor?.moveToFirst()
                while(notLast) {
                    val image_record = Images(
                        image_id = c.getLong(0),
                        project_id = c.getInt(1),
                        project_image_id = c.getInt(2),
                        item_id = c.getLong(3),
                        filename = c.getString(4),
                        hash = c.getString(5),
                        moved = c.getInt(6),
                        create_date = c.getString(7)
                    )
                    resultList.add(image_record)
                    //リストに追加する
                    notLast = c.moveToNext()
                }
            }
        }
        catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
            db?.close()
        }

        return resultList
    }

    /**
     * 仮登録されたレコードを本登録する
     */
    fun fixImageRecord(newImageId:String, tempImageId:String) {
        var syncStatus = -1
        var imgFileName = ""

        //ここで削除か新規追加、更新のどちかを調べる必要がある。
        //tempImageIdで画像を検索して、ステータスを取得するしかない。
        try{
            db?.beginTransaction()
            val sql = "select filename, sync_status from images where image_id=?"
            //ID変更前のレコードのステータスを取得する
            val cursor = db?.rawQuery(sql, arrayOf(tempImageId))
            cursor?.also{c->
                c.moveToFirst()
                imgFileName = c.getString(0)
                syncStatus = c.getInt(1)
            }
        }
        catch(e:Exception) {
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
        }

        if(syncStatus == SYNC_STATUS_DEL) {
            //削除だった場合
            try{
                db?.beginTransaction()
                //レコード削除
                db?.delete("images", "image_id=?", arrayOf(tempImageId.toString()))
                //画像ファイルを削除する
                ImageComponent().delImgFile(context, projectId, imgFileName)
                //レコードと画像の両方を削除できたら成功
                db?.setTransactionSuccessful()!!
            }
            catch (e:Exception) {
                e.printStackTrace()
                throw e
            }
            finally {
                db?.endTransaction()
            }
        }
        else {
            //新規追加、または更新の場合
            try {
                val cv = ContentValues()
                cv.put("image_id", newImageId)
                cv.put("sync_status", SYNC_STATUS_SYNC)
                db?.beginTransaction()
                db?.update("images", cv, "image_id=?", arrayOf(tempImageId.toString()))
                db?.setTransactionSuccessful()!!
            }
            catch (e:Exception) {
                e.printStackTrace()
                throw e
            }
            finally {
                db?.endTransaction()
            }
        }
        //itemテーブルのステータスは、本登録したときにitemのデータも送信しているため
        //SyncItem内でsync_statusは登録完了になっている。
    }

    /**
     * [説明]
     * 同期されていない画像のリストを返す
     * [備考]
     * 数万件あったらどうしようか
     */
    fun searchNosyncRecords():List<Images>{
        var resultList:MutableList<Images> = mutableListOf()
        try {
            db?.beginTransaction()
            val sql = "select image_id, project_id, project_image_id, item_id, filename, hash, moved, create_date from images where sync_status!=?"
            //where sync_status!=?はnot equealに注意
            val cursor = db?.rawQuery(sql, arrayOf(SYNC_STATUS_SYNC.toString()))
            cursor?.also { c->
                var notLast = cursor?.moveToFirst()
                while(notLast) {
                    val image_record = Images(
                        image_id = c.getLong(0),
                        project_id = c.getInt(1),
                        project_image_id = c.getInt(2),
                        item_id = c.getLong(3),
                        filename = c.getString(4),
                        hash = c.getString(5),
                        moved = c.getInt(6),
                        create_date = c.getString(7)
                    )
                    resultList.add(image_record)
                    //リストに追加する
                    notLast = c.moveToNext()
                }
            }
        }
        catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
            db?.close()
        }

        return resultList
    }

    fun deleteImageLocal(imageId:Long) {
        //画像管理テーブルを削除中に変更する。
        //実際の画像ファイル削除は本登録完了後に行う
        try {
            db?.beginTransaction()
            val sql = "select item_id from images where image_id=?"
            val cursor = db?.rawQuery(sql, arrayOf(imageId.toString()))
            cursor?.moveToFirst()
            cursor?.let {
                val itemId = it.getString(0)
                val cvItem = ContentValues()
                cvItem.put("sync_status", SYNC_STATUS_EDIT)
                //データ管理テーブルを更新中に変更する
                db?.update("items",cvItem, "item_id =?", arrayOf(itemId))
                //画像管理テーブルを削除中に変更する
                val cvImage = ContentValues()
                cvImage.put("sync_status", SYNC_STATUS_DEL)
                db?.update("images",cvImage, "image_id =?", arrayOf(imageId.toString()))
            }
            db?.setTransactionSuccessful()!!
        }
        catch(e: Exception) {
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
        }
    }
}