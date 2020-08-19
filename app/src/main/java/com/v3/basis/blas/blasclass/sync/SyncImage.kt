package com.v3.basis.blas.blasclass.sync

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Images
import com.v3.basis.blas.blasclass.db.data.Items
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.blasclass.rest.SyncBlasRestImage
import com.v3.basis.blas.blasclass.rest.SyncBlasRestItem
import com.v3.basis.blas.ui.item.item_image.FileExtensions
import com.v3.basis.blas.ui.item.item_view.ItemsCellModel
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.lang.Exception
import java.net.URLEncoder
import java.security.MessageDigest

class SyncImage(val context: Context, val token : String, val projectId : String, val itemId :Long) {

    lateinit var mapItem : MutableMap<String, String?>
    lateinit var item : Items

    val eventCompleted: PublishSubject<Boolean> = PublishSubject.create()

    fun exec() {
        val itemCtl = ItemsController(context, projectId)
        val imgCtl = ImagesController(context, projectId)
        val imageRecords = imgCtl.searchNosyncRecord(itemId)
        imageRecords.forEach {
            if(it.sync_status == BaseController.SYNC_STATUS_SYNC) {
                //pass
            }
            else if(it.sync_status == BaseController.SYNC_STATUS_DEL) {
                delete(itemCtl, imgCtl, it)
            }
            else {
                upload(itemCtl, imgCtl, it)
            }
        }
    }

    fun delete(itemCtl:ItemsController, imgCtl:ImagesController, imageRecord: Images) {
        //同じitem_idのうち、未送信画像を検索する
        //送信用payloadの作成
        var json:JSONObject? = null
        val payload = mapOf(
            "token" to token,
            "image_id" to imageRecord.image_id.toString()
        )

        imageRecord.image_id?.let {
            if(it < 0) {
                //新規追加でまだサーバに送っていないため、サーバには通信しない
                imageRecord.filename?.also { it1 ->
                    ImageComponent().delImgFile(context, projectId,
                        it1
                    )
                    imgCtl.fixDeleteImage(it1)
                }
                return
            }
            else {
                SyncBlasRestImage().delete(payload)?.let{
                    val errorCode = it.getInt("error_code")
                    if (errorCode == 0) {
                        //DB削除
                        imgCtl.fixDeleteImage(it.toString())
                        //画像削除
                        imageRecord.filename?.also { it1 ->
                            ImageComponent().delImgFile(
                                context, projectId,
                                it1
                            )
                        }
                        //データ管理は、サーバー送信時、変更がなくてもアップデートしているため、
                        //ステータスは本登録済みになっている
                    }
                    else {
                        //画像削除に失敗した
                        val itemRecord = itemCtl.search(itemId)
                        if(itemRecord.count() > 0) {
                            val syncStatus = itemRecord[0]["sync_status"]?.toInt()
                            if(syncStatus == BaseController.SYNC_STATUS_SYNC) {
                                //データ管理は本登録済みの場合、
                                // 本登録できているので、画像登録だけがエラーのときは編集中に戻す
                                itemCtl.setSyncStatus(itemId, BaseController.SYNC_STATUS_EDIT)
                            }
                            itemCtl.setErrorMsg(itemId.toString(), "画像の更新に失敗しました")
                        }
                        else {
                            //やることなし
                        }
                    }
                }
            }
        }
    }

    fun upload(itemCtl:ItemsController, imgCtl:ImagesController, imageRecord: Images) {
        //同じitem_idのうち、未送信画像を検索する
        //拡張子取得
        val ext = imageRecord.filename?.let { it1 -> ImageComponent().getExt(it1) }

        //拡張子からjpg,pngのフォーマット取得
        val format = ext?.let { it1 -> FileExtensions.matchExtension(it1) }

        //bmpを読む
        val bmp = imageRecord.filename?.let { it1 ->
            ImageComponent().readBmpFromLocal(
                context, imageRecord.project_id.toString(), it1
            )
        }

        //bmpをbase64に変換する
        val base64Img = bmp?.let { it1 -> ImageComponent().encode(bmp, ext) }

        // サーバー側base64decodeで微妙に違うものになるので、base64データをhash化する
        val hash = MessageDigest.getInstance("MD5")
            .digest(base64Img?.toByteArray())
            .joinToString(separator = ""){
                "%02x".format(it)
            }

        //送信用payloadの作成
        val payload = mapOf(
            "token" to token,
            "image_id" to imageRecord.image_id.toString(),
            "project_id" to imageRecord.project_id.toString(),
            "project_image_id" to imageRecord.project_image_id.toString(),
            "item_id" to imageRecord.item_id.toString(),
            "image" to base64Img,
            "image_type" to (format?.restImageType ?: "jpg"),
            "hash" to hash
        )

        val json = SyncBlasRestImage().upload(payload)
        if (json != null) {
            val errorCode = json.getInt("error_code")
            if (errorCode == 0) {
                val records = json.getJSONObject("records")
                val newImageId = records.getString("new_image_id")
                val tempImageId = records.getString("temp_image_id")

                val ctl = ImagesController(context, projectId)
                ctl.fixUploadImage(newImageId, tempImageId)
                //itemテーブルのステータスは、本登録したときにitemのデータも送信しているため
                //SyncItem内でsync_statusは登録完了になっている。
            } else {
                //画像送信失敗した
                val itemRecord = itemCtl.search(itemId)
                if(itemRecord.count() > 0) {
                    val syncStatus = itemRecord[0]["sync_status"]?.toInt()
                    if(syncStatus == BaseController.SYNC_STATUS_SYNC) {
                        //データ管理は本登録済みの場合、
                        // 本登録できているので、画像登録だけがエラーのときは編集中に戻す
                        itemCtl.setSyncStatus(itemId, BaseController.SYNC_STATUS_EDIT)
                    }
                    itemCtl.setErrorMsg(itemId.toString(), "画像の更新に失敗しました")
                }
            }
        }
    }
}
