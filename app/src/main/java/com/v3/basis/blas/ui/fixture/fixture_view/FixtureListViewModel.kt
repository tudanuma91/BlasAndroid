package com.v3.basis.blas.ui.fixture.fixture_view

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.db.fixture.FixtureController
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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

    //TODO 三代川さん
    //  エラーメッセージはsetError()を使ってください
    private fun syncDB(model: FixtureCellModel) {
        Log.d("syncDB","start")

        Log.d("project_id",model.project_id.toString())
        Log.d("fixture_id",model.fixture_id.toString())

        val fixtureController = FixtureController(  model.context, model.project_id.toString())
        val records = fixtureController.search( model.fixture_id )
        if( 0 == records.count() ) {
            Log.d("getRecord","Error!!!!!!!!!!!")
            return
        }
        val rec = records[0]

        // TODO:0の時はボタンを表示しないようにしましょう！
        if( 0 ==  rec.sync_status ) {
            return
        }

        if( 0 == rec.status ) {
            // 検品
            var payload2 = mutableMapOf(
                "token" to model.token,
                "fixture_id" to  rec.fixture_id.toString(),
                "project_id" to  model.project_id.toString(),
                "fix_org_id" to rec.fix_org_id.toString(),
                "fix_user_id" to rec.fix_user_id.toString(),
                "fix_date" to rec.fix_date,
                "serial_number" to rec.serial_number,
                "update_date" to rec.update_date,
                "sync_status" to rec.sync_status.toString()
            )
            if( 1 == rec.sync_status ) {
              payload2["create_date"] = rec.create_date
            }

            BlasRestFixture("kenpin", payload2, ::successKenpin, ::error).execute()
        }


    }

    private fun setError(errorMessage: String, model: FixtureCellModel) {
        model.progress.set(false)
        model.status.set("サーバーに登録失敗しました")
        model.errorMessage.set(errorMessage)
    }

    private var vibrationEffect = VibrationEffect.createOneShot(300,
        VibrationEffect.DEFAULT_AMPLITUDE
    )
//    private lateinit var vibrator: Vibrator
    private lateinit var messageText: TextView
    private var realTime = false


    fun successKenpin(result: JSONObject){
        Log.d("result",result.toString())

        val records = result.getJSONObject("records")
        Log.d("records",records.toString())

        val new_fixture_id = records.getString("fixture_id")
        val org_fixture_id = records.getString("temp_fixture_id")
        Log.d("fixture_id","new:" + new_fixture_id + " org:" + org_fixture_id)


        // TODO:実行するとなぜかどこかに行ってしまうので要調査？？？？
//        val fixtureController = FixtureController(  model.context, model.project_id.toString())
//        fixtureController.updateFixtureId(org_fixture_id,new_fixture_id)

        playTone(ToneGenerator.TONE_CDMA_ANSWER)
        Log.d("OK", "検品同期完了")
        realTime = false
    }


    fun success(result: JSONObject){
        //if(realTime) {
//        vibrationEffect =
//            VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
//        vibrator.vibrate(vibrationEffect)
        // tone.startTone(ToneGenerator.TONE_DTMF_S,200)
        //tone.startTone(ToneGenerator.TONE_CDMA_ANSWER,200)
        playTone(ToneGenerator.TONE_CDMA_ANSWER)
        Log.d("OK", "作成完了")
//        messageText.setTextColor(Color.GREEN)
//        messageText.text = "同期しました"
        realTime = false
        // }

    }

    fun error(errorCode: Int, aplCode :Int){
        // if(realTime) {
//        vibrationEffect =
//            VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
//        vibrator.vibrate(vibrationEffect)
        // tone.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_S_X4,200)
        playTone(ToneGenerator.TONE_CDMA_HIGH_PBX_S_X4)
        Log.d("NG", "作成失敗")
        Log.d("errorCorde", "${errorCode}")
//        var message: String? = ""

//        message = BlasMsg().getMessage(errorCode, aplCode)
//        messageText.setTextColor(Color.RED)
//        messageText.text = message
        //  realTime = false
        //}
    }


    private var tone: ToneGenerator? = null

    private fun playTone(mediaFileRawId: Int) {

        try {
            if (tone == null) {
                tone = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
            }
            tone?.also {
                it.startTone(mediaFileRawId, 200)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    if (it != null) {
                        Log.d("FixtureActivity", "ToneGenerator released")
                        it.release()
                        tone = null
                    }
                }, 200)
            }
        } catch (e: Exception) {
            Log.d("FixtureActivity", "Exception while playing sound:$e")
        }
    }


}
