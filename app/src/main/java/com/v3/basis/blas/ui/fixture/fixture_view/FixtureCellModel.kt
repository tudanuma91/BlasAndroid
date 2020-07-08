package com.v3.basis.blas.ui.fixture.fixture_view

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField

data class FixtureCellModel(
    val token : String,
    val project_id : Int,
    val fixture_id: Long,
    val title: String = "",
    val detail: String = "",
    val context : Context
) {

    val errorMessage: ObservableField<String> = ObservableField()
    val progress: ObservableBoolean = ObservableBoolean(false)
    val status: ObservableField<String> = ObservableField("サーバーに登録待ちです")
    val syncEnable: ObservableBoolean = ObservableBoolean(true)
    val syncVisible: ObservableBoolean = ObservableBoolean(true)
}
