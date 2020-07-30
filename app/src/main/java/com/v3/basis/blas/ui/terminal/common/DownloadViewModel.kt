package com.v3.basis.blas.ui.terminal.common

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.rest.BlasRestCache
import com.v3.basis.blas.ui.ext.continueDownloadTask
import com.v3.basis.blas.ui.ext.traceLog
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * プロジェクトダウンロード開始を使用側に通知するのと、ダウンロードステータスの変化に応じた処理を実行する。
 * [引数]　
 * [特記事項]
 * [作成者]
 * fukuda
 */
class DownloadViewModel: ViewModel() {

    companion object {
        const val DOWNLOAD = "download"
        const val COMPLETE = "finish"
    }

    val startDownload: PublishSubject<DownloadModel> = PublishSubject.create()
    private val disposable = CompositeDisposable()
    private lateinit var token: String

    /**
     * DownloadItemを初期化します。
     * 初期化時に前回の状態を読み込み、UIの状態を復元します。
     * [引数]　
     * fragment:Fragment　前回ダウンロード中orダウンロード待ちの場合、実行タスクの監視を再開するために利用
     * item: DownloadItem　前回のUIの状態を復元。
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 戻り値なし。
     * [その他]
     * [特記事項]
     * なし。
     * [作成者]
     * fukuda
     */
    fun setupItem(fragment:Fragment, model: DownloadModel, token: String) {

        this.token = token

        readAsync {
            //   読み込み！！（仮機能）
            val pref = preferences()
            pref.getString(model.projectId, "")!!
        }.subscribeBy {
                with(model) {
                    uuid = it
                    downloading.set(uuid.isNotBlank() && uuid != COMPLETE)
                    doneDownloaded.set(uuid == COMPLETE)
                    if (doneDownloaded.get()) {
                        downloadingText.set("ダウンロード済み")
                    } else if (downloading.get()) {
                        downloadingText.set("ダウンロード中")
                        fragment.continueDownloadTask(this@DownloadViewModel, model)
                    }
                }
            }
            .addTo(disposable)
    }

/*
//    private fun setDownloadUrl(model: DownloadModel, callback: () -> Unit) {
//
////        item.downloadUrl = "https://www.basis-service.com/blas777/item/20200514043512291.zip"
////        item.hasNotDownloadUrl.set(false)
//        val payload = mapOf(
//            "token" to token,
//            "project_id" to model.projectId
//        )
//        val success: (json: JSONObject) -> Unit = {
//            traceLog(it.toString())
//            try {
//                val zipModel = Gson().fromJson(it.toString(), DownloadZipModel::class.java)
//                model.downloadUrl = BuildConfig.HOST + zipModel.zip_path
//                model.hasNotDownloadUrl.set(false)
//                callback.invoke()
//            } catch (e: Exception) {
//                Log.d("parse error", e.toString())
//                traceLog(e.stackTrace.toString())
//                model.hasNotDownloadUrl.set(true)
//            }
//        }
//        val funcError:(Int,Int) -> Unit = {errorCode, aplCode ->
//            model.downloadUrl = ""
//        }
//        BlasRestCache("zip", payload, success, funcError).execute()
//    }
*/

    /**
     * ダウンロードボタンがクリックされたとき、呼び出し側にそれを通知します。
     * [引数]　
     * item: DownloadItem　ダウンロード対象のプロジェクト。
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 戻り値なし。
     * [その他]
     * [特記事項]
     * Databindingを使ったレイアウトから呼び出す想定です。
     * [作成者]
     * fukuda
     */
    fun clickDownload(model: DownloadModel) {

        model.savePath = createZipFile(model.projectId, model.saveDir)
        model.downloading.set(true)
        model.downloadingText.set("ダウンロード待ち")
        model.doneDownloaded.set(false)
        startDownload.onNext(model)
    }

