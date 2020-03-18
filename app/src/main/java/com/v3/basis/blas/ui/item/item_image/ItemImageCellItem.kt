package com.v3.basis.blas.ui.item.item_image

import android.graphics.Bitmap
import androidx.databinding.ObservableField

data class ItemImageCellItem(
    val id: String,
    val title: String,
    var image: Bitmap,
    var empty: ObservableField<Boolean>
)
