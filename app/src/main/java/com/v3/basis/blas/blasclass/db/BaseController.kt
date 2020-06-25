package com.v3.basis.blas.blasclass.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.room.RoomDatabase
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

abstract class BaseController(private val context: Context, val projectId: String) {

    fun openDatabase(): BlasDatabase {

        val path = DownloadWorker.getSavedPath(projectId)
        return path?.let {
            Room.databaseBuilder(context, BlasDatabase::class.java, path).build()
        } ?: throw FileNotFoundException("プロジェクト${projectId}のDBファイルが見つかりません。")
    }

    fun openSQLiteDatabase(): SQLiteDatabase? {

        val path = DownloadWorker.getSavedPath(projectId)
        val helper = path?.let { SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE) }
        return helper
    }

    fun setProperty( fix : Any,cursor : Cursor) : Any {

        fix::class.memberProperties
            .filter{ it.visibility == KVisibility.PUBLIC }
            //.filter{ it.returnType.isSubtypeOf(String::class.starProjectedType) }
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { prop ->
                val value = cursor.getString( cursor.getColumnIndex(prop.name) )

                if( value.isNullOrEmpty() ) {
                    return@forEach
                }

                if( prop.returnType.isSubtypeOf(String::class.starProjectedType) ) {
                    prop.setter.call(fix,value)
                }
                else {
                    prop.setter.call(fix,value.toInt())
                }
            }


        return fix

    }
}
