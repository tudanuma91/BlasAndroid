package com.v3.basis.blas.ui.item.item_view

import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Items
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.blasclass.sync.SyncItem
import com.v3.basis.blas.ui.common.ServerSyncModel
import com.v3.basis.blas.ui.common.ServerSyncViewModel
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.lang.Exception

class ItemsListViewModel: ServerSyncViewModel() {

    val transitionItemImage: PublishSubject<ItemsCellModel> = PublishSubject.create()
    val transitionItemEdit: PublishSubject<ItemsCellModel> = PublishSubject.create()


    override fun syncDB(serverModel: ServerSyncModel) {
        Log.d("syncDB()","start")

        val model = serverModel as ItemsCellModel
        SyncItem(model.context,model.token,model.project_id.toString(),model.item_id).exec()

        Log.d("syncDB()","end")
    }


    fun clickImageButton(model: ItemsCellModel) {
        transitionItemImage.onNext(model)
    }

    fun clickEditButton(model: ItemsCellModel) {
        transitionItemEdit.onNext(model)
    }
}
