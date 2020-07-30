package com.v3.basis.blas.ui.fixture.fixture_view

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.v3.basis.blas.ui.common.ServerSyncModel

class FixtureCellModel(
    token : String,
    project_id : Int,
    val fixture_id: Long,
    val title: String = "",
    val detail: String = "",
    syncStatus: Int,
    context : Context
) : ServerSyncModel(token, project_id, fixture_id, syncStatus, context)
