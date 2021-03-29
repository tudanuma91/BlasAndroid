package com.v3.basis.blas.ui.item.item_view

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject


class ItemsListViewModel: ViewModel() {

    var imageBtnCallBack:((model:ItemsCellModel)->Unit)? = null
    var editBtnCallBack:((model:ItemsCellModel)->Unit)? = null
    var mapBtnCallBack:((model:ItemsCellModel)->Unit)? = null

    fun clickImageButton(model: ItemsCellModel) {
        //ItemViewFragmentのclickImageButton関数を呼び出す
        imageBtnCallBack?.invoke(model)
    }

    fun clickEditButton(model: ItemsCellModel) {
        //ItemViewFragmentのclickEditButton関数を呼び出す
        editBtnCallBack?.invoke(model)
    }

    fun clickMapButton(model: ItemsCellModel) {
        //ItemViewFragmentのclickEditButton関数を呼び出す
        mapBtnCallBack?.invoke(model)
    }

}
