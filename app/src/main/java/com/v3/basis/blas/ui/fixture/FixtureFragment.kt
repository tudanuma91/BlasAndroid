package com.v3.basis.blas.ui.fixture

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.QrcodeActivity


class FixtureFragment : Fragment() {

    private lateinit var fixtureViewModel: FixtureViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fixtureViewModel =
            ViewModelProviders.of(this).get(FixtureViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_fixture, container, false)
        val textView: TextView = root.findViewById(R.id.text_equip)
        fixtureViewModel.text.observe(this, Observer {
            textView.text = it
        })

        val loginBtn = root.findViewById<Button>(R.id.btnStartQr)
        loginBtn.setOnClickListener(ItemClickListener())
        return root
    }

    private inner class ItemClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //ログイン処理開始
            val intent = Intent(activity, QrcodeActivity::class.java)
            startActivity(intent)
        }
    }
}