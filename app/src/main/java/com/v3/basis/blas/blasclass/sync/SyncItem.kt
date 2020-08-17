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

    fun exec(  ) :String? {

        Log.d("project_id",projectId)
        Log.d("item_id",itemId.toString())

        val itemCtl = ItemsController(context,projectId)

        val records = itemCtl.search(itemId)

        if( 0 == records.count() ) {
            Log.d("getRecord","該当レコードが存在しません!!!")
            throw Exception("DB Sync error!! 該当レコードが存在しません")
        }

        mapItem = records[0]
        item = itemCtl.setProperty(Items(),mapItem) as Items

        var payload = mutableMapOf<String,String>()

        mapItem.forEach{
            payload[it.key] = it.value.toString()
        }
        payload["token"] = token

        val json = SyncBlasRestItem().create_sync(payload)
        if(json != null){
            val errorCode = json.getInt("error_code")
            if(errorCode == 0) {
                if( BaseController.SYNC_STATUS_NEW == item.sync_status ) {
                    val records = json.getJSONObject("records")

                    val new_item_id = records.getString("new_item_id")
                    val org_item_id = records.getString("temp_item_id")

                    itemCtl.updateItemId4Insert( org_item_id,new_item_id )
                    return new_item_id
                    //eventCompleted.onNext(true)
                }
                else {
                    itemCtl.updateItemId4Update(item.item_id.toString(),item,mapItem)
                    return item.item_id.toString()
                    //eventCompleted.onNext(true)
                }
            }
            else {
                error(errorCode)
                return null
                //eventCompleted.onNext(false)
            }
        }
        else {
            //通信エラー
            error(-1)
            return null
            //eventCompleted.onNext(false)
        }
//        return true//ここには来ない
    }


    fun error(errorCode: Int) {
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

        val itemCtl = ItemsController(context,projectId.toString())

        itemCtl.setErrorMsg(itemId.toString(),errMsg)

    }


}
