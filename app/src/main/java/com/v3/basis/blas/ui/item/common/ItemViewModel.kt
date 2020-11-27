package com.v3.basis.blas.ui.item.common

import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.blasclass.rest.BlasRest
import com.v3.basis.blas.blasclass.service.BlasSyncMessenger
import com.v3.basis.blas.blasclass.service.SenderHandler
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlin.Exception
import kotlin.concurrent.withLock

class ItemViewModel: ViewModel() {

    val fields: MutableList<Any?> = mutableListOf()

    val dateEvent: PublishSubject<FieldText> = PublishSubject.create()
    val timeEvent: PublishSubject<FieldText> = PublishSubject.create()
    val qrEvent: PublishSubject<FieldText> = PublishSubject.create()
    val qrKenpinEvent: PublishSubject<FieldText> = PublishSubject.create()
    val qrTekkyoEvent: PublishSubject<FieldText> = PublishSubject.create()
    val locationEvent: PublishSubject<FieldText> = PublishSubject.create()
    val accountNameEvent: PublishSubject<FieldText> = PublishSubject.create()
    val completeSave: PublishSubject<Unit> = PublishSubject.create()
    val completeUpdate: PublishSubject<Unit> = PublishSubject.create()

    val disposable = CompositeDisposable()

    var itemsController: ItemsController? = null
    var token: String = ""
    var projectId: String = ""
    var itemId: Long = 0L

    fun setupUpdateMode(itemId: Long = 0L) {
        this.itemId = itemId
    }

    /**
     * [説明]
     * データ管理の保存ボタンを押したときに呼ばれる
     */
    fun clickSave(container: LinearLayout) {
        Log.d("clickSave()","start")

        Completable.fromAction {

            val map = mutableMapOf<String, String?>()
            map.set("project_id", projectId)

            itemsController?.also {
                fields.forEachIndexed { index, f ->
                    val field = (f as FieldModel)
                    map.set("fld${field.col}", field.convertToString())
                }

                try {
                    SenderHandler.lock.withLock {
                        if (itemId == 0L) {
                            it.create(map)
                        } else {
                            map.set("item_id", itemId.toString())
                            if(it.update(map)) {
                                BlasSyncMessenger.notifyBlasItems(token, projectId)
                            }
                            else {
                                BlasLog.trace("E", "データベースの更新に失敗したため、再送イベントは送りません")
                            }
                        }
                    }
                }
                catch ( ex : ItemsController.ItemCheckException ) {
                    // バリデートエラー
                    Log.d("item save validate error!!!",ex.message)

                    throw Exception(ex.message)       // TODO:??????
                }

            }
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = {
                    Toast.makeText(BlasRest.context, it.message, Toast.LENGTH_LONG).show()
                },
                onComplete = {
                    if (itemId == 0L) {
                        completeSave.onNext(Unit)
                    } else {
                        completeUpdate.onNext(Unit)
                    }
                }
            )
            .addTo(disposable)
    }

    fun clickDate(field: FieldText) {
        dateEvent.onNext(field)
    }

    fun clickTime(field: FieldText) {
        timeEvent.onNext(field)
    }

    fun clickLocation(field: FieldText) {
        locationEvent.onNext(field)
    }

    fun clickQRCode(field: FieldText) {
        qrEvent.onNext(field)
    }

    fun clickQRCodeKenpin(field: FieldText) {
        Log.d("ItemViewModel.clickQRCodeKenpin()","start")
        qrKenpinEvent.onNext(field)
    }

    fun clickQRCodeTekkyo(field: FieldText) {
        qrTekkyoEvent.onNext(field)
    }

    fun clickAccountName(field: FieldText) {
        accountNameEvent.onNext(field)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}
