package com.v3.basis.blas.blasclass.sync

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Items
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.rest.BlasRestItem
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

        BlasRestItem("create_sync",payload2,::success,::error).execute()

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

        eventCompleted.onNext(true)

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

        eventCompleted.onNext(false)

    }


}
