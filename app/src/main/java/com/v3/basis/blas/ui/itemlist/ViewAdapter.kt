package com.v3.basis.blas.ui.itemlist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R

class ViewAdapter(private val list: List<RowModel>, private val listener: ListListener) : RecyclerView.Adapter<ViewHolders>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolders {
        Log.d("Adapter", "onCreateViewHolder")
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.test3_item_list, parent, false)
        return ViewHolders(rowView)
    }

    override fun onBindViewHolder(holder:ViewHolders , position: Int) {
        Log.d("Adapter", "onBindViewHolder")
        holder.titleView.text = list[position].title
        holder.detailView.text = list[position].detail
        holder.itemView.setOnClickListener {
            listener.onClickRow(it, list[position])
        }
    }

    override fun getItemCount(): Int {
        Log.d("Adapter", "getItemCount")
        return list.size
    }

    interface ListListener {
        fun onClickRow(tappedView: View, rowModel: RowModel)
    }
}