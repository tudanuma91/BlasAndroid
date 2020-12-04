package com.v3.basis.blas.ui.item.item_view

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject


class ItemsListViewModel: ViewModel() {

    var imageBtnCallBack:((model:ItemsCellModel)->Unit)? = null
    var editBtnCallBack:((model:ItemsCellModel)->Unit)? = null

    fun clickImageButton(model: ItemsCellModel) {
        imageBtnCallBack?.invoke(model)
    }

    fun clickEditButton(model: ItemsCellModel) {
        editBtnCallBack?.invoke(model)
    }
}
