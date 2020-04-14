package com.v3.basis.blas.ui.viewparts.CardRecycler

import android.database.Cursor
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.ui.terminal.dashboards.dashbord_list_view.RowModel
import com.v3.basis.blas.ui.terminal.dashboards.dashbord_list_view.ViewAdapterAdapter


open class CardRecyclerDashboard (act: FragmentActivity?,
                                  reView: RecyclerView,
                                  adapter: ViewAdapterAdapter):CardRecycler(act,reView){
    private val adapter = adapter
    private val dataList = mutableListOf<RowModel>()

    override fun createRecyclerView(): RecyclerView {
        val recycle = super.createRecyclerView()
        recycle.adapter = adapter
        return recycle
    }


    fun createDataList(from: MutableMap<String,MutableMap<String, String?>>,token:String): MutableList<RowModel> {
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

}