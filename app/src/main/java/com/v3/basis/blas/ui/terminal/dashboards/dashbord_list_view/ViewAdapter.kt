package com.v3.basis.blas.ui.terminal.dashboards.dashbord_list_view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R

class ViewAdapterAdapter(private val list: List<RowModel>, private val listener: ListListener) : RecyclerView.Adapter<HomeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        Log.d("Life Cycle", "onCreateViewHolder")
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.list_information, parent, false)
        return HomeViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        Log.d("Life Cycle", "onBindViewHolder")
        holder.titleView.text = list[position].title
        holder.detailView.text = list[position].detail
        holder.fileView1.text = list[position].file1
        holder.fileView2.text = list[position].file2
        holder.fileView3.text = list[position].file3
        holder.informationId = list[position].informationId
        holder.token = list[position].token

        //ここにタップ時に受け渡す値を入力する？
        holder.itemView.setOnClickListener {
            listener.onClickRow(it, list[position])
        }
    }

    override fun getItemCount(): Int {
        Log.d("Life Cycle", "getItemCount")
        return list.size
    }

    interface ListListener {
        fun onClickRow(tappedView: View, rowModel: RowModel)
    }
}
