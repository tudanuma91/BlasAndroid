package com.v3.basis.blas.ui.item.item_view

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

        val model = list[position]
        holder.titleView.text = model.title
        holder.detailView.text = model.detail
        holder.token = model.token
        holder.projectId = model.projectId
        holder.itemList = model.itemList
        holder.itemView.setOnClickListener { listener.onClickRow(it, model) }
        holder.image.setOnClickListener { listener.onClickImage(model.itemId) }

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
        fun onClickImage(itemId: String?)
    }
}
