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
            addDownloadTask(fragment: Fragment,
                            downloadUrl: String,
                            savePath: String,
                            crossinline stateChangedCallBack: (
                              state: WorkInfo.State,
                              progressValue: Int?,
                              uuid: UUID) -> Unit): UUID {


        //  バックグラウンド実行するための条件フィルターを設定する。
        //  ネットワークに接続されている場合のみ、処理を継続する
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //  BaseDownloadWorkerに変数を渡すための処理、BaseDownloadWorkerでも同じキーを使い変数を読み込む
        val data = workDataOf(BaseDownloadWorker.KEY_DOWNLOAD to downloadUrl)
        val path = workDataOf(BaseDownloadWorker.KEY_SAVE_PATH to savePath)

        //  OneTimeWorkRequestBuilderは一度だけ実行するリクエストを作成する。
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

        //  enqueueUniqueWorkとAPPENDで逐次実行を実現する。必要なタスクを登録しておいて、処理が終わり次第、逐次実行される
        //  つまり、enqueueUniqueWorkはダウンロード対象のプロジェクトを複数指定してしておいても、同時にキューイングされず、終わり次第、順次タスク実行される。
        val instance = WorkManager.getInstance(fragment.requireContext())
        instance.enqueueUniqueWork(BaseDownloadWorker.UNIQUE_KEY, ExistingWorkPolicy.APPEND, downloadWorkRequest)

        //  バックグラウンドのイベントを継続的に監視する
        observe(fragment, downloadWorkRequest.id, stateChangedCallBack)

        return downloadWorkRequest.id
    }

    fun stopDownload(context: Context, id: UUID) {
        WorkManager.getInstance(context).cancelWorkById(id)
    }

    inline fun observe(
        fragment: Fragment,
        id: UUID,
        crossinline stateChangedCallBack: (state: WorkInfo.State, progressValue: Int?, uuid: UUID) -> Unit) {

        val instance = WorkManager.getInstance(fragment.requireContext())
        instance.getWorkInfosForUniqueWorkLiveData(BaseDownloadWorker.UNIQUE_KEY)
            .observe(fragment.viewLifecycleOwner, androidx.lifecycle.Observer {
                it?.find { it.id.compareTo(id) == 0 }?.also {
                    when (it.state) {
                        WorkInfo.State.BLOCKED,
                        WorkInfo.State.CANCELLED,
                        WorkInfo.State.FAILED,
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.SUCCEEDED -> {
                            stateChangedCallBack.invoke(it.state, null, id)
                        }
                        WorkInfo.State.RUNNING -> {
                            stateChangedCallBack.invoke(
                                it.state,
                                it.progress.getInt(BaseDownloadWorker.PROGRESS, 0), id
                            )
                        }
                    }
                }
            })
    }
}
