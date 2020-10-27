package com.v3.basis.blas.ui.fixture.fixture_return


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.ui.ext.addTitleWithProjectName
import com.v3.basis.blas.ui.ext.checkPermissions
import com.v3.basis.blas.ui.ext.permissionChk
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment
import com.v3.basis.blas.ui.fixture.fixture_kenpin_multi.FixtureKenpinMultiFragment
import kotlinx.android.synthetic.main.fragment_fixture_return.*
import kotlinx.android.synthetic.main.fragment_qr.qr_view

/**
 * A simple [Fragment] subclass.
 */
class FixtureReturnFragment : Fragment() {

    companion object {
        const val REQUEST_CAMERA_PERMISSION:Int = 1
        fun newInstance() = FixtureReturnFragment()
    }
    lateinit var token:String
    lateinit var projectName:String
    lateinit var projectId:String
    private var McameraManager: CameraManager? = null
    //使用していないため、コメントアウトします
    //private var McameraID: String? = null
    private var SW: Boolean = false
    private var fragm : FragmentActivity? = null
    lateinit var root :View

    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitleWithProjectName("返却画面")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        //トークンとプロジェクトIDの取得
        activity?.intent?.extras?.apply {
            val extras = activity?.intent?.extras

            if(extras?.getString("token") != null) {
                token = extras.getString("token").toString()
            }
            if(extras?.getString("project_id") != null) {
                projectId = extras.getString("project_id").toString()
            }
            if(extras?.getString("project_name") != null) {
                projectName = extras.getString("project_name").toString()
            }
        }


        root = inflater.inflate(R.layout.fragment_fixture_return, container, false)



        //ライト光るボタン実装
        //現在エラーが出ているので使用不可
        //TODO : ここのエラーを解消すること！！


        val btn_light = root.findViewById<ImageButton>(R.id.returnBtnLight)
        McameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager?


        //ボタンがタップされたときの処理
        btn_light.setOnClickListener{
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
        val project_name =root.findViewById<TextView>(R.id.return_project_name)
        try {
            if(token != null && projectId != null && projectName != null) {
                project_name.text = projectName
                //許可取ってカメラを起動する
                fragm = activity
                //requireActivityのパーミッションチェックがエラー原因だった
                //requireActivity().checkPermissions()
                initQRCamera()
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            return_result_text.text = "内部データの取得に失敗しました。返却を実行できません。"
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
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
        var chkPermission = true

        grantResults.forEach {
            if(it != 0) {
                chkPermission = false
            }
        }
        if(chkPermission){
            initQRCamera()
        }else{
            Toast.makeText(getActivity(), "アクセス権限がありません。QRコード読み取りを実行できません。", Toast.LENGTH_LONG).show()
        }
    }


    fun callOnPouse(){
        onPause()
    }



}
