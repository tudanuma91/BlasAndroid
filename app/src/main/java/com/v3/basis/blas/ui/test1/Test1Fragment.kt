package com.v3.basis.blas.ui.test1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestFixture

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

        var payload = mapOf("token" to token, "project_id" to "1", "status" to "0")
        BlasRestFixture("search", payload, ::fixtureSuccess, ::fixtureError).execute()
        return view
    }

    private fun fixtureSuccess(result: MutableList<MutableMap<String, String?>>?) {
        Log.d("konishi", result.toString())
    }

    private fun fixtureError(error_code: Int) {
        var message: String? = null
        Log.d("konishi", error_code.toString())
        when (error_code) {
            BlasRestErrCode.NETWORK_ERROR -> {
                //サーバと通信できません
                message = getString(R.string.network_error)
            }
            else -> {
                //サーバでエラーが発生しました(要因コード)
                message = getString(R.string.server_error, error_code)
            }

        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        val intent = Intent(activity, TerminalActivity::class.java)
        //intent.putExtra("token",token)
        startActivity(intent)
    }



}
