package com.v3.basis.blas.ui.common

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField

open class ServerSyncModel(
    val token : String,
    val project_id : Int,
    val uniqueId: Long,
    syncStatus: Int,
    val context : Context
) {

    val errorMessage: ObservableField<String> = ObservableField("")
    val progress: ObservableBoolean = ObservableBoolean(false)
    val status: ObservableField<String> = ObservableField("サーバーに登録待ちです")
    val syncEnable: ObservableBoolean = ObservableBoolean(true)
    val syncVisible: ObservableBoolean = ObservableBoolean(syncStatus >= 1)
}
