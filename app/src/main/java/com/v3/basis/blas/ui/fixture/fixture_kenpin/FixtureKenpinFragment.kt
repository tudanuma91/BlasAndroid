package com.v3.basis.blas.ui.fixture.fixture_kenpin


import android.content.Context
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.service.BlasSyncMessenger
import com.v3.basis.blas.blasclass.service.SenderHandler
import com.v3.basis.blas.ui.common.FixtureBaseFragment
import com.v3.basis.blas.ui.ext.addTitleWithProjectName
import com.v3.basis.blas.ui.common.QRCameraFragment
import kotlinx.android.synthetic.main.fragment_fixture_kenpin.*
import kotlinx.android.synthetic.main.fragment_qr.qr_view
import java.lang.Exception
import kotlin.concurrent.withLock

/**
 * A simple [Fragment] subclass.
 */
class FixtureKenpinFragment : FixtureBaseFragment() {

    companion object {
        fun newInstance() = FixtureKenpinFragment()
    }

    private val toastErrorLen = Toast.LENGTH_LONG
    private var cameraManager: CameraManager? = null
    private var SW: Boolean = false
    private var msg = BlasMsg()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitleWithProjectName("検品画面")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_fixture_kenpin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ライト光るボタン実装
        //現在エラーが出ているので使用不可
        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager?

        //ボタンがタップされたときの処理
        kenpinBtnLight.setOnClickListener{
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
                //requireActivityのパーミッションチェックがエラー原因だった
                //  requireActivity().checkPermissions()
                kenpin_project_name.text = projectName
            }else{
                throw Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            kenpin_result_text.setTextColor(Color.RED)
            kenpin_result_text.text = "内部データの取得に失敗しました。検品を実行できません"
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }
    }

    override fun onResume() {
        startCamera(qr_view, ::kenpinCallBack)
        super.onResume()
    }

    /**
     * カメラがQRコードを読み込んだときにコールバックされる
     */
    private fun kenpinCallBack(code:String) {
        //読み取った値を表示画面に送る
        kenpin_result_text.text = code

        Thread(Runnable {
            //検品データをLDBに保存する
            SenderHandler.lock.withLock {
                val fixtureController = FixtureController(BlasApp.applicationContext(), projectId)
                fixtureController.kenpin(code)
            }

            //BLASにデータ送信の合図を送る
            BlasSyncMessenger.notifyBlasFixtures(token, projectId)
        }).start()
    }
}

