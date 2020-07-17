package com.v3.basis.blas.ui.fixture.fixture_view

import android.os.VibrationEffect
import android.util.Log
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.sync.Kenpin
import com.v3.basis.blas.blasclass.sync.Rtn
import com.v3.basis.blas.blasclass.sync.SyncFixtureBase
import com.v3.basis.blas.blasclass.sync.Takeout
import com.v3.basis.blas.ui.common.ServerSyncModel
import com.v3.basis.blas.ui.common.ServerSyncViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.lang.Exception

class FixtureListViewModel: ServerSyncViewModel() {

    private lateinit var model: FixtureCellModel

    override fun syncDB(serverModel: ServerSyncModel) {

        val model = serverModel as FixtureCellModel

        Log.d("syncDB","start")
        Log.d("project_id",model.project_id.toString())
        Log.d("fixture_id",model.fixture_id.toString())

        val fixtureController = FixtureController( model.context, model.project_id.toString())
        val records = fixtureController.search( model.fixture_id )

        if( 0 == records.count() ) {
            Log.d("getRecord","該当レコードが存在しません!!!")
            throw Exception("DB Sync error!! 該当レコードが存在しません")
        }
        val rec = records[0]

        if( BaseController.SYNC_STATUS_SYNC ==  rec.sync_status ) {
            return
        }

        var cync:SyncFixtureBase
        if( BaseController.KENPIN_FIN == rec.status ) {
            // 検品
            cync = Kenpin( model,rec )
        }
        else if( BaseController.TAKING_OUT == rec.status ) {
            // 持出
            cync = Takeout( model,rec )
        }
        else if( BaseController.RTN == rec.status ) {
            // 持出
            cync = Rtn( model,rec )
        }
        else {
            Log.d("ERROR!!!","パラメータ異常")
            return
        }
        cync.exec()
    }

    private var vibrationEffect = VibrationEffect.createOneShot(300,
        VibrationEffect.DEFAULT_AMPLITUDE
    )

    fun success(result: JSONObject){
        Log.d("OK", "作成完了")

    }

    fun error(errorCode: Int, aplCode :Int){
        Log.d("NG", "作成失敗")
        Log.d("errorCorde", "${errorCode}")
    }
}
