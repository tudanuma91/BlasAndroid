package com.v3.basis.blas.blasclass.worker

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters

class SampleWorker(context: Context, workerParameters: WorkerParameters): BaseDownloadWorker(context, workerParameters) {

    override fun downloadTask(downloadUrl: String, savePath: String): Result {

        val sec = 1200
        Log.d("worker", "start")
        var progress = 0
        for (count in 0..20) {
            Thread.sleep(1000)
            progress += 10
            setProgressValue(progress)
            Log.d("background Worker", "progress: $progress")
        }

        return Result.success()
    }

    override fun getMaxProgressValue(): Int {
        return 0
    }
}
