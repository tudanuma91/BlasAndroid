package com.v3.basis.blas.ui.item.common

import android.widget.LinearLayout
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.db.data.ItemsController
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

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

    val disposable = CompositeDisposable()

    var itemsController: ItemsController? = null
    var projectId: String = ""

    fun clickSave(container: LinearLayout) {

        Completable.fromAction {
            val map = mutableMapOf<String, String?>()
            map.set("project_id", projectId)
            itemsController?.also {
                fields.forEachIndexed { index, f ->
                    val field = (f as FieldModel)
                    map.set("fld${index + 1}", field.convertToString())
                }
                it.create(map)
            }
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                completeSave.onNext(Unit)
            }
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
        qrKenpinEvent.onNext(field)
    }

    fun clickQRCodeTekkyo(field: FieldText) {
        qrTekkyoEvent.onNext(field)
    }

    fun clickAccountName(field: FieldText) {
        accountNameEvent.onNext(field)
    }
}
