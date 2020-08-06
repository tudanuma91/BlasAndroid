package com.v3.basis.blas.blasclass.sync

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.controller.ImagesController
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureCellModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * [説明]
 * データをBLASに一括送信するためのクラス
 */
class Lump(
    val context: Context
    , val projectId:String
    , val token:String
    , val callType:Int
    , val callBack: (success: Boolean) -> Unit
) {


    fun exec() {
        Log.d("Lump.exec()","start")
        val dis = CompositeDisposable()
        //syncFixture()
        //syncItem()
        Single.fromCallable{
            //syncFixture()
            syncItem()
            syncImages()
        }.subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribeBy {
                callBack.invoke(true)
                dis.dispose()
            }.addTo(CompositeDisposable())
        Log.d("Lump.exec()","end")
    }

    private fun syncFixture() {

        val fixtures = FixtureController(context,projectId.toString()).search(null,true)
        var sucCnt = 0

        fixtures.forEach {fixture ->

            val value = ""
            // 便宜上作成
            val model = FixtureCellModel(
                token
                ,projectId.toInt()
                ,fixture.fixture_id
                ,""
                ,value
                ,fixture.sync_status
                ,context
            )

            var sync:SyncFixtureBase
            when( fixture.status ) {
                BaseController.KENPIN_FIN -> {
                    sync = Kenpin(model,fixture)
                }
                BaseController.TAKING_OUT -> {
                    // 持出
                    sync = Takeout( model,fixture )
                }
                BaseController.RTN -> {
                    // 持出
                    sync = Rtn( model,fixture )
                }
                else -> {
                    Log.d("ERROR!!!","パラメータ異常")
                    return
                }
            }


            val dis = CompositeDisposable()
            sync.eventCompleted
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {

                    if( 0 == callType  ) {
                        sucCnt ++
                        if( sucCnt == fixtures.count() ) {
                            callBack.invoke(it)
                            dis.dispose()
                        }
                    }

                }
                .addTo(dis)

            sync.exec()

        }

    }

    fun syncItem():Int {
        Log.d("Lump.syncItem()","start")

        val items = ItemsController(context,projectId).search(paging = 0,  syncFlg = true)
        var sucCnt = 0

        items.forEach {  itemMap ->
            itemMap["item_id"]?.toLong()?.let {
                //SyncItemクラス生成
                val sync = SyncItem(context,token,projectId, it)
                val bRet = sync.exec()
                if( 1 == callType) {
                    sucCnt ++
                }
                /*
                val dis = CompositeDisposable()
                //実行完了後にコールバックするサブスクライバ設定
                sync.eventCompleted
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy {
                        if( 1 == callType) {
                            sucCnt ++
                            if( sucCnt == items.count() ) {
                                callBack.invoke(it)
                                dis.dispose()
                            }
                        }
                    }
                    .addTo(dis)

                sync.exec()

                 */
            }
        }

        return sucCnt
    }


    fun syncImages() {
        Log.d("Lump.syncImages()","start")
        //同期されていないレコードを取得する
        val images = ImagesController(context,projectId).searchNosyncRecords()
        images.forEach{
            it.item_id?.let { it1 -> SyncImage(context,token,projectId, it1).exec() }
        }
    }
}
