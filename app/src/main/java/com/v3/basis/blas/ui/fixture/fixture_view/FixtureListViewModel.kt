package com.v3.basis.blas.ui.fixture.fixture_view

import android.os.VibrationEffect
import android.util.Log
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.sync.Kenpin
import com.v3.basis.blas.blasclass.sync.Rtn
import com.v3.basis.blas.blasclass.sync.SyncFixtureBase
import com.v3.basis.blas.blasclass.sync.Takeout
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class FixtureListViewModel: ViewModel() {

    //private val disposableMap: MutableMap<Int, CompositeDisposable> = mutableMapOf()
    private val disposableMap: MutableMap<Long, CompositeDisposable> = mutableMapOf()

    private lateinit var model: FixtureCellModel

    fun clickSyncToServer(in_model: FixtureCellModel) {

        model = in_model

        model.progress.set(true)
        model.syncEnable.set(false)

        val disposables = CompositeDisposable()
        Completable
            .fromAction { syncDB(model) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = {
                    model.syncEnable.set(true)
                    setError("例外が発生しました", model)
                },
                onComplete = {
                    model.syncEnable.set(true)
                    model.progress.set(false)
                    model.status.set("サーバーに登録成功しました")
                    Log.d("サーバー登録","成功！！")
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

    //  TODO:エラーメッセージはsetError()を使ってください
    private fun syncDB(model: FixtureCellModel) {
        Log.d("syncDB","start")
        Log.d("project_id",model.project_id.toString())
        Log.d("fixture_id",model.fixture_id.toString())

        val fixtureController = FixtureController(  model.context, model.project_id.toString())
        val records = fixtureController.search( model.fixture_id )
        if( 0 == records.count() ) {
            Log.d("getRecord","Record Nothing!!!")
            return
        }
        val rec = records[0]

        // TODO:0の時はボタンを表示しないようにしましょう！
        if( 0 ==  rec.sync_status ) {
            return
        }

        var cync:SyncFixtureBase
        if( 0 == rec.status ) {
            // 検品
            cync = Kenpin( model,rec )
        }
        else if( 1 == rec.status ) {
            // 持出
            cync = Takeout( model,rec )
        }
        else if( 4 == rec.status ) {
            // 持出
            cync = Rtn( model,rec )
        }
        else {
            Log.d("ERROR!!!","パラメータ異常")
            return
        }
        cync.exec()


    }

    private fun setError(errorMessage: String, model: FixtureCellModel) {
        model.progress.set(false)
        model.status.set("サーバーに登録失敗しました")
        model.errorMessage.set(errorMessage)
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
