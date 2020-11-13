package com.v3.basis.blas.blasclass.controller

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.android.datatransport.runtime.util.PriorityMapping.toInt
import com.tonyodev.fetch2core.getFile
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.blasclass.ldb.LdbImageQueueRecord
import com.v3.basis.blas.blasclass.ldb.LdbImageRecord
import com.v3.basis.blas.blasclass.ldb.LdbItemImageRecord
import com.v3.basis.blas.blasclass.rest.SyncBlasRestImage
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
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
                image.image_id = c_now.getLong(0)
                image.project_id = c_now.getInt(1)
                image.project_image_id = c_now.getInt(2)
                image.item_id = c_now.getLong(3)
                resultList.add(image)
                notLast = c_now.moveToNext()
            }
        }
        cursor?.close()

        return resultList
    }

    /*
    fun queueInit():Boolean {
        var ret = true
        val sql = """
            create table if not exists imageQueue
            (
                id integer primary key  autoincrement,
                image_id integer,
                item_id integer,
                project_image_id integer,
                filename text,
                retry_count integer,
                message text,
                error_count integer
            )"""
        try {
            db?.execSQL(sql)
        }
        catch(e:java.lang.Exception) {
            e.printStackTrace()
            ret = false
        }

        return ret
    }

    fun queueAdd(imageId:String, itemId:String, projectImageId:String, fileName:String):Boolean{
        var ret = true
        val values = ContentValues()
        values.put("image_id", imageId)
        values.put("item_id", itemId)
        values.put("projectImageId", projectImageId)
        values.put("filename", fileName)
        values.put("retry_count", 0)
        values.put("message", "")
        values.put("error_count", 0)

        if(!queueInit()) {
            ret = false
            Log.d("konishi", "Queueの初期化に失敗しました")
            return ret
        }

        try {
            db?.beginTransaction()
            db?.insert("ImageQueue",null,values)
            db?.setTransactionSuccessful()
            Log.d("konishi","imageQueue登録")
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            Log.d("konishi", e.message)
            e.printStackTrace()
            ret = false
        }
        finally {
            db?.endTransaction()
        }
        return ret
    }

    fun getQueue():MutableList<LdbImageQueueRecord>{
        val resultList = mutableListOf<LdbImageQueueRecord>()
        val sql = """select id, 
|                           image_id,
|                           item_id,
|                           project_image_id,
|                           filename,
|                           retry_count,
|                           message,
|                           error_code
|                    from ImageQueue"""

        val cursor = db?.rawQuery(sql, arrayOf())

        if( 0 == cursor?.count ) {
            return resultList
        }

        cursor?.also { c_now ->
            var notLast = c_now.moveToFirst()
            while (notLast) {
                val record = LdbImageQueueRecord()
                record.id = c_now.getInt(0)
                record.image_id = c_now.getLong(1)
                record.item_id = c_now.getLong(2)
                record.project_image_id = c_now.getInt(3)
                record.filename = c_now.getString(4)
                record.retry_count = c_now.getInt(5)
                record.message = c_now.getString(6)
                record.error_code = c_now.getInt(7)
                resultList.add(record)
                notLast = c_now.moveToNext()
            }
        }
        cursor?.close()

        return resultList
    }

    fun queueDel(id:Int):Boolean{
        var ret = true

        try {
            db?.beginTransaction()
            db?.delete("ImageQueue",
                "id=?",
                       arrayOf<String>(id.toString()))

            db?.setTransactionSuccessful()

            Log.d("konishi","imageQueue登録")
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            Log.d("konishi",e.message)
            e.printStackTrace()
            ret = false
        }
        finally {
            db?.endTransaction()
        }
        return ret
    }
*/
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
    fun save2LDB(itemImage: LdbItemImageRecord):Pair<Boolean, Long>{
        var ret = true
        var lastId = 0L
        val fileName = getFileName(itemImage.item_id.toString(), itemImage.project_image_id.toString(), ORIGINAL_IMAGE)
        if(itemImage.image_id == null) {
            return Pair(false, 0)
        }

        itemImage.image_id?.let {
            lastId = it
        }

        try {
            //SQL作成
            db?.beginTransaction()
            //画像データの登録
            if(itemImage.image_id == 0L) {
                //新規登録
                val cv = ContentValues()
                lastId = createTempId()
                cv.put("image_id", lastId)
                cv.put("project_id", itemImage.project_id)
                cv.put("project_image_id", itemImage.project_image_id)
                cv.put("item_id", itemImage.item_id)
                cv.put("filename", fileName)
                cv.put("create_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
                cv.put("sync_status", itemImage.sync_status)

                db?.insert("images", null, cv)
            }
            else {
                //IDもあって、画像もあるので更新
                val cv = ContentValues()
                cv.put("filename", fileName)
                cv.put("create_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
                cv.put("sync_status", itemImage.sync_status)

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


        return Pair(ret, lastId)
    }

    /**
     * 仮IDをサーバーから取得した正しいIDに直す
     */
    fun updateImageId( oldImageId:String, newImageId:String) : Boolean {
        var ret = true
        val cv = ContentValues()
        cv.put("image_id",newImageId)
        cv.put("sync_status", SYNC_STATUS_SYNC)
        cv.put("error_msg", "")

        try {
            db?.beginTransaction()
            db?.update("images",cv,"image_id = ?", arrayOf(oldImageId))
            db?.setTransactionSuccessful()
            db?.endTransaction()
            Log.d("update","成功！！")
        }
        catch ( ex : Exception ) {
            ex.printStackTrace()
            ret = false
        }

        return ret
    }

    /**
     * sqliteのsync_status(同期状況)を0(何もなし)に戻す
     */
    fun resetSyncStatus( imageId:String ) : Boolean {

        val cv = ContentValues()
        cv.put("sync_status", SYNC_STATUS_SYNC)

        return try {
            db?.beginTransaction()
            db?.update("images",cv,"image_id = ?", arrayOf(imageId))
            db?.setTransactionSuccessful()
            db?.endTransaction()
            Log.d("sync_status reset","成功！！")
            true
        }
        catch ( ex : Exception ) {
            ex.printStackTrace()
            false
        }
    }

    fun setErrorMsg( fixtureId: String,errMsg : String ) {

        val cv = ContentValues()
        cv.put("error_msg",errMsg)

        return try {
            db?.beginTransaction()
            db?.update("fixtures",cv,"fixture_id = ?", arrayOf(fixtureId))
            db?.setTransactionSuccessful()
            db?.endTransaction()!!
        }
        catch ( ex : Exception ) {
            ex.printStackTrace()
            throw ex
        }
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
    fun searchNosyncRecord(itemId:Long):MutableList<LdbImageRecord>{
        //item_idとsync_statusが0以外の画像レコードを探す
        var resultList:MutableList<LdbImageRecord> = mutableListOf()
        try {
            db?.beginTransaction()
            val sql = "select image_id, project_id, project_image_id, item_id, filename, hash, moved, create_date, sync_status from images where item_id=? and sync_status!=?"
            val cursor = db?.rawQuery(sql, arrayOf(itemId.toString(), SYNC_STATUS_SYNC.toString()))
            cursor?.also { c->
                var notLast = cursor?.moveToFirst()
                while(notLast) {
                    val image_record = LdbImageRecord()
                    image_record.image_id = c.getLong(0)
                    image_record.project_id = c.getInt(1)
                    image_record.project_image_id = c.getInt(2)
                    image_record.item_id = c.getLong(3)
                    image_record.filename = c.getString(4)
                    image_record.hash = c.getString(5)
                    image_record.moved = c.getInt(6)
                    image_record.create_date = c.getString(7)
                    image_record.sync_status = c.getInt(8)

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
    /*
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
    */
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

    fun getImageStatus(imageId:String):Int {
        var retStatus = SYNC_STATUS_SYNC
        val sql = "select sync_status from images where image_id=?"
        try {
            db?.beginTransaction()
            val cursor = db?.rawQuery(sql, arrayOf(imageId))
            cursor?.also { c ->
                var notLast = cursor?.moveToFirst()
                if (notLast) {
                    retStatus = c.getInt(0)
                }
            }
        }
        catch(e:java.lang.Exception) {
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
        }
        return retStatus
    }
    /**
     * 画像項目と各項目の画像フィールドを返す。
     * imagesレコードがない場合は、該当項目はNULLとなる。
     */
    fun getItemImages(itemId:String):MutableList<LdbItemImageRecord> {
        val resultList:MutableList<LdbItemImageRecord> = mutableListOf()
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
                            images.create_date,
                            images.sync_status,
                            images.error_msg
                        from project_images 
                        left outer join (select * from images where item_id=?) AS images
                        on project_images.project_image_id = images.project_image_id where project_images.project_id=?
                        order by project_images.rank asc"""

            val cursor = db?.rawQuery(sql, arrayOf(itemId, projectId))

            cursor?.also { c->
                var notLast = cursor?.moveToFirst()
                while(notLast) {
                    val record = LdbItemImageRecord(
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
                        create_date = c.getString(10),
                        sync_status = c.getInt(11),
                        error_msg = c.getString(12)
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

    /**
     * ファイル名から画像を取得する
     */
    fun getBase64File(fileName:String):String{
        var base64Img = ""
        try{
            val file = File(fileName)
            FileInputStream(file).use {
                val bytes = ByteArray(file.length().toInt())
                it.read(bytes)
                val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                base64Img = Base64.encodeToString(bytes, flag)
            }
        }catch(e:Exception) {
            e.printStackTrace()
        }

        return base64Img
    }

    /**
     * IDからファイル名を特定してBase64の画像ファイルを返す
     */
    fun getBase64File(itemId:String, projectImageId:String, sizeType:Int):String{
        var base64Img = ""
        val fileName = getFileName(itemId, projectImageId, sizeType)
        try{
            val file = File(fileName)
            FileInputStream(file).use {
                val bytes = ByteArray(file.length().toInt())
                it.read(bytes)
                val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                base64Img = Base64.encodeToString(bytes, flag)
            }
        }catch(e:Exception) {
            e.printStackTrace()
        }

        return base64Img
    }

    fun getExtNumber(itemId:String, projectImageId:String):Int {
        val fileName = getFileName(itemId, projectImageId)
        val ext = File(fileName).extension.toLowerCase()
        var value = 0
        if((ext == "jpg") or (ext == "jpeg")) {
            value = 0
        }
        else if(ext == "png") {
            value = 1
        }
        else if(ext == "gif") {
            value = 2
        }
        else {
            Log.d("konishi", "拡張子が不正")
            value = 0
        }

        return value


    }
}