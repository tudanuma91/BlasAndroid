package com.v3.basis.blas.ui.item.item_view

import android.util.Log
import androidx.databinding.ObservableField
import com.v3.basis.blas.blasclass.sync.SyncItem
import com.v3.basis.blas.ui.common.ServerSyncModel
import com.v3.basis.blas.ui.common.ServerSyncViewModel
import io.reactivex.subjects.PublishSubject
import kotlin.Exception

class ItemsListViewModel: ServerSyncViewModel() {

    val transitionItemImage: PublishSubject<ItemsCellModel> = PublishSubject.create()
    val transitionItemEdit: PublishSubject<ItemsCellModel> = PublishSubject.create()


    val sendCount: ObservableField<Int> = ObservableField(0)

    override fun syncDB(serverModel: ServerSyncModel) {
        Log.d("syncDB()","start")

        val model = serverModel as ItemsCellModel
        val item_id = SyncItem(model.context,model.token,model.project_id.toString(),model.item_id).exec()

        if (item_id == null) {
            throw Exception("item_id is null!!")
        }
       // SyncImage(model.context,model.token,model.project_id.toString(),item_id.toLong()).exec()

        Log.d("syncDB()","end")
    }


    fun clickImageButton(model: ItemsCellModel) {
        transitionItemImage.onNext(model)
    }

    fun clickEditButton(model: ItemsCellModel) {
        transitionItemEdit.onNext(model)
    }
}
