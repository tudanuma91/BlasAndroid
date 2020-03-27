package com.v3.basis.blas.ui.item.item_image.model

import android.graphics.Bitmap
import android.util.Base64
import com.v3.basis.blas.ui.ext.translateToBitmap
import java.util.*

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
    var bitmap: Bitmap? = null
}
