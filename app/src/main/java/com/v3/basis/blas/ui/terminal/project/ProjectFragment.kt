package com.v3.basis.blas.ui.terminal.project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestProject
import com.v3.basis.blas.ui.ext.getStringExtra
import com.v3.basis.blas.ui.terminal.project.project_list_view.RowModel
import com.v3.basis.blas.ui.terminal.project.project_list_view.ViewAdapterAdapter
import kotlinx.android.synthetic.main.fragment_project.*
import org.json.JSONObject
import java.lang.Exception

/**
 * 表示・遷移などデータ管理画面にかかわる処理を行う。
 */
class ProjectFragment : Fragment() {

    private lateinit var homeViewModel: ProjectViewModel
    private var handler = Handler()
    lateinit var token:String
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG
    private var toastSuccessLen = Toast.LENGTH_SHORT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeViewModel = ViewModelProviders.of(this).get(ProjectViewModel::class.java)

        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null ) {
            token = extras.getString("token").toString() //トークンの値を取得
        }

        return inflater.inflate(R.layout.fragment_project, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            if(token != null) {
                /*プロジェクトの取得*/
                val payload = mapOf("token" to token)
                BlasRestProject(payload, ::projectSearchSuccess, ::projectSearchError).execute()
            }else{
                throw Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }

    }

    /**
     * マップ形式からリスト形式に変換する
     * @param projectのマップ形式のデータ
     * @return プロジェクトのリスト
     */
    private fun createProjectList(from: MutableMap<String,MutableMap<String, String>>): List<RowModel> {
        val dataList = mutableListOf<RowModel>()
        from.forEach{
            val project_name = it.value["project_name"].toString()
            val project_id = it.value["project_id"].toString()
            val data: RowModel =
                RowModel().also {
                    it.detail = project_id
                    it.title = project_name
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
    private fun projectSearchSuccess(result: JSONObject) {
        val newMap = RestHelper().createProjectList(result)
        val recyclerView = recyclerView
        var project_list = createProjectList(newMap)
        val adapter = ViewAdapterAdapter(project_list,
            object : ViewAdapterAdapter.ListListener {
                override fun onClickRow(tappedView: View, rowModel: RowModel) {
                    //Toast.makeText(activity, rowModel.title, toastSuccessLen).show()
                    Log.d(
                        "DataManagement",
                        "click_NAME => ${rowModel.title}/click_ID => ${rowModel.detail}"
                    )
                    val intent = Intent(activity, ItemActivity::class.java)
                    intent.putExtra("token", token)
                    intent.putExtra("project_id", rowModel.detail)
                    intent.putExtra("projectName", rowModel.title)
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
    private fun projectSearchError(error_code: Int , aplCode:Int) {
        var message: String? = null

        message = BlasMsg().getMessage(error_code,aplCode)

        handler.post {
            Toast.makeText(getActivity(), message, toastErrorLen).show()
        }
        val intent = Intent(activity, TerminalActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        super.onDestroyView()
    }

}


