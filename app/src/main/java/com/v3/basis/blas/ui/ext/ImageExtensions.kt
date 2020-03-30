package com.v3.basis.blas.ui.ext

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.rotateBitmap(angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        this,
        0,
        0,
        this.width,
        this.height,
        matrix,
        true
    )
}

fun Bitmap.rotateRight() : Bitmap {
    return rotateBitmap(90.0f)
}

fun Bitmap.rotateLeft() : Bitmap {
    return rotateBitmap(270.0f)
}
