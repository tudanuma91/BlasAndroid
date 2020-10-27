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
import kotlinx.android.synthetic.main.fragment_fixture_kenpin_multi.*
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.ui.ext.addTitleWithProjectName
import com.v3.basis.blas.ui.fixture.FixtureBaseFragment
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_fixture_kenpin_multi.view.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FixtureKenpinMultiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FixtureKenpinMultiFragment : FixtureBaseFragment() {
    //val disposables = CompositeDisposable()
    var barcodeReader:MultiQrBarcodeReader? = null
    var barcodeSubscriber:BarCodeSubScriber<String>? = null

    private val PERMISSIONS_REQUEST_CODE = 1234
    private val PERMISSIONS_REQUIRED = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    companion object {
        fun newInstance() = FixtureKenpinMultiFragment()
    }

    /**
     * 権限チェック　関数に関数を代入する。
     */
    private fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * カメラの初期化
     */
    private fun initBarcodeReader() {
        barcodeReader = activity?.let { MultiQrBarcodeReader(it, preview1, imageView) }

        if(barcodeReader != null) {
            var flow = Flowable.create<String>(barcodeReader, BackpressureStrategy.BUFFER)

            /**
             * カメラを非同期で動作させる。
             * 読み取ったバーコードの値を発行し、購読者に提供する
             */
            barcodeSubscriber = context?.let { BarCodeSubScriber(it, projectId) }
            if(barcodeSubscriber != null) {
                flow.observeOn(Schedulers.newThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(barcodeSubscriber)
            }
        }
    }

    fun setBarCode(barcode:String) {
        context?.let {context->
            FixtureController(
                context,
                projectId.toString()
            ).kenpin(barcode)
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
        barcodeReader?.unbindAll()
        barcodeSubscriber?.dispose()
        super.onPause()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

class BarCodeSubScriber<String>(context:Context, projectId:String): Subscriber<String> {
    var subscription:Subscription? = null
    var controller = FixtureController(
        context,
        projectId.toString()
    )

    override fun onComplete() {
    }

    override fun onSubscribe(s: Subscription?) {
        //一個だけデータください
        subscription = s
        subscription?.request(1)
    }

    override fun onNext(t: String) {
        controller.kenpin(t.toString())
        subscription?.request(1)
    }

    override fun onError(t: Throwable?) {
    }

    fun dispose() {
        subscription?.cancel()
    }
}
