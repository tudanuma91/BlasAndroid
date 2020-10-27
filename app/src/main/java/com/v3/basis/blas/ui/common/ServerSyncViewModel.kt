package com.v3.basis.blas.ui.common

import android.util.Log
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.controller.FixtureController
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

abstract class ServerSyncViewModel: ViewModel() {

    protected val disposableMap: MutableMap<Long, CompositeDisposable> = mutableMapOf()
    protected abstract fun syncDB(model: ServerSyncModel)

    val serverSyncedEvent: PublishSubject<Unit> = PublishSubject.create()

    fun clickSyncToServer(serverModel: ServerSyncModel) {

        //  TODO 確認する
//        model = in_model

        serverModel.progress.set(true)
        serverModel.syncEnable.set(false)

        val disposables = CompositeDisposable()
        Completable
            .fromAction { syncDB(serverModel) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = {
                    serverModel.syncEnable.set(true)
                    it.message?.let { it1 -> setError(it1, serverModel) }
                },
                onComplete = {
                    serverModel.syncEnable.set(false)
                    serverModel.progress.set(false)
                    serverModel.status.set("サーバーに登録成功しました")
                    serverModel.syncedToServer.set(true)
                    serverSyncedEvent.onNext(Unit)
                    Log.d("サーバー登録","成功！！")
                }
            )
            .addTo(disposables)

        disposableMap.put(serverModel.uniqueId, disposables)
    }

    fun clickCancel(model: ServerSyncModel) {
        /*
        model.progress.set(false)
        model.syncEnable.set(true)
        model.status.set("サーバーに登録待ちです")
        */
        //DB同期スレッドをキャンセルする！！
        if (disposableMap.containsKey(model.uniqueId)) {
            disposableMap[model.uniqueId]?.dispose()
            disposableMap.remove(model.uniqueId)
        }

        val className = model::class.simpleName
        if(className == "FixtureCellModel") {
            if(model.uniqueId < 0) {
                //仮登録で負のIDだったら削除する。
                //例外返ったときにキチンとキャッチすること
                try {
                    FixtureController(
                        model.context,
                        model.project_id.toString()
                    ).delete(model.uniqueId)
                }
                catch(e:Exception) {
                }
            }

            //仮登録で正のIDだったら、サーバと同期する
        }
        else if (className == "ItemsCellModel") {
            Log.d("konishi", className)
            //例外返ったときにキチンとキャッチすること
            ItemsController(model.context, model.project_id.toString()).delete(model.uniqueId)
        }
    }

    private fun setError(errorMessage: String, model: ServerSyncModel) {
        model.progress.set(false)
        model.status.set("サーバーに登録失敗しました")
        model.errorMessage.set(errorMessage)
    }

    override fun onCleared() {
        super.onCleared()
        disposableMap.forEach { it.value.dispose() }
    }
}
