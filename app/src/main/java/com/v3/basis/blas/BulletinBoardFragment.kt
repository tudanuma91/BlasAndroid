package com.v3.basis.blas


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * A simple [Fragment] subclass.
 */
class BulletinBoardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bulletin_board,container,false)
        val intent = activity?.intent
        val extras = intent?.extras
        val token= extras?.getString("token")
        Log.d("【Bulletin】", "token:${token}")
        val tokenText = view.findViewById<TextView>(R.id.tokenText)
        tokenText.text = token
        return view
    }


}
