package com.v3.basis.blas.ui.item.item_drawing_search

import android.graphics.Bitmap

data class DrawingImage(
    val bitmap: Bitmap,
    val spots: List<DrawingSpot>) {
}

data class DrawingSpot(
    val name: String,
    val color: String,
    val x: Int,
    val y: Int) {
}