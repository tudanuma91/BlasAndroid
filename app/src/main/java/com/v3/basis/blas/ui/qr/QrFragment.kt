package com.v3.basis.blas.ui.qr


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.ui.common.QRCameraFragment
import com.v3.basis.blas.ui.ext.checkPermissions
import kotlinx.android.synthetic.main.fragment_qr.*
import kotlinx.android.synthetic.main.fragment_qr.qr_view

/**
 * A simple [Fragment] subclass.
 */
class QrFragment : QRCameraFragment() {
    companion object {
    }

    private var cameraID: String? = null
    private var SW: Boolean = false
    private var intent :Intent? = null

//    private var tone: ToneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intent = Intent(activity, QRActivity::class.java)
        val extras = activity?.intent?.extras
        data_qr_project_name.text = ""
        //プロジェクト名をここで入れる。
        if(extras?.getString("projectName") != null){
            data_qr_project_name.text = extras.getString("projectName")
        }

        requireActivity().checkPermissions()


        //ライト光るボタン実装

        //ボタンがタップされたときの処理
        dataQrBtnLight.setOnClickListener{
            if(cameraID == null){
                Log.d("null","nullだったよ")
            }
            try {
                if(SW == false){
                    //SWがfalseならばトーチモードをtrueにしてLDEオン
                    qr_view.setTorchOn()
                    SW=true
                }else{
                    //SWがtrueならばトーチモードをfalseにしてLDEオフ
                    qr_view.setTorchOff()
                    SW=false
                }

            } catch (e: Exception) {
                //エラー処理
                e.printStackTrace();
            }
        }
        startCamera(qr_view, ::QRCallBack)
    }

    /**
     * カメラがQRコードを読み込んだときにコールバックされる
     */
    private fun QRCallBack(code:String) {
        //検品データをLDBに保存する
        requireActivity().setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra("qr_code", code) })
        requireActivity().finish()
    }
}
