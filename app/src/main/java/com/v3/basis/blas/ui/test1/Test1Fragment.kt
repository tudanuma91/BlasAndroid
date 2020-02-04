package com.v3.basis.blas.ui.test1

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.v3.basis.blas.R

class Test1Fragment : Fragment() {
    private var token:String? = null
    private var fragmentName = "Test1"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test1,container,false)
        //トークン取得
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
        }
        //トークンをtextViewにセット
        val test = view.findViewById<TextView>(R.id.text_test1)
        test.text ="token=${token}"
        //取得したトークンをログに出す
        Log.d("${fragmentName}","${token}")
        //変更を加えたviewを返す
        return view
    }

}
