package com.v3.basis.blas.blasclass.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * テーブルの作成を行うクラス
 */

class BlasSQLDataBaseHelper(context: Context?, databaseName:String, factory: SQLiteDatabase.CursorFactory?, version: Int): SQLiteOpenHelper(context, databaseName, factory, version) {

    companion object {
        const val CRT_QUE_TABLE = "create table if not exists QueueTable (queue_id integer primary key, uri text, method text, param_file text, retry_count integer, error_code integer, status integer)"
    }


    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL(CRT_QUE_TABLE);
    }

    /* ひとまず処理なし */
    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}