package com.v3.basis.blas.ui.test3.Test3Second

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.Test3Activity

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Test3SecondFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Test3SecondFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Test3SecondFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //もとになるviewの取得
        val root = inflater.inflate(R.layout.fragment_test3_second, container, false)
        //ボタンの取得
        val button = root.findViewById<Button>(R.id.btn_test3)
        val button2 = root.findViewById<Button>(R.id.btn_test3_2)
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
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }
}