package com.v3.basis.blas.ui.fixture.fixture_return


import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.checkPermissions
import com.v3.basis.blas.ui.ext.permissionChk
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import kotlinx.android.synthetic.main.fragment_fixture_return.*
import kotlinx.android.synthetic.main.fragment_qr.qr_view

/**
 * A simple [Fragment] subclass.
 */
class FixtureReturnFragment : Fragment() {

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
        super.onCreateView(inflater, container, savedInstanceState)

        //トークンとプロジェクトIDの取得
        activity?.intent?.extras?.apply {
            if(getString("token") != null) {
                token = getString("token")
                Log.d("token_fixture","${token}")
            }
            if(getString("project_id") != null) {
                projectId = getString("project_id")
                Log.d("project_id","${projectId}")
            }
            if(getString("project_name") != null) {
                projectName = getString("project_name")
                Log.d("project_name","${projectName}")
            }
        }


        val root = inflater.inflate(R.layout.fragment_fixture_return, container, false)
        //プロジェクト名をここで入れる。
        val project_name =root.findViewById<TextView>(R.id.return_project_name)
        project_name.text = projectName


        //ライト光るボタン実装
        //現在エラーが出ているので使用不可
        //TODO : ここのエラーを解消すること！！

        val btn_light = root.findViewById<ImageButton>(R.id.returnBtnLight)
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
        fragm = activity
        requireActivity().checkPermissions()
        initQRCamera()
    }

    private fun initQRCamera() {//QRコードリーダ起動
        //権限チェック
        val setPermisson = requireActivity().permissionChk()

        //カメラの起動
        if (setPermisson) {
            qr_view.resume()
            FixtureActivity().openQRCamera(
                "return",
                qr_view,
                fragm,
                token,
                projectId,
                return_result_text,
                return_message
            )
        } else {
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
            REQUEST_CAMERA_PERMISSION -> { initQRCamera() }
        }
    }


    fun callOnPouse(){
        onPause()
    }



}
