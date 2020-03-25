package com.v3.basis.blas.ui.qr


import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.ui.ext.checkPermissions
import com.v3.basis.blas.ui.ext.permissionChk
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import com.v3.basis.blas.ui.item.item_create.ItemCreateFragment
import kotlinx.android.synthetic.main.fragment_fixture_kenpin.*
import kotlinx.android.synthetic.main.fragment_qr.*
import kotlinx.android.synthetic.main.fragment_qr.qr_view
import org.json.JSONObject
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class QrFragment : Fragment() {
    companion object {
        const val REQUEST_CAMERA_PERMISSION:Int = 1
    }
    private var token:String? = null
    private var cameraManager: CameraManager? = null
    private var cameraID: String? = null
    private var SW: Boolean = false
    private var projectId:String? = null
    private var intent :Intent? = null
    private var messageText: TextView? = null
    private var oldResult:String? =null
    private var vibrator: Vibrator? = null
    private var vibrationEffect = VibrationEffect.createOneShot(300,
        VibrationEffect.DEFAULT_AMPLITUDE
    )
    private var tone: ToneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intent = Intent(activity, QRActivity::class.java)

        requireActivity().checkPermissions()

        //プロジェクト名をここで入れる。
        data_qr_project_name.text = "aiueo"


        //ライト光るボタン実装
        //現在エラーが出ているので使用不可
        cameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?

        //ボタンがタップされたときの処理
        dataQrBtnLight.setOnClickListener{
            if(cameraID == null){
                Log.d("null","nullだったよ")
            }
            try {
                if(SW == false){
                    //SWがfalseならばトーチモードをtrueにしてLDEオン
                    //McameraManager!!.setTorchMode(McameraID!!, true)
                    qr_view.setTorchOn()
                    SW=true
                }else{
                    //SWがtrueならばトーチモードをfalseにしてLDEオフ
                    //McameraManager!!.setTorchMode(McameraID!!, false)
                    qr_view.setTorchOff()
                    SW=false
                }

            } catch (e: Exception) {
                //エラー処理
                e.printStackTrace();
            }
        }

        initQRCamera()
    }


    private fun initQRCamera() {//QRコードリーダ起動
        //権限チェック
        val setPermisson =requireActivity().permissionChk()
        //カメラの起動
        if (setPermisson) {
            qr_view.resume()
            openQRCamera(
                "kenpin",
                qr_view,
                requireActivity(),
                token,
                projectId,
                data_qr_result_text,
                data_qr_message)
        } else {//許可取れなかった場合、行う
            requestPermissions(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), FixtureKenpinFragment.REQUEST_CAMERA_PERMISSION
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            FixtureKenpinFragment.REQUEST_CAMERA_PERMISSION -> { initQRCamera() }
        }
    }

    fun callOnPouse(){
        onPause()
    }

    /**
     * QRカメラを起動する関数
     * type => return/kenpin/takeoutのどれかが入る
     * qr_view => QRカメラ起動時に設定を入れる
     * fragm => fragmentを入れる。
     * token => トークンを入れる
     * projectId => プロジェクトIDを入れる
     * result_text => 読みとったQRコードの値を出力する場所
     * message_text => restを使用した結果を出力する場所
     *
     */
    fun openQRCamera(
        type:String,
        qr_view : CompoundBarcodeView,
        fragm: FragmentActivity?,
        token: String?,
        projectId:String?,
        result_text: TextView,
        message_text: TextView

    ) {
        qr_view.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {//QRコードを読み取った時の処理
                if(result.toString() != oldResult) {
                    if (result != null) {
                        //初期化と変数宣言
                        messageText = null
                        messageText = message_text

                        //読み取りを伝えるバイブレーション
                        vibrator = fragm!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                        //Thread.sleep(500)
                        Log.d("QRCode", "処理実行！！")
                        result_text.text = "$result"



                        //ひとつ前のQRコードをこのQRコードにする。連続読み取りを避けるため。
                        oldResult = result.toString()


                        var payload2 = mapOf(
                            "token" to token,
                            "project_id" to projectId,
                            "serial_number" to "${result}"
                        )
                        //val intent = Intent(activity, QRActivity::class.java)


                        QrFragment().callOnPouse()

                        Log.d("OK", "終了")
                        //この時、エラーが帰ってきたら逃がす処理を追加する。
                    }

                }
            }
            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) { }
        })
    }

    /**
     * rest成功時の処理
     *
     */
    fun success(result: JSONObject){
        vibrationEffect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator!!.vibrate(vibrationEffect)
        tone.startTone(ToneGenerator.TONE_DTMF_S,200)
        Log.d("OK","作成完了")
        messageText!!.setTextColor(Color.GREEN)
        messageText!!.text = "QRコードを読み取りました"
    }

    /**
     * rest失敗時の処理
     */
    fun error(errorCode: Int){
        vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator!!.vibrate(vibrationEffect)
        tone.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,200)
        Log.d("NG","作成失敗")
        Log.d("errorCorde","${errorCode}")
        messageText!!.setTextColor(Color.RED)
        messageText!!.text = "すでに登録済のQRコードです"

    }

}
