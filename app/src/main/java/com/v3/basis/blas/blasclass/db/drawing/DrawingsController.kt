package com.v3.basis.blas.blasclass.db.drawing

import android.content.Context
import com.v3.basis.blas.blasclass.db.BaseController


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
}


