package com.v3.basis.blas.ui.item.common

import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
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
import java.util.*
import kotlin.Exception
import kotlin.concurrent.withLock

class ItemViewModel: ViewModel() {

    val fields: MutableList<FieldModel> = mutableListOf()

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

        //TODO:ここでバリデーションを行う
        var validateFlg = true
        fields.forEach {
            //バリデーション
            var ret = it.validate(itemId.toString())
            validateFlg = ret and validateFlg
            ret = it.parentValidate()
            validateFlg = ret and validateFlg
            
        }

        if(!validateFlg) {
            //エラーメッセージはvalidate内でセットしている
            Toast.makeText(context, "入力に誤りがあります。項目上部に表示されるエラーメッセージを確認してください", Toast.LENGTH_LONG).show()
            return
        }

        Completable.fromAction {

            val map = mutableMapOf<String, String?>()
            map.set("project_id", projectId)

            itemsController?.also {
                fields.forEachIndexed { index, f ->
                    val fieldModel = (f as FieldModel)
                    map.set("fld${fieldModel.field.col}", fieldModel.convertToString())


                    when( fieldModel.field.type.toString() ) {
                        FieldType.CATEGORY_SELECTION -> {
                            map.set("category",fieldModel.convertToString())
                        }
                        FieldType.WORKER_NAME -> {

                            if( fieldModel.convertToString().isNullOrEmpty() ) {
                                map.set("worker_user_id",null)
                            }
                            else{
                                map.set(
                                    "worker_user_id"
                                    , itemsController!!.getWorkerUserId( fieldModel.convertToString() ).toString()
                                )
                            }

                        }
                        FieldType.SCHEDULE_DATE -> {
                            map.set("schedule_date",fieldModel.convertToString())
                        }
                        else -> {}
                    }


                }

                try {
                    SenderHandler.lock.withLock {
                        if (itemId == 0L) {
                            //LDBにレコードを追加する
                            if(it.insertToLDB(map)) {
                                BlasSyncMessenger.notifyBlasItems(token, projectId)
                            }
                            else {
                                BlasLog.trace("E", "データベースの更新に失敗したため、再送イベントは送りません")
                            }
                        } else {
                            map.set("item_id", itemId.toString())
                            //LDBのレコードを更新する
                            if(it.updateToLDB(map)) {
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

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}
