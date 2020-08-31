package com.v3.basis.blas.blasclass.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//import net.sqlcipher.database.SQLiteDatabase
//import net.sqlcipher.database.SQLiteOpenHelper

/**
 * テーブルの作成を行うクラス
 *
 */
class BlasSQLDataBaseHelper(context: Context?, databaseName:String, factory: SQLiteDatabase.CursorFactory?, version: Int): SQLiteOpenHelper(context, databaseName, factory, version) {

    companion object {
        const val CRT_QUE_TABLE = "create table if not exists RequestTable (queue_id integer primary key autoincrement,uri text, method text, param_file text, retry_count integer, error_code integer, status integer)"
        const val CRT_NOTICE_TABLE = "create table if not exists NoticeTable (id integer primary key autoincrement,apl_code integer, read_status integer, func_name text, operation text, project_id integer,data_key text, update_date text)"
    }

    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL(CRT_QUE_TABLE);
        database?.execSQL(CRT_NOTICE_TABLE);
    }

    /* ひとまず処理なし */
    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}