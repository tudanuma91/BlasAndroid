package com.v3.basis.blas.blasclass.db

import com.v3.basis.blas.blasclass.app.BlasApp
import net.sqlcipher.database.SQLiteDatabase
import java.lang.Exception

/**
 * シングルトンです。
 * DBのオープンとクローズを管理します。
 * クローズはプロジェクトダウンロード開始時、
 * オープンはプロジェクトダウンロード完了後。
 * 同じDBファイルを二重オープンしないようにすることが目的。
 * 二重オープンすると、トランザクションが効きません。
 */
object BlasLdbHandleManager {
    val handlers = mutableMapOf<String, SQLiteDatabase>()

    fun openDB(dbPath:String):SQLiteDatabase? {
        var dbHandle: SQLiteDatabase? = null

        if(handlers.containsKey(dbPath)) {
            dbHandle = handlers[dbPath]
            if(dbHandle != null) {
                if(!dbHandle.isOpen()) {
                    //ハンドルは登録されているが、何らかの理由で勝手に閉じてしまっているとき
                    try {
                        dbHandle = SQLiteDatabase.openDatabase(
                            dbPath,
                            BlasApp.key,
                            null,
                            SQLiteDatabase.OPEN_READWRITE
                        )
                        if (dbHandle != null) {
                            handlers[dbPath] = dbHandle
                        }
                    }catch(e :Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        else {
            try {
                dbHandle = SQLiteDatabase.openDatabase(
                    dbPath,
                    BlasApp.key,
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                )
                if(dbHandle != null) {
                    handlers[dbPath] = dbHandle
                }
            }
            catch(e:Exception) {
                e.printStackTrace()
            }
        }

        return dbHandle
    }

    fun getDbHandle(dbPath:String):SQLiteDatabase? {
        var dbHandle:SQLiteDatabase? = null
        if(handlers.containsKey(dbPath)) {
            dbHandle = handlers[dbPath]
        }

        return dbHandle
    }

    /* 全てのDBを閉じる */
    fun allClose() {
        handlers.forEach { t, u -> u.close() }
        handlers.clear()
    }

    fun closeDB(dbPath:String) {
        /*リトライ中にクローズしたら、どうなるんだろな… */
        if(handlers.containsKey(dbPath)) {
            var dbHandle = handlers[dbPath]
            //クローズ前にロックはかかっているみたい
            dbHandle?.close()
            handlers.remove(dbPath)
        }
    }
}