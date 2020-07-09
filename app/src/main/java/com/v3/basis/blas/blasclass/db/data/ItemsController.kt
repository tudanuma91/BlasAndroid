package com.v3.basis.blas.blasclass.db.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.linkFixtures.LinkFixture
import com.v3.basis.blas.blasclass.db.data.linkFixtures.LinkRmFixture
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ItemsController(context: Context, projectId: String): BaseController(context, projectId) {

    //TODO 三代川さん
    fun search(item_id: Int = 0): MutableList<MutableMap<String, String?>> {

        val db = openSQLiteDatabase()
        val cursor = if (item_id == 0) {
            db?.rawQuery("select * from items order by create_date desc", null)
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
//        map.set("item_id", (System.currentTimeMillis()/1000L).toString())
//        map.set("end_flg", "0")


        val item = Items()
        setProperty(item, map)

        item.item_id = createTempId()
        item.project_id = projectId.toInt()

        val user = getUserInfo(db)
        if( null != user ) {
            item.user_id = user?.user_id
            item.org_id = user?.org_id
        }
        else {
            item.user_id = 1
            item.org_id = 1
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

        item.create_date = current.format(formatter)
        item.update_date = current.format(formatter)
        item.sync_status = 1

        val cv = createConvertValue(item)

        return try {
            db.beginTransaction()

            // itemテーブルに追加
            db.insert("items",null,cv)
            // fixture(rm_fixture)を更新
           updateFixture(db,item,map)

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

        val user = getUserInfo(db)
        if( null != user ) {
            item.user_id = user?.user_id
            item.org_id = user?.org_id
        }
        else {
            item.user_id = 1
            item.org_id = 1
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")
        item.update_date = current.format(formatter)
        item.sync_status = 2

        // けどまたmap…
        val cv = createConvertValue(item,null)

        return try {
            db.beginTransaction()

            // itmeテーブルを更新
            db.update("items",cv,"item_id = ?", arrayOf(item.item_id.toString()))
            // fixture(rm_fixture)を更新
            updateFixture(db,item,map)

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


    private fun updateFixture(db : SQLiteDatabase?, item :Items, map: Map<String, String?> ) {
        val inst = getFieldCols( db,8 )
        val rms = getFieldCols(db,11)

        // 設置
        inst.forEach{
            // TODO:約束事ではmapからではなくitemから取得する ⇒ うまくいかないのでとりあえずmapから取得
//                val test = item::class.java.getField("fld" + it.toString())
//                Log.d("test",test.toString())

            if( map.containsKey("fld" + it.toString()) ) {
                //updateFixtureExec(db,item,map.get("fld" + it.toString()).toString() ,8)
                LinkFixture(db,item,map.get("fld" + it.toString()).toString()).exec()
            }
        }

        // 撤去
        rms.forEach{
            if( map.containsKey("fld" + it.toString()) ) {
                //updateFixtureExec(db,item,map.get("fld" + it.toString()).toString() ,11)
                LinkRmFixture(db,item,map.get("fld" + it.toString()).toString()).exec()
            }
        }

    }

    /**
     * 設置できるかを確認
     */
    private fun checkInst() {

        // TODO:検品されている機器か？
        // TODO:持出者と同じ設置者か？
        // TODO:持出できない機器ではないか？
        // TODO:持出確認が行われているか？
        // TODO:既に設置されていないか？

    }


    private fun updateFixtureExec(db : SQLiteDatabase?, item:Items, serialNumber:String, type:Int ) {

        if(serialNumber.isEmpty()) {
            Log.d("serial number","空なのでreturn")
            return
        }

        var table:String
//        var cv : ContentValues
        var cv = ContentValues()

        if(type == 11) {
            // 撤去の時
            table = "rm_fixtures"
            /*
            val rmFixture = LDBRmFixtureRecord()
            rmFixture.item_id = item.item_id
            rmFixture.rm_org_id = item.org_id
            rmFixture.rm_user_id = item.user_id
            rmFixture.rm_date = item.update_date
            rmFixture.status = 5    // 現場撤去
            rmFixture.sync_status = 2
            cv = createConvertValue(rmFixture)
             */
            // TODO:とりあえず・・・　data clessでやると設定してないところが全部０とかnullにupdateされる！！！
            cv.put("item_id",item.item_id)
            cv.put("rm_org_id",item.org_id)
            cv.put("rm_user_id",item.user_id)
            cv.put("rm_date",item.update_date)
            cv.put("status",5)  // 現場撤去
            cv.put("sync_status",2)
        }
        else {
            // 設置の時
            table = "fixtures"
            /*
            val fixture = LdbFixtureRecord()
            fixture.item_id = item.item_id!!
            fixture.item_org_id = item.org_id!!
            fixture.item_user_id = item.user_id!!
            fixture.item_date = item.update_date!!
            fixture.sync_status = 2
            cv = createConvertValue(fixture)
             */
            cv.put("item_id",item.item_id)
            cv.put("item_org_id",item.org_id)
            cv.put("item_user_id",item.user_id)
            cv.put("item_date",item.update_date)
            cv.put("status",2)  // 設置済み
            cv.put("sync_status",2)

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
