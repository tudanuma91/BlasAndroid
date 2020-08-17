package com.v3.basis.blas.ui.fixture.fixture_view

import android.os.VibrationEffect
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.sync.Kenpin
import com.v3.basis.blas.blasclass.sync.Rtn
import com.v3.basis.blas.blasclass.sync.SyncFixtureBase
import com.v3.basis.blas.blasclass.sync.Takeout
import com.v3.basis.blas.ui.common.ServerSyncModel
import com.v3.basis.blas.ui.common.ServerSyncViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.lang.Exception

class FixtureListViewModel: ServerSyncViewModel() {

    private lateinit var model: FixtureCellModel
    val errorEvent: PublishSubject<String> = PublishSubject.create()
    private val disposable = CompositeDisposable()

    val sendCount: ObservableField<Int> = ObservableField(0)

    override fun syncDB(serverModel: ServerSyncModel) {

        val model = serverModel as FixtureCellModel

        Log.d("syncDB","start")
        Log.d("project_id",model.project_id.toString())
        Log.d("fixture_id",model.fixture_id.toString())

        val fixtureController = FixtureController( model.context, model.project_id.toString())
      /*  fixtureController
            .errorMessageEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { errorEvent.onNext(it) }
            .addTo(disposable)*/
        val records = fixtureController.search( model.fixture_id )
        if( 0 == records.count() ) {
            Log.d("getRecord","該当レコードが存在しません!!!")
            throw Exception("DB Sync error!! 該当レコードが存在しません")
        }
        val rec = records[0]

        if( BaseController.SYNC_STATUS_SYNC ==  rec.sync_status ) {
            return
        }

        var sync:SyncFixtureBase

        when( rec.status ) {
            BaseController.KENPIN_FIN -> {
                // 検品
                sync = Kenpin( model,rec )
            }
            BaseController.TAKING_OUT -> {
                // 持出
                sync = Takeout( model,rec )
            }
            BaseController.RTN -> {
                // 持出
                sync = Rtn( model,rec )
            }
            else -> {
                Log.d("ERROR!!!","パラメータ異常")
                throw Exception("パラメーター異常")
            }
        }
        //ここで例外を返すのでコール元で表示する
        sync.exec()
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

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}
