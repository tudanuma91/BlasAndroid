package com.v3.basis.blas.ui.terminal.dashboards

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestInformation
import com.v3.basis.blas.ui.ext.getStringExtra
import com.v3.basis.blas.ui.item.item_view.ViewAdapter
import com.v3.basis.blas.ui.terminal.dashboards.dashbord_list_view.RowModel
import com.v3.basis.blas.ui.terminal.dashboards.dashbord_list_view.ViewAdapterAdapter
import kotlinx.android.synthetic.main.fragment_project.*
import org.json.JSONObject
import java.io.File

class DashboardsFragment : Fragment() {

    private lateinit var dashboardsViewModel: DashboardsViewModel
    private var token:String? =null
    private val dataList = mutableListOf<RowModel>()

    private val adapter:ViewAdapterAdapter = ViewAdapterAdapter(dataList,object  : ViewAdapterAdapter.ListListener{
        override fun onClickRow(tappedView: View, rowModel: RowModel) {
        }

    })

    /*private val adapter: ViewAdapter = ViewAdapter(dataList, object : ViewAdapter.ListListener {
        override fun onClickRow(tappedView: View, rowModel: RowModel) {
            //カードタップ時の処理
        }
    })*/


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dashboardsViewModel =
            ViewModelProviders.of(this).get(DashboardsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboards, container, false)
        token = getStringExtra("token") //トークンの値を取得
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        var payload = mapOf("token" to token)
        BlasRestInformation("search",payload, ::getInformationSuccess, ::getInformationError).execute()
    }


    private fun getInformationSuccess(result: JSONObject) {
        val informationList = RestHelper().createInformationList(result)
        dataList.addAll(createProjectList(informationList))
        adapter.notifyItemInserted(0)

    }


    private fun createProjectList(from: MutableMap<String,MutableMap<String, String?>>): List<RowModel> {
        val dataList = mutableListOf<RowModel>()
        from.forEach{
            Log.d("tqegfafwg","${it}")
            val informationList = it
            val data: RowModel =
                RowModel().also {
                    it.title = informationList.value["information_id"].toString()
                    it.detail = informationList.value["body"].toString()
                    it.file1 = informationList.value["file1"].toString()
                    it.file2 = informationList.value["file2"].toString()
                    it.file3 = informationList.value["file3"].toString()
                    it.token = token
                    it.informationId = informationList.value["information_id"].toString()

                }
            dataList.add(data)
        }
        return dataList
    }

    private fun getInformationError(error_code: Int, aplCode:Int) {

    }

}