package com.v3.basis.blas.ui.test1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.work.*

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.db.BlasSQLDataBaseHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestFixture
import com.v3.basis.blas.blasclass.rest.BlasRestProject
import java.util.concurrent.TimeUnit
import android.net.NetworkInfo
import android.net.ConnectivityManager
import org.json.JSONObject


class Test1Fragment : Fragment() {
    private lateinit var token:String
    private var fragmentName = "Test1"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test1,container,false)
        //トークン取得
        val extras = activity?.intent?.extras
        token = extras?.getString("token")?:""

        val prjBtn = view.findViewById<Button>(R.id.prj_button) as Button

        prjBtn.setOnClickListener {
            Toast.makeText(activity, "プロジェクト取得処理開始", Toast.LENGTH_LONG).show()
            getProject(it, token)
        }

        return view
    }

    private fun getProject(view:View, token:String) {
        //トークンをtextViewにセット
        //取得したトークンをログに出す
        var payload = mapOf("token" to token, "project_id" to "1", "status" to "0")
        BlasRestProject(payload, ::projectSuccess, ::projectError).execute()
    }

    private fun projectSuccess(result:JSONObject?) {
        Log.d("konishi", result.toString())
    }

    private fun projectError(error_code: Int) {
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
