package com.v3.basis.blas.blasclass.db

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
}