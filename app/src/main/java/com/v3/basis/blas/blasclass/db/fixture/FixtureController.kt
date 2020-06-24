package com.v3.basis.blas.blasclass.db.fixture

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.ldb.LdbFixtureTest
import java.lang.Exception
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

class FixtureController(context: Context, projectId: String): BaseController(context, projectId) {

    fun joinTest(): List<FixturesAndUsers> {
        val db = openDatabase()
        return db.fixtureDao().selectJoinUsers()
    }

    //TODO 三代川さん
    fun search(fixture_id: Int? = null): List<LdbFixtureTest> {

        val db = openSQLiteDatabase()
        val cursor = db?.rawQuery("select * from fixtures", null)
        val ret = mutableListOf<LdbFixtureTest>()
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {

                val fix = LdbFixtureTest()

                fix::class.memberProperties
                    .filter{ it.visibility == KVisibility.PUBLIC }
//                    .filter{ it.returnType.isSubtypeOf(String::class.starProjectedType) }
                    .filterIsInstance<KMutableProperty<*>>()
                    .forEach { prop ->
                        Log.d("aaaa","name:" + prop.name)
                        val value = cursor.getString( c.getColumnIndex(prop.name) )
                        Log.d("bbbb","value:" + value)
                        Log.d("cccc","prop field:" + prop.javaField)

                        if( value.isNullOrEmpty() ) {
                            return@forEach
                        }

                        if( prop.returnType.isSubtypeOf(String::class.starProjectedType) ) {
                            prop.setter.call(fix,value)
                        }
                        else {
                            prop.setter.call(fix,value.toInt())
                        }

                   }

                ret.add(fix)
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
