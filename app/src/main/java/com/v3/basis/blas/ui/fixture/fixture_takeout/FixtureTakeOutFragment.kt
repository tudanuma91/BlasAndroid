package com.v3.basis.blas.ui.fixture.fixture_takeout

import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
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
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.checkPermissions
import com.v3.basis.blas.ui.ext.permissionChk
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import com.v3.basis.blas.ui.fixture.fixture_return.FixtureReturnFragment
import kotlinx.android.synthetic.main.fragment_fixture_takeout.*
import kotlinx.android.synthetic.main.fragment_qr.qr_view
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FixtureTakeOutFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FixtureTakeOutFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FixtureTakeOutFragment : Fragment() {
    companion object {
        const val REQUEST_CAMERA_PERMISSION:Int = 1
    }
    private var token:String? = null
    private var McameraManager: CameraManager? = null
    private var McameraID: String? = null
    private var SW: Boolean = false
    private var projectName:String? =null
    private var projectId:String? = null
    private var fragm : FragmentActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("project_name")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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


        val root = inflater.inflate(R.layout.fragment_fixture_takeout, container, false)

        //プロジェクト名をここで入れる。
        val project_name =root.findViewById<TextView>(R.id.takeout_project_name)
        project_name.text = projectName


        //ライト光るボタン実装
        //現在エラーが出ているので使用不可

        val btn_light = root.findViewById<ImageButton>(R.id.takeoutBtnLight)
        McameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?


        //ボタンがタップされたときの処理
        btn_light.setOnClickListener{
            if(McameraID == null){
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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //許可取ってカメラを起動する
        try {
            if(token != null && projectId != null && projectName != null) {
                fragm = activity
                requireActivity().checkPermissions()
                initQRCamera()
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){

        }
    }

    /**
     * カメラの初期化（カメラの起動・停止はonResume,onPauseにて実施する）
     */
    private fun initQRCamera() {//QRコードリーダ起動
        //権限チェック
        val setPermisson = requireActivity().permissionChk()

        if (setPermisson) {
            // qr_view.resume()
            FixtureActivity().openQRCamera(
                "takeout",
                qr_view,
                fragm,
                token,
                projectId,
                takeout_result_text,
                takeout_message
                )
        } else {
            requestPermissions(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), FixtureKenpinFragment.REQUEST_CAMERA_PERMISSION
            )
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
