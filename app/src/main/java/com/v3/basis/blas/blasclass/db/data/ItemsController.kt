package com.v3.basis.blas.blasclass.db.data

import android.content.Context
import com.v3.basis.blas.blasclass.db.BaseController

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

        val item = Items()
        setProperty(item, map)

        return try {
            db.beginTransaction()
            db.execSQL("INSERT into items(item_id,project_id,org_id,user_id,lat,lng,fld1,fld2,fld3,fld4,fld5,fld6,fld7,fld8,fld9,fld10,fld11,fld12,fld13,fld14,fld15,fld16,fld17,fld18,fld19,fld20,fld21,fld22,fld23,fld24,fld25,fld26,fld27,fld28,fld29,fld30,fld31,fld32,fld33,fld34,fld35,fld36,fld37,fld38,fld39,fld40,fld41,fld42,fld43,fld44,fld45,fld46,fld47,fld48,fld49,fld50,fld51,fld52,fld53,fld54,fld55,fld56,fld57,fld58,fld59,fld60,fld61,fld62,fld63,fld64,fld65,fld66,fld67,fld68,fld69,fld70,fld71,fld72,fld73,fld74,fld75,fld76,fld77,fld78,fld79,fld80,fld81,fld82,fld83,fld84,fld85,fld86,fld87,fld88,fld89,fld90,fld91,fld92,fld93,fld94,fld95,fld96,fld97,fld98,fld99,fld100,fld101,fld102,fld103,fld104,fld105,fld106,fld107,fld108,fld109,fld110,fld111,fld112,fld113,fld114,fld115,fld116,fld117,fld118,fld119,fld120,fld121,fld122,fld123,fld124,fld125,fld126,fld127,fld128,fld129,fld130,fld131,fld132,fld133,fld134,fld135,fld136,fld137,fld138,fld139,fld140,fld141,fld142,fld143,fld144,fld145,fld146,fld147,fld148,fld149,fld150,ee_enter,ee_exit,ee_enter_location,ee_exit_location,`temp`,end_flg,work_flg,modified_user,create_date,update_date,sync_status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                Items.getPropertyArray(item))
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
