package com.v3.basis.blas.ui.terminal.common

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField

class DownloadItem(
    val id: String,
    val downloadUrl: String,
    val savePath: String) {

    val downloadingText: ObservableField<String> = ObservableField("")
    var doneDownloaded: ObservableBoolean = ObservableBoolean(false)
    val downloading: ObservableBoolean = ObservableBoolean(false)
    var uuid: String = ""
}
