package com.v3.basis.blas.blasclass.controller
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

/**
 * ローカルのSQLiteDBにアクセスするためのクラス
 * [引数]
 * context: アプリケーションコンテキスト
 * databaseName:DBのファイル名
 * factory: 使用しない
 * version: 1以上の整数値。バージョン管理に使用する
 */
class LDBHelper(context: Context, databaseName:String, factory: SQLiteDatabase.CursorFactory?=null, version: Int=1) : SQLiteOpenHelper(context, databaseName, factory, version){
    override fun onCreate(db: SQLiteDatabase?) {
        TODO("状態管理テーブルを作成する")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}