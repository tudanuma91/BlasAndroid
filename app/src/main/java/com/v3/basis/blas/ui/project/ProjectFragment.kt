package com.v3.basis.blas.ui.project

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRestProject
import com.v3.basis.blas.ui.project.project_list_view.ItemsProjectViewAdapterAdapter
import com.v3.basis.blas.ui.project.project_list_view.RowModel
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestOrgs
import com.v3.basis.blas.blasclass.rest.BlasRestItem

/**
 * 表示・遷移などデータ管理画面にかかわる処理を行う。
 */
class ProjectFragment : Fragment() {

    private lateinit var homeViewModel: ProjectViewModel
    private var token:String? = null
    override fun onCreateView(inflater: LayoutInflater,
                               container: ViewGroup?,
                                savedInstanceState: Bundle?): View? {

        homeViewModel = ViewModelProviders.of(this).get(ProjectViewModel::class.java)
        //トークンの値を取得
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
        }

        //プロジェクトの取得
        var payload = mapOf("token" to token)
        BlasRestProject(payload, ::projectSearchSuccess, ::projectSearchError).execute()

        val root = inflater.inflate(R.layout.fragment_data_management, container, false)


        /* テスト用 */
        var payload2 = mapOf("token" to token, "project_id" to 13.toString())
        BlasRestItem("search", payload2, ::fieldS, ::fieldE).execute()

        var payload3 = mapOf("token" to token, "project_id" to 1.toString(), "fld1" to "hello from android")
        BlasRestItem("create", payload3, ::itemCreateSuccess, ::itemCreateError).execute()

        var payload4 = mapOf("token" to token, "item_id" to 406.toString())
        BlasRestItem("delete", payload4, ::itemDeleteSuccess, ::itemDeleteError).execute()

        var payload5 = mapOf("token" to token,  "project_id" to 1.toString(), "item_id" to 404.toString(),  "fld2" to "hello from konishi")
        BlasRestItem("update", payload5, ::itemUpdateSuccess, ::itemUpdateError).execute()

        return root
    }

    private fun itemCreateSuccess(result:MutableList<MutableMap<String,String?>>?) {
        Toast.makeText(getActivity(), "追加しました", Toast.LENGTH_LONG).show()
    }
    private fun itemDeleteSuccess(result:MutableList<MutableMap<String,String?>>?) {
        Toast.makeText(getActivity(), "削除しました", Toast.LENGTH_LONG).show()
    }
    private fun itemUpdateSuccess(result:MutableList<MutableMap<String,String?>>?) {
        Toast.makeText(getActivity(), "更新しました", Toast.LENGTH_LONG).show()
    }

    private fun itemCreateError(errorCode:Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
    }

    private fun itemDeleteError(errorCode:Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
    }
    private fun itemUpdateError(errorCode:Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
    }
    private fun fieldS(result:MutableList<MutableMap<String,String?>>?) {
        Toast.makeText(getActivity(), "field success", Toast.LENGTH_LONG).show()
    }

    private fun fieldE(errorCode:Int) {
        Toast.makeText(getActivity(), "field error", Toast.LENGTH_LONG).show()
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
                    it.text = project_name
                }
            dataList.add(data)
        }
        return dataList
    }

    /**
     * プロジェクトのデータ取得時にコールバックされる
     * @param  プロジェクト名 to プロジェクトIDのマップ形式
     * @return なし
     */
    private fun projectSearchSuccess(result:MutableMap<String,Int>) {

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycler_list)
        var project_list = createProjectList(result)
        val adapter = ItemsProjectViewAdapterAdapter(project_list,
            object : ItemsProjectViewAdapterAdapter.ListListener {
                override fun onClickRow(tappedView: View, rowModel: RowModel) {
                    Toast.makeText(activity, rowModel.text, Toast.LENGTH_LONG).show()
                    Log.d("DataManagement", "click_NAME => ${rowModel.text}/click_ID => ${rowModel.detail}")
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
    private fun projectSearchError(error_code:Int) {
        var message:String? = null

        when(error_code) {
            BlasRestErrCode.NETWORK_ERROR->{
                //サーバと通信できません
                message = getString(R.string.network_error)
            }
            else-> {
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

