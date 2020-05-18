package com.v3.basis.blas.blasclass.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

abstract class BaseDownloadWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters)  {

    companion object {
        const val KEY_DOWNLOAD = "key_download"
        const val KEY_SAVE_PATH = "key_download"
        const val PROGRESS = "Progress"
        const val UNIQUE_KEY = "download_request"
    }

    override fun doWork(): Result {

        val downloadUrl = inputData.getString(KEY_DOWNLOAD)
            ?: throw IllegalStateException("might be forgot set to download key via with WorkerHelper")
        val savePath = inputData.getString(KEY_SAVE_PATH)
            ?: throw IllegalStateException("might be forgot set to savePath key via with WorkerHelper")

        return downloadTask(downloadUrl, savePath)
    }

    protected fun setProgressValue(value: Int) {
        val progress = workDataOf(PROGRESS to value)
        setProgressAsync(progress)
    }

    //***********************************************************************************/
    //  downloadタスクを実装して、Result.success,Result.failure,Result.retryのいずれかを返す /
    //*********************************************************************************/
    abstract fun downloadTask(downloadUrl: String, savePath: String): Result

    //  Progressで進捗状況の管理に使う.ファイルサイズなどを返すように実装する
    open fun getMaxProgressValue(): Int = 0
}
