package com.v3.basis.blas.ui.terminal.status.UnRead

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R

class UnReadViewAdapter(private val list: MutableList<UnReadRowModel>, private val listener: ListListener) : RecyclerView.Adapter<UnReadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnReadViewHolder {
        Log.d("Life Cycle", "onCreateViewHolder")
        val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.list_status_unread, parent, false)
        return UnReadViewHolder(rowView)
    }

    override fun onBindViewHolder(holder: UnReadViewHolder, position: Int) {
        Log.d("Life Cycle", "onBindViewHolder")

        val model = list[position]
        holder.titleView.text = list[position].title
        holder.detailView.text = list[position].detail
        holder.statusId = list[position].statusId
        holder.token = list[position].token
        holder.changeAlready.setOnClickListener{listener.onClickChangeAlready(model.statusId)}

        //ここにタップ時に受け渡す値を入力する？
        holder.itemView.setOnClickListener {
            listener.onClickRow(it, list[position])
        }
    }

    override fun getItemCount(): Int {
        Log.d("Life Cycle", "getItemCount")
        return list.size
    }

    interface ListListener{
        fun onClickRow(tappedView: View, rowModel:UnReadRowModel)
        fun onClickChangeAlready(id:String)
    }

}
