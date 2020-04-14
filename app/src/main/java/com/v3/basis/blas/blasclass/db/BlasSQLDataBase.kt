package com.v3.basis.blas.blasclass.db

import android.database.Cursor
import com.v3.basis.blas.blasclass.app.BlasApp

/**
 * データベース操作を行うクラス
 */
class BlasSQLDataBase {


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
        val db = dbHelper.writableDatabase
        val sql = "SELECT * FROM NoticeTable WHERE read_status = '0'"
        val value = db.rawQuery(sql,null)
        return value
    }

    fun getRecordAlreadyRead(): Cursor {
        val db = dbHelper.writableDatabase
        val sql = "SELECT * FROM NoticeTable WHERE read_status = '1'"
        val value = db.rawQuery(sql,null)
        return value
    }


}