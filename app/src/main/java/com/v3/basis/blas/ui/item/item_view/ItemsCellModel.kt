package com.v3.basis.blas.ui.item.item_view

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.v3.basis.blas.ui.common.ServerSyncModel

class ItemsCellModel(
    token : String,
    project_id : Int,
    val item_id: Long,
    val title: String = "",
    val detail: String = "",
    val valueList: ArrayList<String?>,
    syncStatus: Int,
    context : Context
) : ServerSyncModel(token, project_id, item_id, syncStatus, context)
