package com.v3.basis.blas.blasclass.db.fixture

import android.content.Context
import com.v3.basis.blas.blasclass.db.BaseController
import java.lang.Exception

class FixtureController(context: Context, projectId: String): BaseController(context, projectId) {

    fun joinTest(): List<FixturesAndUsers> {
        val db = openDatabase()
        return db.fixtureDao().selectJoinUsers()
    }

    fun search(fixture_id: Int? = null): List<Fixtures> {

        val db = openSQLiteDatabase()
        val cursor = db?.rawQuery("select * from fixtures", null)
        val ret = mutableListOf<Fixtures>()
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                val r = c.getColumnIndex("serial_number").let {
                    val serialNumber = cursor.getString(it)
                    Fixtures(serial_number = serialNumber)
                }
                ret.add(r)
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret

//        val db = openDatabase()
//        return if (fixture_id != null) {
//            db.fixtureDao().select(fixture_id)
//        } else {
//            db.fixtureDao().selectAll()
//        }
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

    fun takeout(serial_number: String): Boolean {

        val db = openDatabase()

        return try {
            db.fixtureDao().update(Fixtures(serial_number = serial_number))
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
    }

    fun rtn(serial_number: String): Boolean {

        val db = openDatabase()

        return try {
            db.fixtureDao().update(Fixtures(serial_number = serial_number))
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
    }
}
