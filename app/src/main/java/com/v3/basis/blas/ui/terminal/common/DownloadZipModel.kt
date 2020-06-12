package com.v3.basis.blas.ui.terminal.common

data class DownloadZipModel(
    val error_code: Int, // 0
    val message: String, // None
    val zip_path: String // /blas7/sqlite/20200612061617206.zip
)
