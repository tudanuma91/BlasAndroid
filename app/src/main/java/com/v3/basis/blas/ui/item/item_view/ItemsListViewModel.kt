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

    override fun syncDB(serverModel: ServerSyncModel) {
        //TODO 三代川さん
        Log.d("syncDB()","start")

        val model = serverModel as ItemsCellModel

        Log.d("project_id",model.project_id.toString())
        Log.d("item_id",model.item_id.toString())

        val ctl = ItemsController(model.context,model.project_id.toString())
        val records = ctl.search(model.item_id)

        if( 0 == records.count() ) {
            Log.d("getRecord","該当レコードが存在しません!!!")
            throw Exception("DB Sync error!! 該当レコードが存在しません")
        }

        val map = records[0]
        val item = ctl.setProperty(Items(),records[0])

        var payload2 = mutableMapOf<String,String>()

        map.forEach{
            payload2[it.key] = it.value.toString()
        }
        payload2["token"] = model.token

        BlasRestItem("create_sync",payload2,::success,::error).execute()


        Log.d("syncDB()","end")
    }

    fun success(result: JSONObject) {

    }

    fun error(errorCode: Int, aplCode :Int) {

    }

    fun clickImageButton(model: ItemsCellModel) {
        transitionItemImage.onNext(model)
    }

    fun clickEditButton(model: ItemsCellModel) {
        transitionItemEdit.onNext(model)
    }
}
