package com.v3.basis.blas.ui.item.item_image

import android.graphics.Bitmap

enum class FileExtensions(val compressFormat: Bitmap.CompressFormat, val restImageType: String) {
    JPEG(Bitmap.CompressFormat.JPEG, "0"),
    PNG(Bitmap.CompressFormat.PNG, "1");

    companion object {
        fun matchExtension(ext: String) : FileExtensions {

            return when (ext) {
                "image/jpeg", "jpeg", "jpg" -> { JPEG }
                "image/png", "png" -> { PNG }
                else -> { JPEG }
            }
        }
    }
}
