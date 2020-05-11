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
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
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
    private var token:String? = null
    private var cameraManager: CameraManager? = null
    private var cameraID: String? = null
    private var SW: Boolean = false
    private var oldResult:String? =null
    private var projectName:String? =null
    private var projectId:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("project_name")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        token = getStringExtra("token")
        projectId = getStringExtra("project_id")
        projectName = getStringExtra("project_name")

        return inflater.inflate(R.layout.fragment_fixture_kenpin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().checkPermissions()

        //プロジェクト名をここで入れる。
        kenpin_project_name.text = projectName


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

        initQRCamera()
    }


    private fun initQRCamera() {//QRコードリーダ起動
        //権限チェック
        val setPermisson =requireActivity().permissionChk()
        //カメラの起動
        if (setPermisson) {
            qr_view.resume()
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

