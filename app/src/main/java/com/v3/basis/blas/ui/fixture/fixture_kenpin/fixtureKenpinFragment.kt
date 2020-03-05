package com.v3.basis.blas.ui.fixture.fixture_kenpin


import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.os.*
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import kotlinx.android.synthetic.main.fragment_fixture_kenpin.*
import kotlinx.android.synthetic.main.fragment_qr.*
import kotlinx.android.synthetic.main.fragment_qr.qr_view
import org.json.JSONObject
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class fixtureKenpinFragment : Fragment() {

    companion object {
        const val REQUEST_CAMERA_PERMISSION:Int = 1
    }
    private var token:String? = null
    private var project_id:String? = null
    private var McameraManager: CameraManager? = null
    private var McameraID: String? = null
    private var SW: Boolean = false
    private var oldResult:String? =null


    //APIレベル調べて、達していない場合できませんって表示をすること
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        //トークンとプロジェクトIDの取得
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
            Log.d("token_fixture","${token}")
        }
        if(extras?.getString("project_id") != null) {
            project_id = extras?.getString("project_id")
            Log.d("project_id","${project_id}")
        }


        val root =  inflater.inflate(R.layout.fragment_fixture_kenpin, container, false)
        //プロジェクト名をここで入れる。
        val project_name =root.findViewById<TextView>(R.id.project_name)
        project_name.text = "ここにプロジェクト名が入る"


        //ライト光るボタン実装
        //現在エラーが出ているので使用不可
        //TODO : ここのエラーを解消すること！！

        val btn_light = root.findViewById<ImageButton>(R.id.btnLight2)
        McameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        //Log.d("kentakijima","${McameraManager!!.cameraIdList}")
        val list = McameraManager!!.cameraIdList
        list.forEach {
            Log.d("kentakijima","${it}")
        }
        McameraManager!!.registerTorchCallback(object : CameraManager.TorchCallback() {
            //トーチモードが変更された時の処理
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                //カメラIDをセット
                McameraID = cameraId
                //SWに現在の状態をセット
                SW = enabled
            }
        }, Handler())
        //ボタンがタップされたときの処理
        btn_light.setOnClickListener{
            if(McameraID == null){
                Log.d("null","nullだったよ")
            }
            try {
                if(SW == false){
                    //SWがfalseならばトーチモードをtrueにしてLDEオン
                    McameraManager!!.setTorchMode(McameraID!!, true)
                }else{
                    //SWがtrueならばトーチモードをfalseにしてLDEオフ
                    McameraManager!!.setTorchMode(McameraID!!, false)
                }

            } catch (e: Exception) {
                //エラー処理
                e.printStackTrace();
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //許可取ってカメラを起動する
        checkPermissions()
        initQRCamera()
    }

    private fun checkPermissions() {//許可取り
        // すでに許可をしていた場合はスキップする
        if (context?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.CAMERA) } == PackageManager.PERMISSION_GRANTED) {
            qr_view.resume()
        }

        if (activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.CAMERA) }!!) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.CAMERA), 999)
        }
    }


    private fun initQRCamera() {//QRコードリーダ起動

        //権限チェック
        val isReadPermissionGranted = (activity?.let {
            PermissionChecker.checkSelfPermission(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } == PackageManager.PERMISSION_GRANTED)
        val isWritePermissionGranted = (activity?.let {
            PermissionChecker.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } == PackageManager.PERMISSION_GRANTED)
        val isCameraPermissionGranted = (activity?.let {
            PermissionChecker.checkSelfPermission(
                it,
                Manifest.permission.CAMERA
            )
        } == PackageManager.PERMISSION_GRANTED)


        //カメラの起動
        if (isReadPermissionGranted && isWritePermissionGranted && isCameraPermissionGranted) {
            openQRCamera()
        } else {
            requestPermissions(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun openQRCamera() {
        qr_view.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {//QRコードを読み取った時の処理
                if(result.toString() != oldResult) {
                    if (result != null) {
                        //読み取りを伝えるバイブレーション
                        val vibrator =
                            activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        val vibrationEffect = VibrationEffect.createOneShot(300, DEFAULT_AMPLITUDE)
                        vibrator.vibrate(vibrationEffect)

                        //Thread.sleep(500)
                        Log.d("QRCode", "処理実行！！")
                        kenpin_result_text.text = "$result"


                        //ひとつ前のQRコードをこのQRコードにする。連続読み取りを避けるため。
                        oldResult = result.toString()
                        onPause()

                        //restで更新する処理
                        var payload2 = mapOf(
                            "token" to token,
                            "project_id" to project_id,
                            "serial_number" to "${result}"
                        )
                        BlasRestFixture("kenpin", payload2, ::success, ::error).execute()
                        Log.d("OK", "終了")
                        //この時、エラーが帰ってきたら逃がす処理を追加する。
                    }

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

    private fun success(result: JSONObject){
        Log.d("OK","作成完了")
        message_kenpin.setTextColor(Color.GREEN)
        message_kenpin.text = "QRコードを読み取りました"
    }

    private fun error(errorCode: Int){
        Log.d("NG","作成失敗")
        Log.d("errorCorde","${errorCode}")
        message_kenpin.setTextColor(Color.RED)
        message_kenpin.text = "すでに登録済のQRコードです"
    }


}

