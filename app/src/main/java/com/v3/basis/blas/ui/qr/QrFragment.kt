package com.v3.basis.blas.ui.qr


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.ui.common.QRCameraFragment
import com.v3.basis.blas.ui.ext.checkPermissions
import kotlinx.android.synthetic.main.fragment_qr.*
import kotlinx.android.synthetic.main.fragment_qr.view.*

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val layout = inflater.inflate(R.layout.fragment_qr, container, false)

        val barCodeView = layout.qr_view

        val decodeFormats = listOf(
            BarcodeFormat.CODE_128,BarcodeFormat.CODE_93,BarcodeFormat.CODE_39,
            BarcodeFormat.EAN_13,BarcodeFormat.EAN_8,
            BarcodeFormat.CODABAR,BarcodeFormat.UPC_A,BarcodeFormat.UPC_E,
            BarcodeFormat.CODABAR,BarcodeFormat.ITF,
            BarcodeFormat.QR_CODE
        )

        barCodeView.barcodeView.decoderFactory = DefaultDecoderFactory(decodeFormats)

        return layout
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
                Log.d("null", "nullだったよ")
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
    private fun QRCallBack(code: String) {
        //検品データをLDBに保存するl
        requireActivity().setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra("qr_code", code) })
        requireActivity().finish()
    }
}

