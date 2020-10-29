package com.v3.basis.blas.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import java.lang.RuntimeException

open class QRCameraFragment:
    Fragment() {
    private var tone : ToneGenerator? = null
    var vibrator : Vibrator? = null
    lateinit var vibrationEffect : VibrationEffect
    var cameraPreview: CompoundBarcodeView? = null
    lateinit var userCallBack:(barcode:String)->Unit

    protected val PERMISSIONS_REQUEST_CODE = 1234
    protected val PERMISSIONS_REQUIRED = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    /**
     * 権限チェック　関数に関数を代入する。
     */
    protected fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //バイブレーションの設定
        try{
            vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrationEffect = VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        catch(e: RuntimeException) {
            vibrator = null
            Toast.makeText(context, "バイブレーションを使用できません", Toast.LENGTH_SHORT).show()
        }

        //ビープ音の設定
        try{
            tone = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)
        }
        catch(e: RuntimeException) {
            tone = null
            Toast.makeText(context, "ビープ音を使用できません", Toast.LENGTH_SHORT).show()
        }
    }

    override  fun onResume() {
        cameraPreview?.resume()    // カメラ起動
        super.onResume()

    }

    override  fun onPause() {
        cameraPreview?.pause()    // カメラ停止
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


    public fun startCamera(preview:CompoundBarcodeView, userCallBack:(barcode:String)->Unit) {
        cameraPreview = preview
        this.userCallBack = userCallBack
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

    private fun initBarcodeReader() {
        val cameraCallback = CameraCallback(
            tone,
            vibrator,
            vibrationEffect,
            userCallBack
        )
        cameraPreview?.decodeContinuous(cameraCallback)  //ここがflowableだったらいいのにな
    }
}

class CameraCallback(var tone:ToneGenerator?,
                     var vibrator:Vibrator?,
                     var vibrationEffect : VibrationEffect?,
                     var userCallBack:(barCode:String)->Unit) : BarcodeCallback{
    private var oldCode = ""

    override fun barcodeResult(result: BarcodeResult?) {
        if(oldCode != result.toString()) {
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP)
            vibrator?.vibrate(vibrationEffect)
            oldCode = result.toString() //重複チェック用

            userCallBack(result.toString())
        }
    }

    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
        //TODO("Not yet implemented")
    }

}