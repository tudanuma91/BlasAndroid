package com.v3.basis.blas.ui.item.item_drawing_search

/**
 * 図面データクラス
 *
 * @param id 図面ID
 * @param name 図面名
 * @param drawingFile ファイル名
 * @param resId 未使用
 */
data class Drawing(
    val id: Int,
    val name: String,
    val drawingFile: String,
    val resId: Int) {
}