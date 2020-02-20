package com.v3.basis.blas.ui.terminal.fixture

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity


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


        val loginBtn = root.findViewById<Button>(R.id.btnStartQr)
        loginBtn.setOnClickListener(ItemClickListener())
        return root
    }

    private inner class ItemClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //ログイン処理開始
            val intent = Intent(activity, FixtureActivity::class.java)
            startActivity(intent)
        }
    }
}