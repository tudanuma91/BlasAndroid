package com.v3.basis.blas.ui.item.item_view

import android.util.Log
import com.v3.basis.blas.blasclass.db.data.Items
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.common.ServerSyncModel
import com.v3.basis.blas.ui.common.ServerSyncViewModel
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.lang.Exception

class ItemsListViewModel: ServerSyncViewModel() {

    val transitionItemImage: PublishSubject<ItemsCellModel> = PublishSubject.create()
    val transitionItemEdit: PublishSubject<ItemsCellModel> = PublishSubject.create()

    lateinit var model:ItemsCellModel

    lateinit var mapItem : MutableMap<String, String?>
    lateinit var item : Items

    override fun syncDB(serverModel: ServerSyncModel) {
        //TODO 三代川さん
        Log.d("syncDB()","start")

        model = serverModel as ItemsCellModel

        Log.d("project_id",model.project_id.toString())
        Log.d("item_id",model.item_id.toString())

        val ctl = ItemsController(model.context,model.project_id.toString())
        val records = ctl.search(model.item_id)

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
        payload2["token"] = model.token

        BlasRestItem("create_sync",payload2,::success,::error).execute()


        Log.d("syncDB()","end")
    }

    fun success(result: JSONObject) {
        Log.d("success()","start   result ====> " + result.toString())

        val ctl = ItemsController(model.context,model.project_id.toString())

        if( 1 == item.sync_status ) {
            val records = result.getJSONObject("records")

            val new_item_id = records.getString("new_item_id")
            val org_item_id = records.getString("temp_item_id")

            ctl.updateItemId4Insert( org_item_id,new_item_id )
        }
        else {
            ctl.updateItemId4Update(item.item_id.toString(),item,mapItem)
        }

        Log.d("success()","end")
    }

    fun error(errorCode: Int, aplCode :Int) {
        Log.d("error","error!!!!!!!")
    }

    fun clickImageButton(model: ItemsCellModel) {
        transitionItemImage.onNext(model)
    }

    fun clickEditButton(model: ItemsCellModel) {
        transitionItemEdit.onNext(model)
    }
}
