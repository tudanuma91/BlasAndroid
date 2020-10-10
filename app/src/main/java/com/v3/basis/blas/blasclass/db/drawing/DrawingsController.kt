package com.v3.basis.blas.blasclass.db.drawing

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import com.v3.basis.blas.blasclass.component.DrawingImageComponent
import com.v3.basis.blas.blasclass.component.ImageComponent
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Images
import com.v3.basis.blas.blasclass.db.data.Items
import com.v3.basis.blas.ui.item.item_image.model.ItemImage
import java.text.SimpleDateFormat
import java.util.*


class DrawingsController(context: Context, projectId: String): BaseController(context, projectId) {

    fun getCategories(): MutableList<MutableMap<String, String?>> {
        val ret = mutableListOf<MutableMap<String, String?>>()

        val cursor = db?.rawQuery("select * from drawing_categories " +
                "where project_id = ? ", arrayOf(projectId))
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                getMapValues(c)?.let {
                    ret.add(it)
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    fun getSubCategories(categoryId:Int): MutableList<MutableMap<String, String?>> {
        val ret = mutableListOf<MutableMap<String, String?>>()

        val cursor = db?.rawQuery("select * from drawing_sub_categories " +
                "where project_id = ? " +
                "and drawing_category_id = ? ", arrayOf(projectId, categoryId.toString()))
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                getMapValues(c)?.let {
                    ret.add(it)
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    fun getDrawings(categoryId: Int = 0, subCategoryId: Int = 0): MutableList<MutableMap<String, String?>> {
        val ret = mutableListOf<MutableMap<String, String?>>()
        var where = "where project_id = ? "
        var plHolder = arrayOf<String>()
        plHolder += projectId

        if (categoryId == 0) {
            where += "and drawing_category_id IS NULL "
            where += "and drawing_sub_category_id IS NULL "
        } else {
            where += "and drawing_category_id = ? "
            plHolder += categoryId.toString()
            if (subCategoryId == 0) {
                where += "and drawing_sub_category_id IS NULL "
            } else {
                where += "and drawing_sub_category_id = ? "
                plHolder += subCategoryId.toString()
            }
        }

        val cursor = db?.rawQuery("select * from drawings $where" , plHolder)
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                getMapValues(c)?.let {
                    ret.add(it)
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    fun getSpots(drawingId: Int): MutableList<MutableMap<String, String?>> {
        val ret = mutableListOf<MutableMap<String, String?>>()

        val cursor = db?.rawQuery("select * from drawing_spots " +
                "where project_id = ? " +
                "and drawing_id = ? ", arrayOf(projectId, drawingId.toString()))
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                getMapValues(c)?.let {
                    ret.add(it)
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    /**
     * [説明]
     * 画像をローカルに保存する
     * [引数]
     * filename:ファイル名
     * bmp: 画像データ
     */
    fun saveImageToLocal(filename: String, bmp: Bitmap): Boolean {
        bmp.setHasAlpha(true)   // アルファチャンネルを有効にする（黒塗り画像の回避）
        val hash: String = DrawingImageComponent().saveBmp2Local(context, projectId, filename, bmp)
        return hash.isNotEmpty()
    }

    /**
     * [説明]
     * 画像をローカルから取得する
     * [引数]
     * filename:ファイル名
     * bmp: 画像データ
     */
    fun loadImageFromLocal(filename: String): Bitmap? {
        return DrawingImageComponent().readBmpFromLocal(context, projectId, filename)
    }
}


