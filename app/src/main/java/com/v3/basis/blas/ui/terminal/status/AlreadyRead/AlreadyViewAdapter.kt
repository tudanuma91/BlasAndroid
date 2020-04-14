package com.v3.basis.blas.ui.terminal.status.UnRead

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R

class AlreadyViewAdapter(private val list: List<AlreadyRowModel>, private val listener: ListListener) : RecyclerView.Adapter<AlreadyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlreadyViewHolder {
        Log.d("Life Cycle", "onCreateViewHolder")
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.list_status_already, parent, false)
        return AlreadyViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: AlreadyViewHolder, position: Int) {
        Log.d("Life Cycle", "onBindViewHolder")
        holder.titleView.text = list[position].title
        holder.detailView.text = list[position].detail
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
        fun onClickRow(tappedView: View, unReadRowModel: AlreadyRowModel)
    }
}
