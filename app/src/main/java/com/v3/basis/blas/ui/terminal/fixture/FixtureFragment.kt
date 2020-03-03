package com.v3.basis.blas.ui.terminal.fixture

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.FixtureActivity
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestProject
import com.v3.basis.blas.ui.terminal.fixture.project_list_view.RowModel
import com.v3.basis.blas.ui.terminal.fixture.project_list_view.ViewAdapterAdapter
import kotlinx.android.synthetic.main.fragment_project.*


class FixtureFragment : Fragment() {

    private lateinit var fixtureViewModel: FixtureViewModel
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fixtureViewModel =
            ViewModelProviders.of(this).get(FixtureViewModel::class.java)
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras?.getString("token")
        }

        val root = inflater.inflate(R.layout.fragment_fixture, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var payload = mapOf("token" to token)
        BlasRestProject(payload, ::projectSearchSuccess, ::projectSearchError).execute()
    }


    private inner class ItemClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //ログイン処理開始
            val intent = Intent(activity, FixtureActivity::class.java)
            startActivity(intent)
        }
    }

    private fun projectSearchSuccess(result: MutableMap<String, Int>) {

        val recyclerView = recycler_list
        var project_list = createProjectList(result)
        val adapter = ViewAdapterAdapter(project_list,
            object : ViewAdapterAdapter.ListListener {
                override fun onClickRow(tappedView: View, rowModel: RowModel) {
                    Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
                    Log.d(
                        "DataManagement",
                        "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
                    )
                    val intent = Intent(activity, FixtureActivity::class.java)
                    intent.putExtra("token", token)
                    intent.putExtra("project_id", rowModel.detail)
                    startActivity(intent)
                }
            })

        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = adapter

    }


    /**
     * プロジェクト取得失敗時
     * @param  error_code 失敗した要因コード
     */
    private fun projectSearchError(error_code: Int) {
        var message: String? = null

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

    /**
     * マップ形式からリスト形式に変換する
     * @param projectのマップ形式のデータ
     * @return プロジェクトのリスト
     */
    private fun createProjectList(from: MutableMap<String, Int>): List<RowModel> {
        val dataList = mutableListOf<RowModel>()

        for ((project_name, project_id) in from) {
            val data: RowModel =
                RowModel().also {
                    it.detail = project_id.toString()
                    it.title = project_name
                }
            dataList.add(data)
        }
        return dataList
    }
}