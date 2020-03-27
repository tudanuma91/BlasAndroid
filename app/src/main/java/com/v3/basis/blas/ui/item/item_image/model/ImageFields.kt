package com.v3.basis.blas.ui.item.item_image.model

import androidx.databinding.ObservableField
import com.v3.basis.blas.ui.item.item_image.ItemImageCellItem

data class ImageFields(
    val create_date: String,
    val field_id: String,
    val list: String,
    val name: String,
    val project_id: String,
    val project_image_id: String,
    val rank: String,
    val update_date: String
) {
    fun mapToItemImageCellItem() : ItemImageCellItem {
        return ItemImageCellItem(
            id = project_image_id,
            title = name,
            image = ObservableField(),
            empty = ObservableField(true),
            loading = ObservableField(false)
        )
    }
}
