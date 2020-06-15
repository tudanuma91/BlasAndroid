package com.v3.basis.blas.blasclass.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * バックグラウンドでDownloadを実行するためのWorkerを継承した抽象クラス。
 * 継承クラスではdownloadTaskを実装する必要がある。
 * [引数]　
 * context: Context　親クラスWorkerのコンストラクタに必要
 * workerParameters: WorkerParameters　親クラスWorkerのコンストラクタに必要
 * [特記事項]
 * コンストラクタの引き数はAPI内部クラスで処理されるため、意識しなくても良い。
 * [作成者]
 * fukuda
 */
abstract class BaseDownloadWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters)  {

    companion object {
        const val KEY_DOWNLOAD = "key_download"
        const val KEY_SAVE_PATH = "key_save_path"
        const val KEY_UNZIP_PATH = "key_unzip_path"
        const val KEY_SAVE_PATH_KEY_NAME = "key_save_path_kay_name"
        const val PROGRESS = "Progress"
        const val UNIQUE_KEY = "download_request"
    }

    //  Runningに変わったら実行される
    override fun doWork(): Result {

        val downloadUrl = inputData.getString(KEY_DOWNLOAD)
            ?: throw IllegalStateException("might be forgot set to download key via with WorkerHelper")
        val savePath = inputData.getString(KEY_SAVE_PATH)
            ?: throw IllegalStateException("might be forgot set to savePath key via with WorkerHelper")
        val unzipPath = inputData.getString(KEY_UNZIP_PATH)
            ?: throw IllegalStateException("might be forgot set to savePath key via with WorkerHelper")

        return downloadTask(downloadUrl, savePath, unzipPath)
    }

    /**
     * ダウンロード中のプログレスの値を設定します
     * [引数]　
     * value: Int　プログレスの値を設定する。
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 戻り値なし。
     * [その他]
     * [特記事項]
     * downloadTask関数内で呼び出します。
     * ダウンロード中のファイルで、ダウンロード完了したサイズバイトなどを指定。
     * [作成者]
     * fukuda
     */
    protected fun setProgressValue(value: Int) {
        val progress = workDataOf(PROGRESS to value)
        setProgressAsync(progress)
    }

    /**
     * ダウンロード処理を実行します。
     * [引数]　
     * downloadUrl: String　ダウンロード対象URL
     * savePath: String　ダウンロード完了後にファイル保存するためのパス
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * Result　Resultクラスを使いsuccess, retry, failureの何れかを返却
     * [その他]
     * [特記事項]
     * BaseDownloadWorkerを継承したクラスでオーバライドします。
     * [作成者]
     * fukuda
     */
    abstract fun downloadTask(downloadUrl: String, savePath: String, unZipPath: String): Result

    /**
     * ダウンロード処理を実行します。
     * [引数]　
     * なし。
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * Int　プログレスの最大値を設定。
     * [その他]
     * [特記事項]
     *　戻り値はダウンロードファイルのサイズなどを指定。
     * [作成者]
     * fukuda
     */
    open fun getMaxProgressValue(): Int = 0
}
