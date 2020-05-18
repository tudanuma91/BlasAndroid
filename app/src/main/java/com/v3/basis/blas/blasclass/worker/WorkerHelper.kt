package com.v3.basis.blas.blasclass.worker

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object WorkerHelper {

    /**
     * return value: Scheduling worker task id
     *      idはタスクをストップするときに利用します
     */
    inline fun <reified T: BaseDownloadWorker>
            startDownload(fragment: Fragment,
                          downloadUrl: String,
                          savePath: String,
                          crossinline stateChangedCallBack: (state: WorkInfo.State, progressValue: Int?) -> Unit): UUID {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = workDataOf(BaseDownloadWorker.KEY_DOWNLOAD to downloadUrl)
        val path = workDataOf(BaseDownloadWorker.KEY_SAVE_PATH to savePath)

        val downloadWorkRequest = OneTimeWorkRequestBuilder<T>()
            .setConstraints(constraints)
            .setInputData(data)
            .setInputData(path)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        val instance = WorkManager.getInstance(fragment.requireContext())
        instance.getWorkInfoByIdLiveData(downloadWorkRequest.id)
            .observe(fragment.viewLifecycleOwner, androidx.lifecycle.Observer {
                it?.also {
                    when (it.state) {
                        WorkInfo.State.CANCELLED,
                        WorkInfo.State.FAILED,
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.SUCCEEDED -> {
                            stateChangedCallBack.invoke(it.state, null)
                        }
                        WorkInfo.State.RUNNING -> {
                            stateChangedCallBack.invoke(it.state,
                                it.progress.getInt(BaseDownloadWorker.PROGRESS, 0))
                        }
                    }
                }
            })

        //  enqueueUniqueWorkとAPPENDで逐次実行を実現する。必要なタスクを登録しておいて、処理が終わり次第、逐次実行される
        //  つまり、enqueueUniqueWorkはダウンロード対象のプロジェクトを複数指定してしておいても、同時にキューイングされず、終わり次第、順次タスク実行される。
        instance.enqueueUniqueWork(BaseDownloadWorker.UNIQUE_KEY, ExistingWorkPolicy.APPEND, downloadWorkRequest)

        return downloadWorkRequest.id
    }

    inline fun continueObserve(
        fragment: Fragment,
        id: UUID,
        crossinline stateChangedCallBack: (state: WorkInfo.State, progressValue: Int?) -> Unit) {

        val instance = WorkManager.getInstance(fragment.requireContext())
        instance.getWorkInfoByIdLiveData(id)
            .observe(fragment.viewLifecycleOwner, androidx.lifecycle.Observer {
                it?.also {
                    when (it.state) {
                        WorkInfo.State.CANCELLED,
                        WorkInfo.State.FAILED,
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.SUCCEEDED -> {
                            stateChangedCallBack.invoke(it.state, null)
                        }
                        WorkInfo.State.RUNNING -> {
                            stateChangedCallBack.invoke(it.state,
                                it.progress.getInt(BaseDownloadWorker.PROGRESS, 0))
                        }
                    }
                }
            })
    }

    fun stopDownload(context: Context, id: UUID) {
        WorkManager.getInstance(context).cancelWorkById(id)
    }
}
