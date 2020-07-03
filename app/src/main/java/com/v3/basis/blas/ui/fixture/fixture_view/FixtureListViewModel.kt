package com.v3.basis.blas.ui.fixture.fixture_view

import android.widget.Button
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class FixtureListViewModel: ViewModel() {

    private val disposableMap: MutableMap<Int, CompositeDisposable> = mutableMapOf()

    fun clickSyncToServer(model: FixtureCellModel) {

        model.progress.set(true)
        model.syncEnable.set(false)

        val disposables = CompositeDisposable()
        Completable
            .fromAction { syncDB(model) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .delay(5, TimeUnit.SECONDS)
            .subscribeBy(
                onError = {
                    model.syncEnable.set(true)
                    setError("例外が発生しました", model)
                },
                onComplete = {
                    model.syncEnable.set(true)
                    model.progress.set(false)
                    model.status.set("サーバーに登録成功しました")
                }
            )
            .addTo(disposables)

        disposableMap.put(model.fixture_id, disposables)
    }

    fun clickCancel(model: FixtureCellModel) {
        model.progress.set(false)
        model.syncEnable.set(true)
        model.status.set("サーバーに登録待ちです")

        //DB同期スレッドをキャンセルする！！
        if (disposableMap.containsKey(model.fixture_id)) {
            disposableMap[model.fixture_id]?.dispose()
            disposableMap.remove(model.fixture_id)
        }
    }

    //TODO 三代川さん
    //  エラーメッセージはsetError()を使ってください
    private fun syncDB(model: FixtureCellModel) {

    }

    private fun setError(errorMessage: String, model: FixtureCellModel) {
        model.progress.set(false)
        model.status.set("サーバーに登録失敗しました")
        model.errorMessage.set(errorMessage)
    }
}
