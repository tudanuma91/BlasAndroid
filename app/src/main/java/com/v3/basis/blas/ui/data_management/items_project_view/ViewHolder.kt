package com.v3.basis.blas.ui.data_management.items_project_view

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.R

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleView: TextView = itemView.findViewById(R.id.row_title)
    val detailView: TextView = itemView.findViewById(R.id.row_detail)
    val textView: TextView = itemView.findViewById(R.id.row_test)
}