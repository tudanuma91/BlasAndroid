package com.v3.basis.blas.ui.terminal.adapter

import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.databinding.ListProjectBinding
import com.v3.basis.blas.ui.terminal.common.DownloadItem
import com.v3.basis.blas.ui.terminal.common.DownloadViewModel
import com.v3.basis.blas.ui.terminal.fixture.project_list_view.RowModel
import com.v3.basis.blas.ui.terminal.fixture.project_list_view.ViewAdapterAdapter
import com.xwray.groupie.databinding.BindableItem

class ProjectListCellItem(
    val title: String,
    val detail: String,
    private val viewModel: DownloadViewModel,
    val item: DownloadItem,
    var listener: ViewAdapterAdapter.ListListener? = null) : BindableItem<ListProjectBinding>() {

    override fun getLayout(): Int = R.layout.list_project

    override fun bind(viewBinding: ListProjectBinding, position: Int) {
        viewBinding.vm = viewModel
        viewBinding.item = item
        viewBinding.rowTitle.text = title
        viewBinding.rowDetail.text = detail
        viewBinding.root.setOnClickListener {
            listener?.onClickRow(it, RowModel().apply {
                this.detail = this@ProjectListCellItem.detail
                this.title = this@ProjectListCellItem.title
            })
        }
    }
}
