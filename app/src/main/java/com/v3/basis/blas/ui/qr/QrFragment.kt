package com.v3.basis.blas.ui.qr


import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult

import com.v3.basis.blas.R
import kotlinx.android.synthetic.main.fragment_qr.*
import java.util.jar.Manifest

/**
 * A simple [Fragment] subclass.
 */
class QrFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_qr, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
        initQRCamera()
    }

    private fun checkPermissions() {
        // already we got permission.
        if (context?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.CAMERA) } == PackageManager.PERMISSION_GRANTED) {
            qr_view.resume()
            Log.d("1","kokodayo")
        }

        if (activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.CAMERA) }!!) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.CAMERA), 999)
            Log.d("2","kokodayo")
        }
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION:Int = 1
    }


    private fun initQRCamera() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val isReadPermissionGranted = (activity?.let { checkSelfPermission(it,READ_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_GRANTED)
        val isWritePermissionGranted = (activity?.let { checkSelfPermission(it,WRITE_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_GRANTED)
        val isCameraPermissionGranted = (activity?.let { checkSelfPermission(it,CAMERA) } == PackageManager.PERMISSION_GRANTED)

        if (isReadPermissionGranted && isWritePermissionGranted && isCameraPermissionGranted) {
            openQRCamera() // ← カメラ起動
            Log.d("OK","こっちだぜ")
        } else {
            requestPermissions(arrayOf(CAMERA,WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA_PERMISSION)
            Log.d("いいや","こっちだ")
        }
    }

    private fun openQRCamera() {
        qr_view.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null) {
                    onPause()
                    Log.d("QRCode", "$result")
                    Log.d("QRCode", "だよ！！！")
                    val aaa = "test"
                    result_text.text = "$result"
                }else{
                    result_text.text = "読み取り中"
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) { }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CAMERA_PERMISSION -> { initQRCamera() }
        }
    }


}
