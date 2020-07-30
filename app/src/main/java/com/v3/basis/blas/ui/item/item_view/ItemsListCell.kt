package com.v3.basis.blas.ui.item.item_view

import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.ListItemBinding
import com.xwray.groupie.databinding.BindableItem

class ItemsListCell(private val viewModel: ItemsListViewModel, val model: ItemsCellModel) : BindableItem<ListItemBinding>() {

    override fun getLayout(): Int = R.layout.list_item

    override fun bind(viewBinding: ListItemBinding, position: Int) {
        viewBinding.vm = viewModel
        viewBinding.model = model
    }
}
