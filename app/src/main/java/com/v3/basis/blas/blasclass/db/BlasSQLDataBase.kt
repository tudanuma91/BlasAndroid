package com.v3.basis.blas.blasclass.db

import android.database.Cursor
import com.v3.basis.blas.blasclass.app.BlasApp
import android.content.ContentValues
import java.lang.Exception


/**
 * データベース操作を行うクラス
 */
class BlasSQLDataBase {

    val db = dbHelper.writableDatabase

    companion object {
        //TODO:データベースのバージョン
        val DB_VERSION = 1
        //TODO:データベースの名前
        val DB_NAME = "BLAS_DB"

        val context = BlasApp.applicationContext()
        val dbHelper = BlasSQLDataBaseHelper(context, BlasSQLDataBase.DB_NAME, null, BlasSQLDataBase.DB_VERSION);
        val database = dbHelper.writableDatabase
    }

    open fun close(){
        //ここにclose処理を書く。内容を共通化する。
    }

    fun getRecordUnRead(): Cursor {
        val sql = "SELECT * FROM NoticeTable WHERE read_status = '0'"
        val value = db.rawQuery(sql,null)
        return value
    }

    fun getRecordAlreadyRead(): Cursor {
        val sql = "SELECT * FROM NoticeTable WHERE read_status = '1'"
        val value = db.rawQuery(sql,null)
        return value
    }

    fun upDateStatus(dbId:String): Boolean {
        try {
            val values = ContentValues()
            values.put("read_status", 1)
            db.update("NoticeTable", values, "id = '${dbId}' ", null)
            return true
        }catch (e : Exception){
            return false
        }
    }


}