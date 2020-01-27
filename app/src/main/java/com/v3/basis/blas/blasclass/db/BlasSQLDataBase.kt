package com.v3.basis.blas.blasclass.db

/**
 * データベース操作を行うクラス
 */
abstract class BlasSQLDataBase {
    companion object {
        //TODO:データベースのバージョン
        val DB_version = 11.2
        //TODO:データベースの名前
        val DB_name = "Database_Name"
    }

    open fun close(){
        //ここにclose処理を書く。内容を共通化する。
    }
}