    private fun createZipFile(projectId: String, dir: String): String {
        val filePath = dir + "/" + System.currentTimeMillis() + "_" + projectId + ".zip"
        File(filePath).apply {
            if (exists().not()) {
                createNewFile()
            }
        }
        return filePath
    }

    /**
     * UIの状態をダウンロード待ちに変更します。
     * [引数]　
     * item: DownloadItem ダウンロード対象のItem
     * uuid: UUID　キューに追加されたタスクID
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 戻り値なし。
     * [その他]
     * [特記事項]
     * ダウンロード待機中を表すフラグを保存します。
     * [作成者]
     * fukuda
     */
    fun preDownloading(model: DownloadModel, uuid: UUID) {

        model.downloading.set(true)
        model.downloadingText.set("ダウンロード待ち")
        //   仮のセーブ方法！！
        taskAsync {
            val pref = preferences()
            pref.edit().putString(model.projectId, uuid.toString()).apply()
        }
    }

    fun cancelDownloading(model: DownloadModel, uuid: UUID) {

        model.downloading.set(false)
        model.downloadingText.set("ダウンロードに失敗")
        taskAsync {
            val pref = preferences()
            pref.edit().putString(model.projectId, "").apply()
        }
    }

    /**
     * UIの状態をダウンロード中に変更します。
     * [引数]　
     * item: DownloadItem ダウンロード対象のItem
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 戻り値なし。
     * [その他]
     * [特記事項]
     * [作成者]
     * fukuda
     */
    fun downloading(model: DownloadModel) {
        model.downloadingText.set("ダウンロード中")
    }

    /**
     * UIの状態をダウンロード済みに変更します。
     * [引数]　
     * item: DownloadItem ダウンロード対象のItem
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 戻り値なし。
     * [その他]
     * [特記事項]
     * ダウンロード済みを表すフラグを保存します。
     * [作成者]
     * fukuda
     */
    fun setFinishDownloading(model: DownloadModel) {

        model.downloading.set(false)
        model.downloadingText.set("ダウンロード済み")
        model.doneDownloaded.set(true)
        //   仮のセーブ方法！！
        taskAsync {
            val pref = preferences()
            pref.edit().putString(model.projectId, COMPLETE).apply()
        }
    }

    /**
     * ダウンロード対象リストのプリファレンスインスタンスを取得します。
     * [引数]　
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 戻り値なし。
     * [その他]
     * [特記事項]
     * [作成者]
     * fukuda
     */
    private fun preferences(): SharedPreferences {

        val context = BlasApp.applicationContext()
        return context.getSharedPreferences(DOWNLOAD, Context.MODE_PRIVATE)
    }

    /**
     * 非同期でコールバックを実行します
     * [引数]　
     * [ジェネリクス]
     * 指定なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * 戻り値なし。
     * [その他]
     *     action　非同期で実行する処理
     *     [引数]
     *     [例外]
     *     明示的な例外なし。
     *     [戻り値]
     *     なし
     * [特記事項]
     * [作成者]
     * fukuda
     */
    private fun taskAsync(action: () -> Unit) {

        val dis = CompositeDisposable()
        Completable.fromAction { action.invoke() }
            .doOnError { Log.d("DownloadItem.saveAsync", "Failed to save flag") }
            .onErrorComplete()
            .doOnComplete { dis.dispose() }
            .subscribe()
            .addTo(dis)
    }

    /**
     * 非同期でコールバックを実行するSingleを返します。
     * [引数]　
     * action 読み込み処理のコールバック
     * [ジェネリクス]
     * なし。
     * [例外]
     * 明示的なthrowなし。
     * [戻り値]
     * Single<Single>
     * [その他]
     *     action　非同期で実行する処理
     *     [引数]
     *     [例外]
     *     明示的な例外なし。
     *     [戻り値]
     *     String
     * [特記事項]
     * [作成者]
     * fukuda
     */
    private fun readAsync(action: () -> String): Single<String> {

        return Single.fromCallable { action.invoke() }
            .doOnError { Log.d("DownloadItem.saveAsync", "Failed to read flag") }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}
