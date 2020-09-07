package com.v3.basis.blas.ui.item.item_image.model

import android.graphics.Bitmap

data class ItemImageWithLink(
    val error_code: Int, // 0
    val message: String, // None
    val records: List<ItemImageWithLinkRecord>
)

data class ItemImageWithLinkRecord(
    val Image: ItemImageWithLinkImage
)

data class ItemImageWithLinkImage(
    val image: String, // /blas7/item/20200818082146102-440-1945139-1541.jpg
    val small_image: String, // /blas7/cache/230_blas7_app_webroot_item_20200818082146102-440-1945139-1541.jpg
    val project_image_id: String,
    val ext: String,
    val image_id: String,
    val item_id: String = ""
)
