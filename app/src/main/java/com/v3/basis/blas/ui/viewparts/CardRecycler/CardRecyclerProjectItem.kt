package com.v3.basis.blas.ui.viewparts.CardRecycler

import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.ui.terminal.project.project_list_view.RowModel

class CardRecyclerProjectItem(act: FragmentActivity?,
                              reView: RecyclerView):CardRecycler(act,reView) {

    private val dataList = mutableListOf<RowModel>()


    /**
     * マップ形式からリスト形式に変換する
     * @param projectのマップ形式のデータ
     * @return プロジェクトのリスト
     */
    fun createProjectList(from: MutableMap<String,MutableMap<String, String>>): MutableList<RowModel> {
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


}