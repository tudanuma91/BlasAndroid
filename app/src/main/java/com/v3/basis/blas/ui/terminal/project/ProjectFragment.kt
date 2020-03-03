package com.v3.basis.blas.ui.terminal.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.terminal.project.project_list_view.RowModel
import android.widget.Toast
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.rest.*
import com.v3.basis.blas.ui.terminal.project.project_list_view.ViewAdapterAdapter
import kotlinx.android.synthetic.main.fragment_project.*

/**
 * 表示・遷移などデータ管理画面にかかわる処理を行う。
 */
class ProjectFragment : Fragment() {

    private lateinit var homeViewModel: ProjectViewModel
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeViewModel = ViewModelProviders.of(this).get(ProjectViewModel::class.java)
        //トークンの値を取得
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras?.getString("token")
        }

        val root = inflater.inflate(R.layout.fragment_project, container, false)

        /*
        var payload2 = mapOf("token" to token, "project_id" to 13.toString())
        BlasRestFixture("search", payload2, ::fieldS, ::fieldE).execute()

        var payload3 = mapOf("token" to token, "project_id" to 1.toString(), "fld1" to "hello from android")
        BlasRestItem("create", payload3, ::itemCreateSuccess, ::itemCreateError).execute()

        var payload4 = mapOf("token" to token, "item_id" to 406.toString())
        BlasRestItem("delete", payload4, ::itemDeleteSuccess, ::itemDeleteError).execute()

        var payload5 = mapOf("token" to token,  "project_id" to 1.toString(), "item_id" to 404.toString(),  "fld2" to "hello from konishi")
        BlasRestItem("update", payload5, ::itemUpdateSuccess, ::itemUpdateError).execute()
        */

        //   var payload2 = mapOf("token" to token, "project_id" to 13.toString(), "status" to 0.toString(), "serial_number" to "aiueo")
        //   BlasRestFixture("create", payload2, ::fieldS, ::fieldE).execute()

        // var payload2 = mapOf("token" to token, "project_id" to 1.toString(), "item_id" to 401.toString())
        // BlasRestImage("donwload", payload2, ::fieldS, ::fieldE).execute()

        return root

    }

    private fun fieldS(result: MutableList<MutableMap<String, String?>>?) {
        val map = mutableMapOf<String, String>()
        var col = "1"
        //val cnt = 1
        if (result != null) {
            result.forEach {
                for ((k, v) in it) {
                    when (k) {
                        "col" -> {
                            col = v.toString()
                        }
                        "name" -> {
                            map["fld_${col}(name)"] = v.toString()
                        }
                        "type" -> {
                            map["fld_${col}(type)"] = v.toString()
                        }
                        "choice" -> {
                            map["fld_${col}(choice)"] = v.toString()
                        }
                    }
                    Log.d("konishi succcess", "${k}  ${v}")
                    /* Toast.makeText(getActivity(), "field success", Toast.LENGTH_LONG).show()
        if(result != null) {
            result.forEach {
                /*for((v, k) in it) {
                    Log.d("konishi", "${v} ${k}")
                }*/
                var base64_img = it["image"]
                if(context != null){
                    val img_byte = Base64.decode(base64_img, Base64.DEFAULT)
                    val fileOutputStream: FileOutputStream = context!!.openFileOutput("supepe.jpg", Context.MODE_PRIVATE)
                    fileOutputStream.write(img_byte)
                    fileOutputStream.close()

                    val fileInputStream: FileInputStream = context!!.openFileInput("supepe.jpg")
                    val bytes = fileInputStream.readBytes()
                    val aaa = Base64.encodeToString(bytes, Base64.DEFAULT)
                    //Log.d("konishi", base64)
                    Log.d("konishi", aaa)
                }*/


                }
                for ((k, v) in map) {
                    Log.d("fld1_name", "NAME => ${k}=>${v}")
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*プロジェクトの取得*/
        var payload = mapOf("token" to token)
        BlasRestProject(payload, ::projectSearchSuccess, ::projectSearchError).execute()

        /* テスト用 */
       // var payload2 = mapOf("token" to token, "project_id" to 1.toString())
        //BlasRestField(payload2, ::fieldS, ::fieldE).execute()

    }

        private fun fieldE(errorCode: Int) {
            Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
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

        /**
         * プロジェクトのデータ取得時にコールバックされる
         * @param  プロジェクト名 to プロジェクトIDのマップ形式
         * @return なし
         */
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
                        val intent = Intent(activity, ItemActivity::class.java)
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


    }


