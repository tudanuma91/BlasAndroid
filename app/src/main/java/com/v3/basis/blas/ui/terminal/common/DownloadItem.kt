package com.v3.basis.blas.ui.terminal.common

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField

/**
 * プロジェクトダウンロードにてダウンロードUIの状態を変更するためのクラス。
 * [引数]　
 * id: String　プロジェクトIDを指定
 * downloadUrl: String　対象プロジェクトのダウンロード先URLを指定
 * savePath: String　ダウンロードしたプロジェクトの保存先パスを指定
 * [特記事項]
 * 主にDownloadViewModelで使用する
 * [作成者]
 * fukuda
 */
class DownloadItem(
    val id: String,
    val downloadUrl: String,
    val savePath: String) {

    val downloadingText: ObservableField<String> = ObservableField("")
    var doneDownloaded: ObservableBoolean = ObservableBoolean(false)
    val downloading: ObservableBoolean = ObservableBoolean(false)
    var uuid: String = ""
}
