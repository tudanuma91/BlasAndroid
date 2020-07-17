package com.v3.basis.blas.blasclass.worker

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object WorkerHelper {

    /**
     * バックグラウンドでファイルをダウンロードします。
     * [引数]　
     * fragment WorkerHelperを呼び出すfragment
     * downloadUrl ダウンロード先URL
     * savePath 保存先ファイル名
     * stateChangedCallBack ダウンロードの状態を受け取るコールバック関数
     * [ジェネリクス]
     * reified T: BaseDownloadWorker　BaseDownloadWorkerクラスを継承したクラスを指定する
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * return Scheduling worker task id。idはタスクをストップするときに利用します
     * [その他]
     *     stateChangedCallBack
     *     [引数]
     *     state ダウンロードの状態
     *     progressValue ダウンロードの進捗率
     *     uuid ダウンロード処理ごとに割り振られたID。uuidはタスクをストップするときに利用します
     *     [例外]
     *     明示的な例外なし。
     *     [戻り値]
     *     なし
     * [特記事項]
     * 稼働条件：ネットワークに接続されている
     * 逐次実行の動作をするため、タスクが完了してから次のタスクを実行する。
     * [作成者]
     * fukuda
     */
    inline fun <reified T: BaseDownloadWorker>
            addDownloadTask(fragment: Fragment,
                            token: String,
                            projectId: String,
                            savePath: String,
                            unzipPath: String,
                            savePathKeyValue: String,
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
        val data = workDataOf(
            BaseDownloadWorker.KEY_TOKEN to token,
            BaseDownloadWorker.KEY_PROJECT_ID to projectId,
            BaseDownloadWorker.KEY_SAVE_PATH to savePath,
            BaseDownloadWorker.KEY_UNZIP_PATH to unzipPath,
            BaseDownloadWorker.KEY_SAVE_PATH_KEY_NAME to savePathKeyValue
        )

        //  OneTimeWorkRequestBuilderは一度だけ実行するリクエストを作成する。
        val downloadWorkRequest = OneTimeWorkRequestBuilder<T>()
            .setConstraints(constraints)
            .setInputData(data)
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

    /**
     * バックグラウンドで実行中のファイルダウンロードを中止します。
     * [引数]　
     * context: Context WorkManagerのインスタンを取得するのに必要です
     * uuid: UUID 停止したいタスクのUUID
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 無し。
     * [その他]
     * なし。
     * [特記事項]
     * なし。
     * [作成者]
     * fukuda
     */
    fun stopDownload(context: Context, id: UUID) {
        WorkManager.getInstance(context).cancelWorkById(id)
    }

    /**
     * 既にキューイングされたダウンロードタスクの状態を定期的に監視します。
     * [引数]　
     * fragment WorkerHelperを呼び出すfragment
     * uuid: UUID 監視したいタスクのID
     * stateChangedCallBack ダウンロードの状態を受け取るコールバック関数
     * [ジェネリクス]
     * reified T: BaseDownloadWorker　BaseDownloadWorkerクラスを継承したクラスを指定する
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * return Scheduling worker task id。idはタスクをストップするときに利用します
     * [その他]
     *     stateChangedCallBack
     *     [引数]
     *     state ダウンロードの状態
     *     progressValue ダウンロードの進捗率
     *     uuid ダウンロード処理ごとに割り振られたID。uuidはタスクをストップするときに利用します
     *     [例外]
     *     明示的な例外なし。
     *     [戻り値]
     *     なし
     * [特記事項]
     * なし。
     * [作成者]
     * fukuda
     */
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
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.FAILED,
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
