package com.v3.basis.blas.ui.item.item_drawing_search

import android.graphics.Bitmap

/**
 * 図面画像データクラス
 *
 * @param bitmap 図面のビットマップデータ
 * @param spots 図面に設定されている設置箇所リスト
 */
data class DrawingImage(
    val bitmap: Bitmap,
    val spots: List<DrawingSpot>) {
}

/**
 * 設置箇所データクラス
 *
 * @param name ラベル名
 * @param color ラベル色（blue, red, green or yellow）
 * @param x 設置箇所のX座標
 * @param y 設置箇所のY座標
 */
data class DrawingSpot(
    val name: String,
    val color: String,
    val x: Int,
    val y: Int) {
}