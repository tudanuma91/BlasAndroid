package com.v3.basis.blas.blasclass.sync

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Items
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.blasclass.rest.SyncBlasRestItem
import com.v3.basis.blas.ui.item.item_image.FileExtensions
import com.v3.basis.blas.ui.item.item_view.ItemsCellModel
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.lang.Exception

class SyncItem(val context: Context,val token : String, val projectId : String ,val itemId :Long) {

    lateinit var mapItem : MutableMap<String, String?>
    lateinit var item : Items

    val eventCompleted: PublishSubject<Boolean> = PublishSubject.create()

    fun exec(  ) {

        Log.d("project_id",projectId)
        Log.d("item_id",itemId.toString())

        val ctl = ItemsController(context,projectId)

        val records = ctl.search(itemId)

        if( 0 == records.count() ) {
            Log.d("getRecord","該当レコードが存在しません!!!")
            throw Exception("DB Sync error!! 該当レコードが存在しません")
        }

        mapItem = records[0]
        item = ctl.setProperty(Items(),mapItem) as Items

        var payload2 = mutableMapOf<String,String>()

        mapItem.forEach{
            payload2[it.key] = it.value.toString()
        }
        payload2["token"] = token

        //BlasRestItem("create_sync",payload2,::success,::error).execute()
        val json = SyncBlasRestItem().create_sync(payload2)
        if(json != null){
            val errorCode = json.getInt("error_code")
            if(errorCode == 0) {
                val ctl = ItemsController(context,projectId.toString())

                if( BaseController.SYNC_STATUS_NEW == item.sync_status ) {
                    val records = json.getJSONObject("records")

                    val new_item_id = records.getString("new_item_id")
                    val org_item_id = records.getString("temp_item_id")

                    ctl.updateItemId4Insert( org_item_id,new_item_id )
                }
                else {
                    ctl.updateItemId4Update(item.item_id.toString(),item,mapItem)
                }
            }
        }
    }

    fun imageSuccess(result:JSONObject){
        //画像の送信に成功した。
        //IDと画像ファイル名のすり替えを行う
        val records = result.getJSONObject("records")
        //ここを実装する！！！！！！！！！！！！！！！！！
        //まず、サーバー側からIDが返ってこないのが問題。


        Log.d("imageSuccess()","start   result ====> " + result.toString())
        eventCompleted.onNext(true)
        val newImageId = records.getString("new_image_id")
        val tempImageId = records.getString("temp_image_id")

        val ctl = ImagesController(context, projectId)
        ctl.fixImageRecord(newImageId, tempImageId)

    }

    fun imageError(errorCode: Int, aplCode :Int) {
        Log.d("imageError","error!!!!!!!")
        val ctl = ItemsController(context,projectId.toString())
        ctl.setErrorMsg(itemId.toString(),"画像の登録に失敗しました")
    }


    fun SyncImage() {
        //同じitem_idのうち、未送信画像を検索する
        val imgCtl = ImagesController(context, projectId)
        val imageRecords = imgCtl.searchNosyncRecord(itemId)
        imageRecords.forEach{
            //拡張子取得
            val ext = it.filename?.let { it1 -> ImageComponent().getExt(it1) }

            //拡張子からjpg,pngのフォーマット取得
            val format = ext?.let { it1 -> FileExtensions.matchExtension(it1) }

            //bmpを読む
            val bmp = it.filename?.let { it1 ->
                ImageComponent().readBmpFromLocal(context, it.project_id.toString(),
                    it1
                )
            }

            //bmpをbase64に変換する
            val base64Img = bmp?.let { it1 -> ImageComponent().encode(bmp, ext) }
            //送信用payloadの作成
            val payload = mapOf(
                "token" to token,
                "image_id" to it.image_id.toString(),
                "project_id" to it.project_id.toString(),
                "project_image_id" to it.project_image_id.toString(),
                "item_id" to it.item_id.toString(),
                "image" to base64Img,
                "image_type" to (format?.restImageType ?: "jpg")
            )

            BlasRestImage("upload", payload, ::imageSuccess, ::imageError).execute()
        }
    }

    fun success(result: JSONObject) {
        Log.d("success()","start   result ====> " + result.toString())

        val ctl = ItemsController(context,projectId.toString())

        if( BaseController.SYNC_STATUS_NEW == item.sync_status ) {
            val records = result.getJSONObject("records")

            val new_item_id = records.getString("new_item_id")
            val org_item_id = records.getString("temp_item_id")

            ctl.updateItemId4Insert( org_item_id,new_item_id )
        }
        else {
            ctl.updateItemId4Update(item.item_id.toString(),item,mapItem)
        }
        SyncImage()
        //eventCompleted.onNext(true)

        Log.d("success()","end")
    }

    fun error(errorCode: Int, aplCode :Int) {
        Log.d("error","error!!!!!!!")

        var errMsg = ""
        when( errorCode ) {

            // TODO:三代川　たぶん足りない・・・Listに全部入れよう！(fixtureも含めて)
            306 -> {
                errMsg = "重複エラー"
            }
            307 -> {
                errMsg = "設置機器が登録されているものと異なります"
            }
            308 -> {
                errMsg = "撤去機器が登録されているものと異なります"
            }
            else -> {
                errMsg = "サーバー同期エラー"
            }
        }

        Log.d("errMsg",errMsg)

        val ctl = ItemsController(context,projectId.toString())
        ctl.setErrorMsg(itemId.toString(),errMsg)

      //  eventCompleted.onNext(false)

    }


}
