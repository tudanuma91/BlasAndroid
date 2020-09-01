package com.v3.basis.blas.blasclass.db.data.linkFixtures

import android.content.ContentValues
//import android.database.sqlite.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabase

import android.util.Log
import com.v3.basis.blas.blasclass.db.data.Items

abstract  class LinkBase(val db : SQLiteDatabase?,val item: Items,val serialNumber:String ) {

    abstract open val table:String


    abstract fun check() : Boolean

    abstract fun createCV() : ContentValues

    fun exec() {

        if(serialNumber.isEmpty()) {
            Log.d("serial number","空なのでreturn")
            return
        }

        if( !check() ) {
            // TODO:とりあえず例外をthrow
            throw Exception()
        }

        var cv = createCV()
        db?.update(table,cv,"serial_number = ?", arrayOf(serialNumber))

        Log.d("updateFixture()",table + "を更新完了")
    }
}