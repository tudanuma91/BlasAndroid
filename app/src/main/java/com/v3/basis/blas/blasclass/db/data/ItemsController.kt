package com.v3.basis.blas.blasclass.db.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
        Log.d("insert()","start")

        val db = openSQLiteDatabase()
        db ?: return false

        // todo 一時的に設定
        map.set("item_id", (System.currentTimeMillis()/1000L).toString())
        map.set("end_flg", "0")

/*
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

        item.item_id = createTempId()
        item.create_date = current.format(formatter)
        item.update_date = current.format(formatter)
*/

//        val item = Items()
//        setProperty(item, map)
//        val cv = createConvertValue(item)


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
        Log.d("update()","start")

        val db = openSQLiteDatabase()
        db ?: return false

        // mapで来たのをclass入れる
         val item = Items()
        setProperty(item, map)

        val inspections = getFieldCols( db,8 )
        val rms = getFieldCols(db,11)

        // けどまたmap…
        val cv = createConvertValue(item,null)

        return try {
            db.beginTransaction()
//            db.execSQL("UPDATE items set fld1 = 'test' where item_id = ?", arrayOf(item.item_id))
            db.update("items",cv,"item_id = ?", arrayOf(item.item_id.toString()))

            inspections.forEach{
                // TODO:約束事ではmapからではなくitemから取得する
//                val test = item::class.java.getField("fld" + it.toString())
//                Log.d("test",test.toString())

                if( map.containsKey("fld" + it.toString()) ) {
                    updateFixture(db,item.item_id,map.get("fld" + it.toString()).toString() ,8)
                }
            }

            rms.forEach{
                if( map.containsKey("fld" + it.toString()) ) {
                    updateFixture(db,item.item_id,map.get("fld" + it.toString()).toString() ,11)
                }
            }


            db.setTransactionSuccessful()
            db.endTransaction()

            Log.d("item update","仮登録完了！！")
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
    }


    private fun getFieldCols(db : SQLiteDatabase?, type:Int) : List<Int> {
        val ret = mutableListOf<Int>()

        val sql = "select * from fields where type = ?"
        val cursor = db?.rawQuery(sql, arrayOf(type.toString()))

        cursor?.also {
            var notLast = it.moveToFirst()
            while( notLast ) {
                ret.add( cursor.getInt( cursor.getColumnIndex("col") )  )
                notLast = it.moveToNext()
            }
        }
        cursor?.close()

        return ret as List<Int>
    }


    private fun updateFixture(db : SQLiteDatabase?,item_id:Long?,serialNumber:String,type:Int ) {
        Log.d("item_id",item_id.toString())
        Log.d("serial number",serialNumber)

        if(serialNumber.isEmpty()) {
            Log.d("serial number","空なのでreturn")
            return
        }

        val cv = ContentValues()
        cv.put("item_id",item_id)

        var table = "fixtures"
        if( 11 == type ) {
            table = "rm_fixtures"
        }
        db?.update(table,cv,"serial_number = ?", arrayOf(serialNumber))

        Log.d("updateFixture()",table + "を更新完了")
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
