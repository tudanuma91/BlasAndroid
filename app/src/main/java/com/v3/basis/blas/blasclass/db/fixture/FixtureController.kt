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

        val db = openDatabase()
        return if (fixture_id != null) {
            db.fixtureDao().select(fixture_id)
        } else {
            db.fixtureDao().selectAll()
        }
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

        val db = openDatabase()

        return try {
            db.fixtureDao().insert(Fixtures(serial_number = serial_number))
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
