package com.v3.basis.blas.ui.terminal.common

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.ui.ext.continueDownloadTask
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import java.util.*

class DownloadViewModel: ViewModel() {

    companion object {
        const val DOWNLOAD_IDS = "download_ids"
        const val COMPLETE = "finish"
    }

    val startDownload: PublishSubject<DownloadItem> = PublishSubject.create()
    private val disposable = CompositeDisposable()

    fun setupItem(fragment:Fragment, item: DownloadItem) {

        val single = readAsync {
            //   読み込み！！（仮機能）
            val pref = preferences()
            pref.getString(item.id, "")!!
        }

        single.subscribeBy {
                with(item) {
                    uuid = it as String
                    downloading.set(uuid.isNotBlank() && uuid != COMPLETE)
                    doneDownloaded.set(uuid == COMPLETE)
                    if (doneDownloaded.get()) {
                        downloadingText.set("ダウンロード済み")
                    } else if (downloading.get()) {
                        downloadingText.set("ダウンロード中")
                        fragment.continueDownloadTask(this@DownloadViewModel, item)
                    }
                }
            }
            .addTo(disposable)
    }

    fun downloadClick(item: DownloadItem) {

        startDownload.onNext(item)
    }

    fun preDownloading(item: DownloadItem, uuid: UUID) {

        item.downloadingText.set("ダウンロード待ち")
        item.downloading.set(true)
        //   仮のセーブ方法！！
        taskAsync {
            val pref = preferences()
            pref.edit().putString(item.id, uuid.toString()).apply()
        }
    }

    fun downloading(item: DownloadItem) {
        item.downloadingText.set("ダウンロード中")
    }


    fun setFinishDownloading(item: DownloadItem) {

        item.downloading.set(false)
        item.downloadingText.set("ダウンロード済み")
        item.doneDownloaded.set(true)
        //   仮のセーブ方法！！
        taskAsync {
            val pref = preferences()
            pref.edit().putString(item.id, COMPLETE).apply()
        }
    }

    private fun preferences(): SharedPreferences {

        val context = BlasApp.applicationContext()
        return context.getSharedPreferences(DOWNLOAD_IDS, Context.MODE_PRIVATE)
    }

    private fun taskAsync(action: () -> Unit) {

        val dis = CompositeDisposable()
        Completable.fromAction { action.invoke() }
            .doOnError { Log.d("DownloadItem.saveAsync", "Failed to save flag") }
            .onErrorComplete()
            .doOnComplete { dis.dispose() }
            .subscribe()
            .addTo(dis)
    }

    private fun readAsync(action: () -> Any): Single<*> {

        return Single.fromCallable { action.invoke() }
            .doOnError { Log.d("DownloadItem.saveAsync", "Failed to read flag") }
    }
}
