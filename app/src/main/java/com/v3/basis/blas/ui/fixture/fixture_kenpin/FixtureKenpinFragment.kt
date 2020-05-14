package com.v3.basis.blas.ui.fixture.fixture_kenpin


import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.checkPermissions
import com.v3.basis.blas.ui.ext.getStringExtra
import com.v3.basis.blas.ui.ext.permissionChk
import kotlinx.android.synthetic.main.fragment_fixture_kenpin.*
import kotlinx.android.synthetic.main.fragment_qr.qr_view
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class FixtureKenpinFragment : Fragment() {

    companion object {
        const val REQUEST_CAMERA_PERMISSION:Int = 1
    }
    private lateinit var token: String
    private lateinit var projectId: String
    private lateinit var projectName: String
    private var parentChk :Boolean = true
    private val toastErrorLen = Toast.LENGTH_LONG
    private var cameraManager: CameraManager? = null
    private var cameraID: String? = null
    private var SW: Boolean = false
    private var oldResult:String? =null
    private var msg = BlasMsg()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("project_name")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras.getString("token").toString()
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras.getString("project_id").toString()
        }
        if (extras?.getString("project_name") != null) {
            projectName = extras.getString("project_name").toString()
        }
        return inflater.inflate(R.layout.fragment_fixture_kenpin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().checkPermissions()



        //ライト光るボタン実装
        //現在エラーが出ているので使用不可
        cameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?

        //ボタンがタップされたときの処理
        kenpinBtnLight.setOnClickListener{
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
        try {
            if(token != null && projectId != null && projectName != null) {
                //プロジェクト名をここで入れる。
                kenpin_project_name.text = projectName
                initQRCamera()
            }else{
                throw Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            kenpin_project_name.text = "内部データの取得に失敗しました。検品を実行できません"
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }
    }

    /**
     * カメラの初期化（カメラの起動・停止はonResume,onPauseにて実施する）
     */
    private fun initQRCamera() {//QRコードリーダ起動
        //権限チェック
        val setPermisson =requireActivity().permissionChk()

        if (setPermisson) {
            // qr_view.resume()
            FixtureActivity().openQRCamera(
                "kenpin",
                qr_view,
                requireActivity(),
                token,
                projectId,
                kenpin_result_text,
                kenpin_message)
        } else {//許可取れなかった場合、行う
            requestPermissions(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_CAMERA_PERMISSION)
        }
    }


    override  fun onResume() {
        super.onResume()
        qr_view.resume()    // カメラ起動
    }

    override  fun onPause() {
        qr_view.pause()    // カメラ停止
        super.onPause()
    }

    /*private fun openQRCamera() {
        qr_view.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {//QRコードを読み取った時の処理
                if(result.toString() != oldResult) {
                    if (result != null) {
                        //読み取りを伝えるバイブレーション


                        //Thread.sleep(500)
                        Log.d("QRCode", "処理実行！！")
                        kenpin_result_text.text = "$result"


                        //ひとつ前のQRコードをこのQRコードにする。連続読み取りを避けるため。
                        oldResult = result.toString()
                        onPause()

                        //restで更新する処理
                        var payload2 = mapOf(
                            "token" to token,
                            "project_id" to projectId,
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
    }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CAMERA_PERMISSION -> { initQRCamera() }
        }
    }
}

