package com.v3.basis.blas.ui.item.item_image.model

import android.util.Base64
import androidx.databinding.ObservableField
import com.v3.basis.blas.blasclass.rest.BlasRestImage
import com.v3.basis.blas.ui.ext.translateToBitmap
import com.v3.basis.blas.ui.item.item_image.ItemImageCellItem

data class ItemImage(
    val create_date: String,
    val ext: String,
    val filename: String,
    val hash: String,
    val image: String,
    val image_id: String,
    val item_id: String,
    val moved: String,
    val project_id: String,
    val project_image_id: String
) {
    fun mapToItemImageCellItem() : ItemImageCellItem {
        return ItemImageCellItem(
            id = item_id,
            title = filename,
            image = Base64.decode(image, Base64.DEFAULT).translateToBitmap(),
            empty = ObservableField(false)
        )
    }
}
