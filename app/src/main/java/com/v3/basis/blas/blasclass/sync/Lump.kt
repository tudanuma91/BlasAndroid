package com.v3.basis.blas.blasclass.sync

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.ui.fixture.fixture_view.FixtureCellModel

class Lump(
    val context: Context
    , val projectId:String
    , val token:String
) {

    fun exec() {
        Log.d("Lump.exec()","start")

        syncFixture()

        syncItem()

    }

    private fun syncFixture() {

        val fixtures = FixtureController(context,projectId.toString()).search(null,true)

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
            sync.exec()

        }

    }

    fun syncItem() {
        Log.d("Lump.syncItem()","start")

        val items = ItemsController(context,projectId).search(paging = 0,  syncFlg = true)

        items.forEach {  itemMap ->
            itemMap["item_id"]?.toLong()?.let { SyncItem(context,token,projectId, it).exec() }
        }

    }

}