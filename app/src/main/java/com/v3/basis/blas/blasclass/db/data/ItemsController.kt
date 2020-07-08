package com.v3.basis.blas.blasclass.db.data

import android.content.Context
import com.v3.basis.blas.blasclass.db.BaseController
import kotlin.random.Random

class ItemsController(context: Context, projectId: String): BaseController(context, projectId) {

    //TODO 三代川さん
    fun search(item_id: Int = 0): MutableList<MutableMap<String, String?>> {

        val db = openSQLiteDatabase()
        val cursor = if (item_id == 0) {
            db?.rawQuery("select * from items", null)
        } else {
            db?.rawQuery("select * from items where item_id = ?", arrayOf(item_id.toString()))
        }
        val ret = mutableListOf<MutableMap<String, String?>>()
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                ret.add(getMapValues(cursor))
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    //TODO 三代川さん
    fun create(map: MutableMap<String, String?>): Boolean {

        val db = openSQLiteDatabase()
        db ?: return false

        // todo 一時的に設定
        map.set("item_id", (System.currentTimeMillis()/1000L).toString())
        map.set("end_flg", "0")
//        val item = Items()
//        setProperty(item, map)
        val columns = map.keys.joinToString(separator = ",")
        val values = map.values.map { "?" }.joinToString(",")
        val sql = "INSERT into items($columns) values ($values)"
        map.values.map { if (it?.isBlank() == true) { null } else { it } }
        val arr = map.values.toTypedArray()

        return try {
            db.beginTransaction()
            db.execSQL(sql, arr)
            db.setTransactionSuccessful()
            db.endTransaction()
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }
        }
    }

    //TODO 三代川さん
    fun update(map: Map<String, String?>): Boolean {

        val db = openSQLiteDatabase()
        db ?: return false

        val item = Items()
        setProperty(item, map)

        return try {
            db.beginTransaction()
            db.execSQL("UPDATE items set fld1 = 'test' where item_id = ?", arrayOf(item.item_id))
            db.setTransactionSuccessful()
            db.endTransaction()
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
    }

    //TODO 三代川さん
    //アプリからDELETEは実行されていない。
//    fun delete(item: Items): Boolean {
//        val db = openSQLiteDatabase()
//        db ?: return false
//
//        return try {
//            db.beginTransaction()
//            db.execSQL("DELETE from items where item_id = ?", arrayOf(item.item_id))
//            db.setTransactionSuccessful()
//            db.endTransaction()
//            true
//        } catch (e: Exception) {
//            //とりあえず例外をキャッチして、Falseを返す？
//            e.printStackTrace()
//            false
//        }
//    }
}
