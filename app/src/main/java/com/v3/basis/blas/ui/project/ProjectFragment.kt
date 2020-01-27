package com.v3.basis.blas.ui.project

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
import com.v3.basis.blas.ui.project.items_project_view.ItemsProjectViewAdapterAdapter
import com.v3.basis.blas.ui.project.items_project_view.RowModel
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

/**
 * 表示・遷移などデータ管理画面にかかわる処理を行う。
 */
class ProjectFragment : Fragment() {

    private lateinit var homeViewModel: ProjectViewModel
    val GET_PROJECT_URL = BlasRestProject().GET_PGOJECT_URL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(ProjectViewModel::class.java)
        //トークンの値を取得
        val extras = activity?.intent?.extras
        val token = extras?.getString("token")
        Log.d("【DataManagement】", "Token:${token}")

        //プロジェクトの取得
        val receiver = ProjectReceiver()
        receiver.execute(token, null)

        val root = inflater.inflate(R.layout.fragment_data_management, container, false)
        /** val textView: TextView = root.findViewById(R.id.text_data_management)
        homeViewModel.text.observe(this, Observer {
        textView.text = it
        })*/

        //リストタップ時の処理
        //val lvProject = root.findViewById<ListView>(R.id.project_listView)
        // lvProject?.onItemClickListener = ListItemClickListener()

        return root
    }


    /**
     * プロジェクト一覧を取得する処理
     */
    private inner class ProjectReceiver() : AsyncTask<String, String, String>() {
        /**
         * APIを使用して、プロジェクト一覧を取得
         */
        override fun doInBackground(vararg params: String?): String? {
            val key = listOf("token", "name")
            //レスポンスデータを取得
            //レスポンスデータをJSON文字列にする
            val response = BlasRestProject().getResponseData(params, key, "GET", GET_PROJECT_URL)

            return response
        }

        /**
         * 取得したプロジェクトを画面に表示する処理
         */
        override fun onPostExecute(response: String) {
            //listView を取得する
            val projectList = BlasRestProject().getProject(response)
            /*val from = arrayOf("name","id")
            val to = intArrayOf(android.R.id.text1,android.R.id.text2)
            val adapter = SimpleAdapter(activity,projectList,android.R.layout.simple_expandable_list_item_2,from,to)
            lvProjectList?.adapter = adapter
            Log.d("testtesttesttesttest","${projectList}")*/
            val recyclerView = view?.findViewById<RecyclerView>(R.id.recycler_list)
            val adapter = ItemsProjectViewAdapterAdapter(
                createDataList(projectList),
                object : ItemsProjectViewAdapterAdapter.ListListener {
                    override fun onClickRow(tappedView: View, rowModel: RowModel) {
                        Toast.makeText(activity, rowModel.title, Toast.LENGTH_LONG).show()
                    }
                })

            recyclerView?.setHasFixedSize(true)
            recyclerView?.layoutManager = LinearLayoutManager(activity)
            recyclerView?.adapter = adapter


        }
    }

    /**
     * リストをタップした時の処理
     */
    /* private inner class   ListItemClickListener : AdapterView.OnItemClickListener{
         override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
             Log.d("DataManagement", "start")
             val item = parent?.getItemAtPosition(position) as MutableMap<String,String>
             val pjName = item["name"]
             val pjId = item["id"]
             Log.d("DataManagement", "click_ID => ${pjId}/click_NAME => ${pjName}")
         }
     }*/

    private fun createDataList(from: MutableList<MutableMap<String, String>>): List<RowModel> {
        Log.d("Life Cycle", "createDataList")
        Log.d("testtesttesttesttest", "${from.size}")
        val dataList = mutableListOf<RowModel>()

        for (i in from) {
            val data: RowModel =
                RowModel().also {
                    it.detail = i.get("id").toString()
                    it.text = i.get("name").toString()
                }
            dataList.add(data)
        }
        return dataList
    }
}

