package com.v3.basis.blas.ui.fixture.fixture_kenpin


import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
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
    private var token:String? = null
    private var McameraManager: CameraManager? = null
    private var McameraID: String? = null
    private var SW: Boolean = false
    private var oldResult:String? =null
    private var projectName:String? =null
    private var projectId:String? = null
    private var fragm :FragmentActivity? = null

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
            projectId = extras?.getString("project_id")
            Log.d("project_id","${projectId}")
        }
        if(extras?.getString("project_name") != null) {
            projectName = extras?.getString("project_name")
            Log.d("project_name","${projectName}")
        }



        val root =  inflater.inflate(R.layout.fragment_fixture_kenpin, container, false)
        //プロジェクト名をここで入れる。
        val project_name =root.findViewById<TextView>(R.id.kenpin_project_name)
        project_name.text = projectName


        //ライト光るボタン実装
        //現在エラーが出ているので使用不可
        //TODO : ここのエラーを解消すること！！

        val btn_light = root.findViewById<ImageButton>(R.id.kenpinBtnLight)
        McameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?

        //ボタンがタップされたときの処理
        btn_light.setOnClickListener{
            if(McameraID == null){
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
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //許可取ってカメラを起動する
        fragm = activity

        FixtureActivity().checkPermissions(fragm)
        initQRCamera()
    }


    private fun initQRCamera() {//QRコードリーダ起動
        //権限チェック
        val setPermisson =FixtureActivity().permissionChk(fragm)
        //カメラの起動
        if (setPermisson) {
            qr_view.resume()
            FixtureActivity().openQRCamera(
                "kenpin",
                qr_view,
                fragm,
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

    /*private fun openQRCamera() {
        qr_view.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {//QRコードを読み取った時の処理
                if(result.toString() != oldResult) {
                    if (result != null) {
                        //読み取りを伝えるバイブレーション
                        vibrator = activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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

    fun callOnPouse(){
        onPause()
    }


}

