package com.v3.basis.blas.ui.data_management

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRest
import com.v3.basis.blas.blasclass.rest.BlasRestProject
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 表示・遷移などデータ管理画面にかかわる処理を行う。
 */
class DataManagementFragment : Fragment() {

    private lateinit var homeViewModel: DataManagementViewModel
    //val GET_PROJECT_URL = BlasRestProject().GET_PGOJECT_URL

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(DataManagementViewModel::class.java)
        //トークンの値を取得
        val extras = activity?.intent?.extras
        val token = extras?.getString("token")
        Log.d("【DataManagement】", "Token:${token}")

        //プロジェクトの取得
        val receiver = ProjectReceiver()
        receiver.execute(token,null)

        val root = inflater.inflate(R.layout.fragment_data_management, container, false)
        val textView: TextView = root.findViewById(R.id.text_data_management)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })

        //リストタップ時の処理
        val lvProject = root.findViewById<ListView>(R.id.project_listView)
        lvProject?.onItemClickListener = ListItemClickListener()

        return root
    }


    /**
     * プロジェクト一覧を取得する処理
     */
    private inner class  ProjectReceiver() : AsyncTask<String, String, String>(){
        /**
         * APIを使用して、プロジェクト一覧を取得
         */
        override fun doInBackground(vararg params: String?): String? {
            val key = listOf("token","name")
            //レスポンスデータを取得
            //レスポンスデータをJSON文字列にする
           // val response = super.getResponseData(params,key,"GET",GET_PROJECT_URL)

            return null
        }

        /**
         * 取得したプロジェクトを画面に表示する処理
         */
        override fun onPostExecute(response: String) {
            //listView を取得する
            val lvProjectList = view?.findViewById<ListView>(R.id.project_listView)
            val projectList = BlasRestProject().getProject(response)
            val from = arrayOf("name","id")
           // val Name = arrayOf("name")
            val to = intArrayOf(android.R.id.text1,android.R.id.text2)
            val adapter = SimpleAdapter(activity,projectList,android.R.layout.simple_expandable_list_item_2,from,to)
            lvProjectList?.adapter = adapter

        }
    }

    /**
     * リストをタップした時の処理
     */
    private inner class   ListItemClickListener : AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            Log.d("DataManagement", "start")
            val item = parent?.getItemAtPosition(position) as MutableMap<String,String>
            val pjName = item["name"]
            val pjId = item["id"]
            Log.d("DataManagement", "click_ID => ${pjId}/click_NAME => ${pjName}")
        }
    }

}