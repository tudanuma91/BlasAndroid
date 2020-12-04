package com.v3.basis.blas.blasclass.db.drawing

import android.content.Context
import android.graphics.Bitmap
import com.v3.basis.blas.blasclass.component.DrawingImageComponent
import com.v3.basis.blas.blasclass.db.BaseController

/**
 * 図面検索のデータベースコントローラークラス。
 * 図面検索に関わるデータベーステーブルに対する処理を扱う。また、図面の画像データの保存と取得も行う。
 * @param context コンテキスト
 * @param projectId プロジェクトID
 */
class DrawingsController(context: Context, projectId: String): BaseController(context, projectId) {

    /**
     * 図面カテゴリー一覧をデータベースから取得する。
     * @return 図面カテゴリー一覧
     */
    fun getCategories(): MutableList<MutableMap<String, String?>> {
        val ret = mutableListOf<MutableMap<String, String?>>()

        val cursor = db?.rawQuery("select * from drawing_categories " +
                "where project_id = ? ", arrayOf(projectId))
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                getMapValues(c).let {
                    ret.add(it)
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    /**
     * 図面サブカテゴリー一覧をデータベースから取得する。
     * @param categoryId 図面カテゴリーID
     * @return 図面サブカテゴリー一覧
     */
    fun getSubCategories(categoryId:Int): MutableList<MutableMap<String, String?>> {
        val ret = mutableListOf<MutableMap<String, String?>>()

        val cursor = db?.rawQuery("select * from drawing_sub_categories " +
                "where project_id = ? " +
                "and drawing_category_id = ? ", arrayOf(projectId, categoryId.toString()))
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                getMapValues(c).let {
                    ret.add(it)
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    /**
     * 図面一覧をデータベースから取得する。
     * @param categoryId 図面カテゴリーID（デフォルトは0:図面カテゴリー無し）
     * @param subCategoryId 図面サブカテゴリーID（デフォルトは0:図面サブカテゴリー無し）
     * @return 図面一覧
     */
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
                getMapValues(c).let {
                    ret.add(it)
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    /**
     * 設置場所一覧をデータベースから取得する。
     * @param drawingId 図面ID
     * @return 設置場所一覧
     */
    fun getSpots(drawingId: Int): MutableList<MutableMap<String, String?>> {
        val ret = mutableListOf<MutableMap<String, String?>>()

        val cursor = db?.rawQuery("select * from drawing_spots " +
                "where project_id = ? " +
                "and drawing_id = ? ", arrayOf(projectId, drawingId.toString()))
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                getMapValues(c).let {
                    ret.add(it)
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    /**
     * 図面の画像データをローカルストレージに保存する。
     * @param filename ファイル名
     * @param bmp 画像データ
     */
    fun saveImageToLocal(filename: String, bmp: Bitmap): Boolean {
        bmp.setHasAlpha(true)   // アルファチャンネルを有効にする（黒塗り画像の回避）
        val hash: String = DrawingImageComponent().saveBmp2Local(context, projectId, filename, bmp)
        return hash.isNotEmpty()
    }

    /**
     * 図面の画像データをローカルストレージから取得する。
     * @param filename ファイル名
     * @return 画像データ
     */
    fun loadImageFromLocal(filename: String): Bitmap? {
        return DrawingImageComponent().readBmpFromLocal(context, projectId, filename)
    }
}


