package com.v3.basis.blas.blasclass.controller

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Images
import com.v3.basis.blas.blasclass.db.data.ItemImage
import com.v3.basis.blas.blasclass.db.data.Items
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.blasclass.ldb.LdbImageRecord
import com.v3.basis.blas.blasclass.rest.SyncBlasRestImage
import com.v3.basis.blas.ui.item.item_image.ItemImageCellItem
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ImageControllerException(val errorCode:Int, val msg:String): Exception(msg)

class ImagesController (context: Context, projectId: String): BaseController(context, projectId) {
    val cacheImagePath = context.dataDir.path + "/images/${projectId}/"
    companion object {
        val MINI_IMAGE = 0
        val ORIGINAL_IMAGE = 1

    }

    fun search(syncFlg:Boolean=true):MutableList<LdbImageRecord> {
        var sql = ""
        if(syncFlg) {
            sql = """select image_id, project_id, project_image_id, item_id, sync_status
                     from images where sync_status > 0
                   """
        }
        else {
            sql = """select image_id, project_id, project_image_id, item_id, sync_status
                     from images
                   """
        }
        val cursor = db?.rawQuery(sql, arrayOf<String>())

        val resultList = mutableListOf<LdbImageRecord>()
        cursor?.also { c_now ->
            var notLast = c_now.moveToFirst()
            while (notLast) {
                val image = LdbImageRecord()
                image.image_id = c_now.getLong(1)
                image.project_id = c_now.getInt(2)
                image.project_image_id = c_now.getInt(3)
                image.item_id = c_now.getLong(4)
                resultList.add(image)
                notLast = c_now.moveToNext()
            }
        }
        cursor?.close()

        return resultList
    }

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
    fun save2LDB(itemImage: ItemImage):Boolean{
        var ret = true
        val image = Images()
        val fileName = getFileName(itemImage.item_id.toString(), itemImage.project_image_id.toString(), ORIGINAL_IMAGE)

        if(itemImage.image_id == 0L) {
            //新規追加の場合 仮IDを発行する
            image.image_id = createTempId()
        }

        image.project_id = itemImage.project_id
        image.project_image_id = itemImage.project_image_id?.toInt()
        image.item_id = itemImage.item_id //仮IDが入ることがあるため
        image.filename = fileName //サーバーには送らないので、適当でよい
        image.create_date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        image.sync_status = SYNC_STATUS_NEW;//仮登録

        try {
            //SQL作成
            val cv = createConvertValue(image)
            db?.beginTransaction()
            //画像データの登録
            if(itemImage.image_id == 0L) {
                    //新規登録
                    db?.insert("images", null, cv)
            }
            else {
                //IDもあって、画像もあるので更新
                db?.update("images",cv, "image_id =?", arrayOf(itemImage.image_id.toString()))
            }
            db?.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
            ret = false
        }
        finally {
            db?.endTransaction()
        }

        itemImage.image_id = image.image_id

        return ret
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
            val sql = "select image_id, project_id, project_image_id, item_id, filename, hash, moved, create_date, sync_status from images where item_id=? and sync_status!=?"
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
                        create_date = c.getString(7),
                        sync_status = c.getInt(8)
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
        }

