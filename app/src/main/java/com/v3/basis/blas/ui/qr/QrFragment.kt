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
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.ui.ext.checkPermissions
import com.v3.basis.blas.ui.ext.permissionChk
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import kotlinx.android.synthetic.main.fragment_qr.*
import kotlinx.android.synthetic.main.view_blas_actionbar.*
import org.json.JSONObject

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
    private lateinit var messageText: TextView
    private var oldResult:String? =null
    private lateinit var vibrator: Vibrator
    private var vibrationEffect = VibrationEffect.createOneShot(300,
        VibrationEffect.DEFAULT_AMPLITUDE
    )

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
        //現在エラーが出ているので使用不可
        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager?

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
        initQRCamera()
    }

    /**
     * カメラの初期化（カメラの起動・停止はonResume,onPauseにて実施する）
     */
    private fun initQRCamera() {//QRコードリーダ起動

        //権限チェック
        val setPermisson =requireActivity().permissionChk()

        if (setPermisson) {

            // qr_view.resume()
            openQRCamera(
                "kenpin",
                qr_view,
                requireActivity(),
                token,
                projectId,
                data_qr_result_text,
                data_qr_message
            )
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

    override  fun onResume() {
        super.onResume()
        qr_view.resume()    // カメラ起動
    }

    override  fun onPause() {
        qr_view.pause()    // カメラ停止
        super.onPause()
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
                        messageText = message_text

                        //読み取りを伝えるバイブレーション
                        if(fragm != null){
                            vibrator = fragm.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        }

                        //Thread.sleep(500)
                        Log.d("QRCode", "処理実行！！")

                        result_text.text = "$result"

                        //ひとつ前のQRコードをこのQRコードにする。連続読み取りを避けるため。
                        oldResult = result.toString()

                        requireActivity().setResult(
                            Activity.RESULT_OK,
                            Intent().apply { putExtra("qr_code", oldResult) })


                        var payload2 = mapOf(
                            "token" to token,
                            "project_id" to projectId,
                            "serial_number" to "${result}"
                        )
                        //val intent = Intent(activity, QRActivity::class.java)


                        // onPauseがコールされるので不要
                        //qr_view.pause()

                        Log.d("OK", "終了")
                        requireActivity().finish()
                        //この時、エラーが帰ってきたら逃がす処理を追加する。
                    }

                }
            }
            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) { }
        })
    }
}
