package com.v3.basis.blas.ui.item.item_image.model

data class ItemImageModel(
    val error_code: Int,
    val message: String,
    val records: List<ItemImageRecord>?
)