        return resultList
    }

    fun fixDeleteImage(imageId:String) {
        var imgFileName = ""

        //削除だった場合
        try{
            db?.beginTransaction()
            //レコード削除
            db?.delete("images", "image_id=?", arrayOf(imageId))
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

    /**
     * 仮登録されたレコードを本登録する
     */
    fun fixUploadImage(newImageId:String, tempImageId:String) {
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
        }

        return resultList
    }

    fun reserveDeleteImg(imageId:Long) : Boolean {
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

        return true
    }

    /**
     * 画像項目と各項目の画像フィールドを返す。
     * imagesレコードがない場合は、該当項目はNULLとなる。
     */
    fun getItemImages(itemId:String):MutableList<ItemImage> {
        val resultList:MutableList<ItemImage> = mutableListOf()
        try {
            db?.beginTransaction()
            val sql = """select
                            project_images.project_image_id,
                            project_images.project_id,
                            project_images.list,
                            project_images.field_id,
                            project_images.name,
                            project_images.rank,
                            images.image_id,
                            images.filename,
                            images.item_id,
                            images.moved,
                            images.create_date
                        from project_images 
                        left outer join (select * from images where item_id=?) AS images
                        on project_images.project_image_id = images.project_image_id where project_images.project_id=?
                        order by project_images.rank asc"""

            val cursor = db?.rawQuery(sql, arrayOf(itemId, projectId))

            cursor?.also { c->
                var notLast = cursor?.moveToFirst()
                while(notLast) {
                    val record = ItemImage(
                        project_image_id = c.getLong(0),
                        project_id = c.getInt(1),
                        list = c.getInt(2),
                        field_id = c.getInt(3),
                        name = c.getString(4),
                        rank = c.getInt(5),
                        image_id = c.getLong(6),
                        filename = c.getString(7),
                        item_id = c.getLong(8),
                        moved = c.getInt(9),
                        create_date = c.getString(10)
                    )
                    resultList.add(record)
                    //リストに追加する
                    notLast = c.moveToNext()
                }
            }
        }
        catch(e: Exception) {
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
        }
        return resultList
    }

    /**
     * BLASから表示用の小さな画像(230)を取得する。
     * ローカルにキャッシュがあれば、キャッシュを返却する。
     * キャッシュがない場合は、BLASからダウンロードする
     */
    fun getSmallImage(token:String, item_id:String, project_image_id:String):Bitmap? {
        var json:JSONObject? = null
        var bitmap:Bitmap? = null
        val payload = mutableMapOf<String, String>()

        payload["token"] = token
        payload["item_id"] = item_id
        payload["project_image_id"] = project_image_id

        //キャッシュファイルの読み込み
        bitmap = getBitmap(item_id, project_image_id, 0)
        if(bitmap == null) {
            //キャッシュファイルがない場合
            //画像のURLを取得する
            json = SyncBlasRestImage().getUrl(payload)
            if (json?.getInt("error_code") != 0) {
                val msg = json?.getString("message")
                Log.d("konishi", msg)
                return null
            }

            //画像をダウンロードする
            val jsonRecord = json?.getJSONArray("records").getJSONObject(0)
            val jsonImage = jsonRecord.getJSONObject("Image")
            val smallImagePath = jsonImage.getString("small_image")

            //smallImageUrlから画像をダウンロードする
            val smallImageUrl = URL(BuildConfig.HOST + smallImagePath)
            val imageInputStream = smallImageUrl.openStream()

            bitmap = BitmapFactory.decodeStream(imageInputStream)
            if(bitmap != null) {
                //画像を所定のパスに保存する
                saveBitmap(bitmap, item_id, project_image_id, MINI_IMAGE)
            }
        }

        return bitmap
    }

    /**
     * 画像のファイルの保存先ディレクトリを取得する
     * sizeTypeがMINI_IMAGEの場合、以下のパスを返却する
     * aplicationPath/images/project_id/item_id/230/
     * sizeTypeがORIGINAL_IMAGEの場合、以下のパスを返却する
     * aplicationPath/images/project_id/item_id/original/
     * */
    fun getDirName(itemId:String, sizeType:Int=MINI_IMAGE):String {
        var dirName = ""
        if(sizeType == 0) {
            dirName = cacheImagePath + "${itemId}/230/"
        }
        else {
            dirName = cacheImagePath + "${itemId}/original/"
        }
        return dirName
    }


    /**
     * 画像のファイルパスを取得する
     * sizeTypeがMINI_IMAGEの場合、以下のパスを返却する
     * aplicationPath/images/project_id/item_id/230/project_image_id.jpg
     * sizeTypeがORIGINAL_IMAGEの場合、以下のパスを返却する
     * aplicationPath/images/project_id/item_id/original/project_image_id.jpg
     */
    fun getFileName(itemId:String, projectImageId:String, sizeType:Int=MINI_IMAGE):String {
        var dirName = getDirName(itemId, sizeType)

        return dirName + "${projectImageId}.jpg"
    }

    /**
     * 画像を保存する
     * sizeTypeがMINI_IMAGEの場合、以下のパスに画像を保存する
     * aplicationPath/images/project_id/item_id/230/project_image_id.jpg
     * sizeTypeがORIGINAL_IMAGEの場合、以下のパスに画像を保存する
     * aplicationPath/images/project_id/item_id/original/project_image_id.jpg
     */
    fun saveBitmap(bmp:Bitmap, itemId:String, projectImageId:String, sizeType:Int=MINI_IMAGE) {
        var saveDirName = getDirName(itemId, sizeType)

        val saveDir = File(saveDirName)
        if(!saveDir.exists()) {
            saveDir.mkdirs()
        }

        //例外はコール元で拾ってください
        val saveFileName = getFileName(itemId, projectImageId, sizeType)
        val saveFile = File(saveFileName)
        FileOutputStream(saveFile).use{fs->
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fs)
        }
    }

    fun getBitmap(itemId:String, projectImageId:String, sizeType:Int=MINI_IMAGE):Bitmap? {
        var cacheDirName = getDirName(itemId, sizeType)

        val saveDir = File(cacheDirName)
        if(!saveDir.exists()) {
            return null
        }

        val cacheFileName = getFileName(itemId, projectImageId, sizeType)
        val cacheFile = File(cacheFileName)
        if(!cacheFile.exists()) {
            return null
        }

        var bitmap = BitmapFactory.decodeFile(cacheFileName)
        return bitmap
    }
}