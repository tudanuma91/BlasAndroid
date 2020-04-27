package com.v3.basis.blas.ui.item.item_search_result

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.config.FieldType

class ViewAdapter(private val list: List<RowModel>, private val listener: ListListener) : RecyclerView.Adapter<ViewHolders>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolders {
        Log.d("Adapter", "onCreateViewHolder")
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolders(rowView)
    }

    override fun onBindViewHolder(holder:ViewHolders , position: Int) {
        Log.d("Adapter", "onBindViewHolder")
        holder.titleView.text = list[position].title
        holder.detailView.text = list[position].detail
        holder.token = list[position].token
        holder.projectId = list[position].projectId
        holder.itemList = list[position].itemList
        holder.itemView.setOnClickListener {
            listener.onClickRow(it, list[position])
        }

        val regex = Regex(FieldType.ENDTEXT)
        if(regex.containsMatchIn(holder.titleView.text.toString())){
            holder.titleView.setTextColor(Color.RED)
        }else{
            holder.titleView.setTextColor(Color.DKGRAY)
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