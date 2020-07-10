package com.v3.basis.blas.blasclass.db.data

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.linkFixtures.LinkFixture
import com.v3.basis.blas.blasclass.db.data.linkFixtures.LinkRmFixture
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ItemsController(context: Context, projectId: String): BaseController(context, projectId) {

    //TODO 三代川さん
    fun search(item_id: Long = 0L ): MutableList<MutableMap<String, String?>> {

        val cursor = if (item_id == 0L) {
            db?.rawQuery("select * from items order by create_date desc", null)
        } else {
            db?.rawQuery("select * from items where item_id = ?", arrayOf(item_id.toString()))
        }
        val ret = mutableListOf<MutableMap<String, String?>>()
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                val value = getMapValues(cursor)
                if( null != value ) {
                    ret.add(getMapValues(cursor))
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    //TODO 三代川さん
    fun create(map: MutableMap<String, String?>): Boolean {
        Log.d("insert()","start")

        // todo 一時的に設定
//        map.set("item_id", (System.currentTimeMillis()/1000L).toString())
//        map.set("end_flg", "0")


        val item = Items()
        setProperty(item, map)

        item.item_id = createTempId()
        item.project_id = projectId.toInt()

        val user = getUserInfo()
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
            db?.beginTransaction()

            // itemテーブルに追加
            db?.insert("items",null,cv)
            // fixture(rm_fixture)を更新
           updateFixture(item,map)

            db?.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
        finally {
            db?.endTransaction()
        }
    }

    //TODO 三代川さん
    fun update(map: Map<String, String?>): Boolean {
        Log.d("update()","start")

        // mapで来たのをclass入れる
         val item = Items()
        setProperty(item, map)

        val user = getUserInfo()
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
            db?.beginTransaction()

            // itmeテーブルを更新
            db?.update("items",cv,"item_id = ?", arrayOf(item.item_id.toString()))
            // fixture(rm_fixture)を更新
            updateFixture(item,map)

            db?.setTransactionSuccessful()

            Log.d("item update","仮登録完了！！")
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
        finally {
            db?.endTransaction()
            Log.d("item update","終了！")
        }
    }


    private fun getFieldCols(type:Int) : List<Int> {
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


    /**
     * fixtureテーブルに仮登録
     */
    private fun updateFixture(item :Items, map: Map<String, String?> ) {
        val inst = getFieldCols( 8 )
        val rms = getFieldCols(11)

        // 設置
        inst.forEach{
            // TODO:約束事ではmapからではなくitemから取得する ⇒ うまくいかないのでとりあえずmapから取得
//                val test = item::class.java.getField("fld" + it.toString())
//                Log.d("test",test.toString())

            if( map.containsKey("fld" + it.toString()) ) {
                LinkFixture(db,item,map.get("fld" + it.toString()).toString()).exec()
            }
        }

        // 撤去
        rms.forEach{
            if( map.containsKey("fld" + it.toString()) ) {
                LinkRmFixture(db,item,map.get("fld" + it.toString()).toString()).exec()
            }
        }

    }

    fun updateItemId4Insert(org_item_id:String, new_item_id:String ) {
        Log.d("updateItemId()","start")

        val cv = ContentValues()
        cv.put("item_id",new_item_id)
        cv.put("sync_status",0)

        return try {
            db?.beginTransaction()

            db?.update("items",cv,"item_id = ?", arrayOf(org_item_id))
            db?.update("fixtures",cv,"item_id = ?", arrayOf(org_item_id))
            db?.update("rm_fixtures",cv,"item_id = ?", arrayOf(org_item_id))

            db?.setTransactionSuccessful()!!
        }
        catch (e:Exception) {
            e.printStackTrace()
            throw e
        }
        finally {
            db?.endTransaction()
        }

    }

    fun updateItemId4Update( item_id:String,item : Items,mapItem : MutableMap<String, String?> ) {
        Log.d("updateItemId()","start")

        val cv = ContentValues()
        cv.put("sync_status",0)

        return try {
            db?.beginTransaction()

            db?.update("items",cv,"item_id = ?", arrayOf(item_id))
            // fixture,rm_fixtureを更新
            updateFixture(item,mapItem)

            db?.setTransactionSuccessful()!!
        }
        catch (e:Exception) {
            e.printStackTrace()
            throw e
        }
        finally {
            db?.endTransaction()
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
