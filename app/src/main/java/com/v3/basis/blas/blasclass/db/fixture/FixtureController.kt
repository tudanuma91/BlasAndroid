package com.v3.basis.blas.blasclass.db.fixture

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import java.lang.Exception

class FixtureController(context: Context, projectId: String): BaseController(context, projectId) {

    fun joinTest(): List<FixturesAndUsers> {
        val db = openDatabase()
        return db.fixtureDao().selectJoinUsers()
    }

    /**
     * 機器一覧を表示するためのSQL
     */
    private val searchFixtureSql  =
        "select fixtures.*" +
            ",o_fix.name as fix_org_name,u_fix.name as fix_user_name " +
            ",o_takeout.name as takeout_org_name,u_takeout.name as takeout_user_name" +
            ",o_rtn.name as rtn_org_name,u_rtn.name as rtn_user_name" +
            ",o_item.name as item_org_name,u_item.name as item_user_name" +
            " from fixtures " +
            " left join orgs as o_fix on fixtures.fix_org_id = o_fix.org_id" +
            " left join users as u_fix on fixtures.fix_user_id = u_fix.user_id" +
            " left join orgs as o_takeout on fixtures.takeout_org_id = o_takeout.org_id" +
            " left join users as u_takeout on fixtures.takeout_user_id = u_takeout.user_id" +
            " left join orgs as o_rtn on fixtures.rtn_org_id = o_rtn.org_id" +
            " left join users as u_rtn on fixtures.rtn_user_id = u_rtn.user_id " +
            " left join orgs as o_item on fixtures.item_org_id = o_item.org_id" +
            " left join users as u_item on fixtures.item_user_id = u_item.user_id"

    fun search(fixture_id: Int? = null): List<LdbFixtureRecord> {

        val db = openSQLiteDatabase()

        var groupId = getUsersValue(db,"group_id")
        if( 0 == groupId ) {
            groupId = 1
        }
        Log.d("group_id取得！！！",groupId.toString())
        val fixtureDispRange = getGroupsValue(db,groupId,"fixture_disp_range")
        Log.d("fixtureDispRange",fixtureDispRange.toString())

        var sqlAdition = ""
        var plHolder  = arrayOf<String>()

        if( 0 == fixtureDispRange ) {
            // projectの設定に従う
            val showData = getShowData(db)
            if( 1 == showData ) {
                // 自分の会社分しか見れない
                val myOrgId = getUsersValue(db,"org_id")
                sqlAdition = " where fixtures.fix_org_id = ?"
                plHolder += myOrgId.toString()
            }
        }

        val sql = searchFixtureSql + sqlAdition

        val cursor = db?.rawQuery(sql, plHolder)
        val ret = mutableListOf<LdbFixtureRecord>()
        cursor?.also { c_now ->
            var notLast = c_now.moveToFirst()
            while (notLast) {
                val fix = setProperty(LdbFixtureRecord() ,c_now)  as  LdbFixtureRecord
                ret.add(fix)
                notLast = c_now.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    /*
    //  BlasRestFixture.createはアプリからも使ってない。
    fun create(fixtures: Fixtures): Boolean {

        return true
    }

    //  BlasRestFixture.updateはアプリからも使ってない。
    fun update(fixtures: Fixtures): Boolean {

        return false
    }

    //  BlasRestFixture.deleteはアプリからも使ってない。
    fun delete(fixtures: Fixtures): Boolean {
        return false
    }
     */

    //TODO 三代川さん
    fun kenpin(serial_number: String): Boolean {

        val db = openSQLiteDatabase()
        db ?: return false

        return try {
            db.beginTransaction()
            db.execSQL("INSERT into fixtures(serial_number) values (?)", arrayOf(serial_number))
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
    fun takeout(serial_number: String): Boolean {

        val db = openSQLiteDatabase()
        db ?: return false

        return try {
            db.beginTransaction()
            db.execSQL("UPDATE fixtures set status = 1 where serial_number = ?", arrayOf(serial_number))
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
    fun rtn(serial_number: String): Boolean {

        val db = openSQLiteDatabase()
        db ?: return false

        return try {
            db.beginTransaction()
            db.execSQL("UPDATE fixtures set status = 2 where serial_number = ?", arrayOf(serial_number))
            db.setTransactionSuccessful()
            db.endTransaction()
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
    }
}
