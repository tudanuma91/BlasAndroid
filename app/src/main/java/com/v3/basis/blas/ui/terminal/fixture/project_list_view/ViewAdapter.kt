package com.v3.basis.blas.ui.terminal.fixture.project_list_view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.databinding.ListProjectBinding
import com.v3.basis.blas.ui.terminal.common.DownloadViewModel

class ViewAdapterAdapter(
    private val list: List<RowModel>,
    private val listener: ListListener,
    private val viewModel: DownloadViewModel) : RecyclerView.Adapter<HomeViewHolder>() {

    private lateinit var bind: ListProjectBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        Log.d("Life Cycle", "onCreateViewHolder")
        bind = ListProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        bind.vm = viewModel
        return HomeViewHolder(bind.root, bind)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        Log.d("Life Cycle", "onBindViewHolder")
//        holder.binding.item = list[position].item
        holder.titleView.text = list[position].title
        holder.detailView.text = list[position].detail
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
