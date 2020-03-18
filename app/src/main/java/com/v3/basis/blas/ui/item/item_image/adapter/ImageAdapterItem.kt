package com.v3.basis.blas.ui.item.item_image.adapter

import com.bumptech.glide.Glide
import com.v3.basis.blas.R
import com.v3.basis.blas.databinding.CellItemImageBinding
import com.v3.basis.blas.ui.item.item_image.ItemImageCellItem
import com.v3.basis.blas.ui.item.item_image.ItemImageViewModel
import com.xwray.groupie.databinding.BindableItem

class ImageAdapterItem(private val viewModel: ItemImageViewModel, private val item: ItemImageCellItem) : BindableItem<CellItemImageBinding>() {

    override fun getLayout(): Int = R.layout.cell_item_image

    override fun bind(viewBinding: CellItemImageBinding, position: Int) {
        viewBinding.vm = viewModel
        viewBinding.item = item
        viewBinding.image.also {
            Glide.with(it).load(item.image).into(it)
        }
    }
}
