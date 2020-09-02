package com.v3.basis.blas.ui.item.item_image

import android.graphics.Bitmap
import androidx.databinding.ObservableField

data class ItemImageCellItem(
    val id: String, //project_image_idのこと
    val title: String,
    var image: ObservableField<Bitmap>,
    var url: ObservableField<String>,
    var urlBig: ObservableField<String>,
    var empty: ObservableField<Boolean>,
    var loading: ObservableField<Boolean>
) {
    lateinit var imageId: String
    lateinit var ext: String
}
