package com.v3.basis.blas.blasclass.db

import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.blasclass.log.BlasLog
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
        BlasLog.trace("I", "openDB ${dbPath}")
        if(handlers.containsKey(dbPath)) {
            dbHandle = handlers[dbPath]
            if(dbHandle != null) {
                if(!dbHandle.isOpen()) {
                    //ハンドルは登録されているが、何らかの理由で勝手に閉じてしまっているとき
                    try {
                        SQLiteDatabase.loadLibs(context)
                        dbHandle = SQLiteDatabase.openDatabase(
                            dbPath,
                            BlasApp.key,
                            null,
                            SQLiteDatabase.OPEN_READWRITE
                        )
                        if (dbHandle != null) {
                            dbHandle.rawQuery("PRAGMA foreign_keys=1", null)
                            handlers[dbPath] = dbHandle
                            BlasLog.trace("E", "${dbPath}のオープンに失敗しました")
                        }
                    }catch(e :Exception) {
                        BlasLog.trace("E", "${dbPath}のオープンに失敗しました", e)
                    }
                }
            }
            else {
                BlasLog.trace("E", "${dbPath}のオープンに失敗しました")
            }
        }
        else {
            try {
                BlasLog.trace("I", "${dbPath}を開きます")
                SQLiteDatabase.loadLibs(context)
                dbHandle = SQLiteDatabase.openDatabase(
                    dbPath,
                    BlasApp.key,
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                )

                if(dbHandle != null) {
                    dbHandle.rawQuery("PRAGMA foreign_keys=1", null)
                    handlers[dbPath] = dbHandle
                }
            }
            catch(e:Exception) {
                BlasLog.trace("E", "例外が発生しました", e)
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
            Log.d("send", "cloeDB1 ${dbPath}")
            var dbHandle = handlers[dbPath]
            //クローズ前にロックはかかっているみたい
            dbHandle?.close()
            handlers.remove(dbPath)
        }
        else {
            Log.d("send", "cloeDB2 ${dbPath}")
        }
    }
}