package com.v3.basis.blas.ui.test3.Test3Second

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.Test3Activity
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Test3SecondFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Test3SecondFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Test3SecondFragment : Fragment() {
    var token :String? = null
    private var Mbutton: Button? = null
    private var McameraManager: CameraManager? = null
    private var McameraID: String? = null
    private var SW: Boolean = false

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //もとになるviewの取得
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
        }

        val root = inflater.inflate(R.layout.fragment_test3_second, container, false)
        //ボタンの取得
        val button = root.findViewById<Button>(R.id.btn_test3)
        val button2 = root.findViewById<Button>(R.id.btn_test3_2)
        val button3 = root.findViewById<Button>(R.id.button_test3_item)
        Mbutton = root.findViewById<Button>(R.id.btnLightKenpin)

        //McameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        McameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
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



        //アクティビティの取得
        val test3Activity = activity as Test3Activity?
        //フラグメントの作成
        val second2Fragment = Test3Second2Fragment()
        val second3Fragment = Test3Second3Fragment()
        button.setOnClickListener{
            //fragment-Lの切り替え
           // test3Activity?.displayManager(2)
            //fragment-Sの切り替え
            test3Activity?.replaceFragment(second2Fragment)
            Log.d("aaa","よばれたよ！！")
        }
        button2.setOnClickListener{
            test3Activity?.replaceFragment(second3Fragment)
            Log.d("aaa","よばれたよ！！")
        }
        button3.setOnClickListener{
            val intent = Intent(activity, ItemActivity::class.java)
            intent.putExtra("token",token)
            intent.putExtra("project_id","1")
            startActivity(intent)
        }
        Mbutton!!.setOnClickListener{
            if(McameraID == null){
                return@setOnClickListener;
            }
            try {
                if(SW == false){
                    //SWがfalseならばトーチモードをtrueにしてLDEオン
                    McameraManager!!.setTorchMode(McameraID!!, true);
                }else{
                    McameraManager!!.setTorchMode(McameraID!!, false);
                    //SWがtrueならばトーチモードをfalseにしてLDEオフ
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }
}
