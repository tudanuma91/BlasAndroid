package com.v3.basis.blas.blasclass.controller

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.ldb.LdbImageRecord
import com.v3.basis.blas.blasclass.ldb.LdbItemImageRecord
import com.v3.basis.blas.blasclass.log.BlasLog
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
        val SMALL_IMAGE = 0
        val BIG_IMAGE = 1

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
        var cursor: Cursor? = null
        val resultList = mutableListOf<LdbImageRecord>()
        try {
            //SQL作成
            db?.beginTransaction()
            cursor = db?.rawQuery(sql, arrayOf<String>())

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
        }
        catch(e:java.lang.Exception) {
            e.printStackTrace()
        }
        finally {
            cursor?.close()
            db?.endTransaction()
        }

        return resultList
    }

    fun search(itemId:String, projectImageId:String):LdbImageRecord? {
        var sql = ""
        var imageRecord:LdbImageRecord? = null
        sql = """select image_id, project_id, project_image_id, item_id, sync_status
                 from images where item_id=? and project_image_id=?
              """
        var cursor: Cursor? = null

        try {
            db?.beginTransaction()

            cursor = db?.rawQuery(sql, arrayOf<String>(itemId, projectImageId))
            //何故ここがNULLになる？ダウンロードした直後なら、本データは保存していないのでNULLになる。
            cursor?.also { c_now ->
                var notLast = c_now.moveToFirst()
                while (notLast) {
                    val image = LdbImageRecord()
                    image.image_id = c_now.getLong(0)
                    image.project_id = c_now.getInt(1)
                    image.project_image_id = c_now.getInt(2)
                    image.item_id = c_now.getLong(3)
                    imageRecord = image
                    notLast = c_now.moveToNext()
                }
            }
            cursor?.close()
        }
        catch(e:Exception) {
            e.printStackTrace()
        }
        finally {
            db?.endTransaction()
        }

        return imageRecord
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
    fun save2LDB(itemImage: LdbItemImageRecord):Pair<Boolean, Long>{
        var ret = true
        var lastId = 0L

        val fileName = getFileName(itemImage.item_id.toString(), itemImage.project_image_id.toString(), BIG_IMAGE)
        if(itemImage.image_id == null) {
            return Pair(false, 0)
        }

        //バックグランドで更新されていないか、再度確認
        val dupCheckRecord = search(itemImage.item_id.toString(), itemImage.project_image_id.toString())
        if(dupCheckRecord != null) {
            val record = dupCheckRecord
            itemImage.image_id = record.image_id
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

                Log.d("send", cv.toString())
                val newId = db?.insertOrThrow("images", null, cv)
                Log.d("send", "newId:${newId}")
            }
            else {
                //IDもあって、画像もあるので更新
                val cv = ContentValues()
                cv.put("filename", fileName)
                cv.put("create_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
                cv.put("sync_status", itemImage.sync_status)

                db?.update("images",cv, "item_id=? and project_image_id=?",
                           arrayOf(itemImage.item_id.toString(),itemImage.project_image_id.toString()))
                //db?.update("images",cv, "image_id =?", arrayOf(99.toString()))
            }

            db?.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.d("send", "insert or update error")
            Log.d("send", e.message)
            e.printStackTrace()
            ret = false
        }
        finally {
            db?.endTransaction()
        }


        return Pair(ret, lastId)
    }

    fun save2LDB(image: LdbImageRecord):Pair<Boolean, Long>{
        var ret = true
        var lastId = 0L
        val fileName = getFileName(image.item_id.toString(), image.project_image_id.toString(), BIG_IMAGE)
        if(image.image_id == null) {
            return Pair(false, 0)
        }

        //バックグランドで更新されていないか、再度確認
        val dupCheckRecord = search(image.item_id.toString(), image.project_image_id.toString())
        if(dupCheckRecord != null) {
            val record = dupCheckRecord
            image.image_id = record.image_id
        }

        image.image_id?.let {
            lastId = it
        }

        try {
            //SQL作成
            db?.beginTransaction()
            //画像データの登録
            if(image.image_id == 0L) {
                //新規登録
                val cv = ContentValues()
                lastId = createTempId()
                cv.put("image_id", lastId)
                cv.put("project_id", image.project_id)
                cv.put("project_image_id", image.project_image_id)
                cv.put("item_id", image.item_id)
                cv.put("filename", fileName)
                cv.put("create_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
                cv.put("sync_status", image.sync_status)

                db?.insert("images", null, cv)
            }
            else {
                //IDもあって、画像もあるので更新
                val cv = ContentValues()
                cv.put("filename", fileName)
                cv.put("create_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
                cv.put("sync_status", image.sync_status)

                db?.update("images",cv, "item_id=? and project_image_id=?",
                    arrayOf(image.item_id.toString(),image.project_image_id.toString()))
                //db?.update("images",cv, "image_id =?", arrayOf(99.toString()))
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
        cv.put("error_status", NETWORK_NORMAL)

        try {
            db?.beginTransaction()
            db?.update("images",cv,"image_id = ?", arrayOf(oldImageId))
            db?.setTransactionSuccessful()
            db?.endTransaction()
        }
        catch ( ex : Exception ) {
            BlasLog.trace("E", "imagesテーブルの更新に失敗しました", ex)
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

    fun updateImageRecordStatus(image_id:String?, errorStatus:Int?, sendCnt:Int?, errorMsg:String) {
        if(image_id == null){
            return
        }

        if(errorStatus == null){
            return
        }

        if(sendCnt == null) {
            return
        }

        val cv = ContentValues()
        cv.put("error_status", errorStatus)
        cv.put("send_cnt", sendCnt)
        cv.put("error_msg", errorMsg)

        try {
            db?.beginTransaction()

            db?.update("images", cv, "image_id = ?", arrayOf(image_id))

            db?.setTransactionSuccessful()!!
        }
        catch (e: Exception) {
            BlasLog.trace("E", "レコードの更新に失敗しました", e)
        }
        finally {
            db?.endTransaction()
        }
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
                        and project_images.list=1
                        order by project_images.rank asc"""

            val cursor = db?.rawQuery(sql, arrayOf(itemId, projectId))

            cursor?.also { c->
                var notLast = cursor?.moveToFirst()
                while(notLast) {
                    val record = LdbItemImageRecord()
                    record.project_image_id = c.getLong(0)
                    record.project_id = c.getInt(1)
                    record.list = c.getInt(2)
                    record.field_id = c.getInt(3)
                    record.name = c.getString(4)
                    record.rank = c.getInt(5)
                    record.image_id = c.getLong(6)
                    record.filename = c.getString(7)
                    record.item_id = c.getLong(8)
                    record.moved = c.getInt(9)
                    record.create_date = c.getString(10)
                    record.sync_status = c.getInt(11)
                    record.error_msg = c.getString(12)
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

    fun getImage(token:String, item_id:String, project_image_id:String, size:Int=SMALL_IMAGE):Pair<Bitmap?, Long> {
        var json:JSONObject? = null
        var bitmap:Bitmap? = null
        val payload = mutableMapOf<String, String>()
        var imageId = 0L

        payload["token"] = token
        payload["item_id"] = item_id
        payload["project_image_id"] = project_image_id

        //キャッシュファイルの読み込み
        bitmap = getCacheBitmap(item_id, project_image_id, size)
        if(bitmap == null) {
            //キャッシュファイルがない場合
            //画像のURLを取得する
            if(size == SMALL_IMAGE) {
                json = SyncBlasRestImage().download230(payload)
            }
            else {
                json = SyncBlasRestImage().download(payload)
            }
            if (json?.getInt("error_code") != 0) {
                val msg = json?.getString("message")
                Log.d("konishi", msg)
                return Pair(null, -1)
            }

            //画像をダウンロードする
            val jsonRecord = json?.getJSONArray("records").getJSONObject(0)
            val jsonImage = jsonRecord.getJSONObject("Image")
            var base64Img = jsonImage.getString("image")

            val bytes = SyncBlasRestImage().decodeBase64(base64Img)
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size )
            if(bitmap != null) {
                //画像を所定のパスに保存する
                saveBitmap(bitmap, item_id, project_image_id, size)
            }
        }

        //imageIdが0Lのときは、キャッシュから読み込んだとき。
        return Pair(bitmap, imageId)
    }

    /**
     * BLASから画像を取得する。
     * ローカルにキャッシュがあれば、キャッシュを返却する。
     * キャッシュがない場合は、BLASからダウンロードする
     * ダウンロードした画像はキャッシュする点に注意
     */
    fun getImageOld(token:String, item_id:String, project_image_id:String, size:Int=SMALL_IMAGE):Pair<Bitmap?, Long> {
        var json:JSONObject? = null
        var bitmap:Bitmap? = null
        val payload = mutableMapOf<String, String>()
        var imageId = 0L

        payload["token"] = token
        payload["item_id"] = item_id
        payload["project_image_id"] = project_image_id

        //キャッシュファイルの読み込み
        bitmap = getCacheBitmap(item_id, project_image_id, size)
        if(bitmap == null) {
            //キャッシュファイルがない場合
            //画像のURLを取得する
            json = SyncBlasRestImage().getUrl(payload)
            if (json?.getInt("error_code") != 0) {
                val msg = json?.getString("message")
                Log.d("konishi", msg)
                return Pair(null, -1)
            }

            //画像をダウンロードする
            val jsonRecord = json?.getJSONArray("records").getJSONObject(0)
            val jsonImage = jsonRecord.getJSONObject("Image")
            var ImagePath = ""
            if(size == SMALL_IMAGE) {
                ImagePath = jsonImage.getString("small_image")
            }
            else {
                ImagePath = jsonImage.getString("image")
            }
            imageId = jsonImage.getLong("image_id")
            //smallImageUrlから画像をダウンロードする
            val ImageUrl = URL(BuildConfig.HOST + ImagePath)
            val imageInputStream = ImageUrl.openStream()

            bitmap = BitmapFactory.decodeStream(imageInputStream)
            if(bitmap != null) {
                //画像を所定のパスに保存する
                saveBitmap(bitmap, item_id, project_image_id, size)
            }
        }

        //imageIdが0Lのときは、キャッシュから読み込んだとき。
        return Pair(bitmap, imageId)
    }

    /**
     * 画像のファイルの保存先ディレクトリを取得する
     * sizeTypeがSMALL_IMAGEの場合、以下のパスを返却する
     * aplicationPath/images/project_id/item_id/230/
     * sizeTypeがBIG_IMAGEの場合、以下のパスを返却する
     * aplicationPath/images/project_id/item_id/original/
     * */
    fun getDirName(itemId:String, sizeType:Int=SMALL_IMAGE):String {
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
     * sizeTypeがSMALL_IMAGEの場合、以下のパスを返却する
     * aplicationPath/images/project_id/item_id/230/project_image_id.jpg
     * sizeTypeがORIGINAL_IMAGEの場合、以下のパスを返却する
     * aplicationPath/images/project_id/item_id/original/project_image_id.jpg
     */
    fun getFileName(itemId:String, projectImageId:String, sizeType:Int=SMALL_IMAGE):String {
        var dirName = getDirName(itemId, sizeType)

        return dirName + "${projectImageId}.jpg"
    }

    /**
     * 画像を保存する
     * sizeTypeがSMALL_IMAGEの場合、以下のパスに画像を保存する
     * aplicationPath/images/project_id/item_id/230/project_image_id.jpg
     * sizeTypeがORIGINAL_IMAGEの場合、以下のパスに画像を保存する
     * aplicationPath/images/project_id/item_id/original/project_image_id.jpg
     */
    fun saveBitmap(bmp:Bitmap, itemId:String, projectImageId:String, sizeType:Int=SMALL_IMAGE) {
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

    fun getCacheBitmap(itemId:String, projectImageId:String, sizeType:Int=SMALL_IMAGE):Bitmap? {
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