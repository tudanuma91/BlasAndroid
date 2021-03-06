package com.v3.basis.blas.ui.fixture.fixture_kenpin_multi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_fixture_kenpin_multi.*
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.blasclass.service.BlasSyncMessenger
import com.v3.basis.blas.blasclass.service.SenderHandler
import com.v3.basis.blas.ui.ext.addTitleWithProjectName
import com.v3.basis.blas.ui.common.FixtureBaseFragment
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_fixture_kenpin_multi.view.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import kotlin.concurrent.withLock

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FixtureKenpinMultiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FixtureKenpinMultiFragment : FixtureBaseFragment() {
    var barcodeReader:MultiQrBarcodeReader? = null
    var barcodeSubscriber:BarCodeSubScriber<String>? = null
    companion object {
        fun newInstance() = FixtureKenpinMultiFragment()
    }

    /**
     * カメラの初期化
     */
    private fun initBarcodeReader() {
        BlasLog.trace("I","カメラの初期化を行います")

        barcodeReader = activity?.let { MultiQrBarcodeReader(it, preview1, imageView) }

        if(barcodeReader != null) {
            var flow = Flowable.create<String>(barcodeReader, BackpressureStrategy.BUFFER)

            /**
             * カメラを非同期で動作させる。
             * 読み取ったバーコードの値を発行し、購読者に提供する
             */
            barcodeSubscriber = context?.let { BarCodeSubScriber(it, token, projectId) }
            if(barcodeSubscriber != null) {
                flow.observeOn(Schedulers.newThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(barcodeSubscriber)
            }
        }
    }

    /**
     * オーバーライド関数
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitleWithProjectName("検品画面(複数読み込み)")
        Log.d("konishi", "${token} ${projectId} ${projectName}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_fixture_kenpin_multi, container, false)
        //トーチの設定
        val torchBtn = root.torch_multi_btn
        torchBtn.setOnClickListener {
            barcodeReader?.enableTorch()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BlasLog.trace("I", "マルチバーコードリーダー 起動")

    }

    override fun onResume() {
        super.onResume()
        //カメラの権限設定
        context?.let {
            if(!hasPermissions(it)) {
                requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
                //ここで権限を何かしないとダメっぽい
            }
            else {
                initBarcodeReader()
            }
        }
    }

    override fun onPause() {
        BlasLog.trace("I", "マルチバーコードリーダー onPause")
        barcodeReader?.unbindAll()
        //barcodeSubscriber?.dispose()
        super.onPause()

    }

    override fun onStop() {
        BlasLog.trace("I", "マルチバーコードリーダー onStop")
        barcodeReader?.unbindAll()
        //barcodeSubscriber?.dispose()
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var chkPermission = true

        grantResults.forEach {
            if(it != 0) {
                chkPermission = false
            }
        }
        if(chkPermission){
            initBarcodeReader()
        }else{
            Toast.makeText(getActivity(), "アクセス権限がありません。QRコード読み取りを実行できません。", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * バーコードを購読するクラス。
 */
class BarCodeSubScriber<String>(val context:Context, val token:String, val projectId:String): Subscriber<String> {
    var subscription:Subscription? = null
    val cacheResults:MutableMap<kotlin.String, Int> = mutableMapOf<kotlin.String, Int>()


    override fun onComplete() {
    }

    override fun onSubscribe(s: Subscription?) {
        //最初はとりあえず10000個予約。
        subscription = s
        subscription?.request(10000)
    }

    override fun onNext(barCode: String) {
        //バーコード受信処理
        BlasLog.trace("I","バーコードを読み取りました(${barCode})")
        SenderHandler.lock.withLock {
            BlasLog.trace("I","ロックを取得しました")
            val fixtureController = FixtureController(context, projectId.toString())
            val results: MutableMap<kotlin.String, Int> = fixtureController.kenpin(barCode.toString())
            results.forEach{key, value->
                cacheResults[key] = value
                //リストビューにデータを流す。
                FixtureSlideFragment.listFragment.setItems(key, value)
            }
            BlasSyncMessenger.notifyBlasFixtures(token.toString(), projectId.toString())
            BlasLog.trace("I","サービスにイベントを通知しました")
        }


        subscription?.request(1)
    }

    override fun onError(t: Throwable?) {
        BlasLog.trace("E","エラーが発生しました ${t?.message}")
    }

    /*
    fun saveCache() {
        context.openFileOutput("BarCodeListLog", Context.MODE_APPEND).use {
            var str = Gson().toJson(cacheResults)
            it.write(str.toByteArray())
        }
    }*/


    fun dispose() {
       // saveCache()
        BlasLog.trace("I","購読をキャンセルします")
        subscription?.cancel()
    }
}
