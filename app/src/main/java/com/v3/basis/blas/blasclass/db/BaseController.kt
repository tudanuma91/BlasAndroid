package com.v3.basis.blas.blasclass.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.room.RoomDatabase
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import java.io.FileNotFoundException
import java.text.SimpleDateFormat

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

}
