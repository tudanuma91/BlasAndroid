package com.v3.basis.blas.ui.item.item_view

import com.v3.basis.blas.ui.common.ServerSyncModel
import com.v3.basis.blas.ui.common.ServerSyncViewModel
import io.reactivex.subjects.PublishSubject

class ItemsListViewModel: ServerSyncViewModel() {

    val transitionItemImage: PublishSubject<ItemsCellModel> = PublishSubject.create()
    val transitionItemEdit: PublishSubject<ItemsCellModel> = PublishSubject.create()

    override fun syncDB(model: ServerSyncModel) {
        //TODO 三代川さん
    }

    fun clickImageButton(model: ItemsCellModel) {
        transitionItemImage.onNext(model)
    }

    fun clickEditButton(model: ItemsCellModel) {
        transitionItemEdit.onNext(model)
    }
}
