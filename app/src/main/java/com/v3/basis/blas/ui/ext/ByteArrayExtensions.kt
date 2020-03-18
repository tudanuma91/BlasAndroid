package com.v3.basis.blas.ui.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory


fun ByteArray.translateToBitmap() : Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}
