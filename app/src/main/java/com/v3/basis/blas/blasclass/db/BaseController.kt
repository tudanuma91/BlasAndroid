package com.v3.basis.blas.blasclass.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import io.reactivex.subjects.PublishSubject
import java.io.FileNotFoundException
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

abstract class BaseController(private val context: Context, val projectId: String) {

    // 使うときはerrorMessageEvent.onNext("メッセージ")とする
    val errorMessageEvent: PublishSubject<String> = PublishSubject.create()

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

    /**
     * ClassのプロパティにDBの値を格納する
     * @param in instance データを格納するクラスのインスタンス
     * @param in cursor DBのカーソル
     */
    protected fun setProperty(instance : Any, cursor : Cursor) : Any {

        instance::class.memberProperties
            .filter{ it.visibility == KVisibility.PUBLIC }
            //.filter{ it.returnType.isSubtypeOf(String::class.starProjectedType) }
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { prop ->
                val value = cursor.getString( cursor.getColumnIndex(prop.name) )

                if( value.isNullOrEmpty() ) {
                    return@forEach
                }

                if( prop.returnType.isSubtypeOf(String::class.starProjectedType) ) {
                    prop.setter.call(instance,value)
                }
                else {
                    prop.setter.call(instance,value.toInt())
                }
            }

        return instance
    }

    /**
     * ユーザーテーブルから指定したカラムの値を取得する
     * user_idはログインユーザーに固定される
     */
    protected fun getUsersValue(db : SQLiteDatabase? ,columnName:String) : Int {

        val sql = "select $columnName from users where user_id = ?"
        val cursor = db?.rawQuery(sql, arrayOf( BlasApp.userId.toString() ))

        if( 0 == cursor?.count ) {
            return 0
        }

        var value : Int = 0
        cursor?.also {
            it.moveToFirst()
            value = it.getInt(  it.getColumnIndex(columnName) )
        }
        cursor?.close()

        return value
    }

    /**
     * グループテーブルから指定したグループIDの指定カラムの値を取得する
     */
    protected  fun getGroupsValue(db:SQLiteDatabase?, groupId:Int, columnName:String) : Int {

        val sql = "select $columnName from groups where group_id = ?"
        val cursor = db?.rawQuery(sql, arrayOf(groupId.toString()))

        var value : Int = 0
        cursor?.also {
            it.moveToFirst()
            value = it.getInt( it.getColumnIndex(columnName) )
        }
        cursor?.close()

        return value
    }

    /**
     * show_dataの値を取得する
     */
    protected fun getShowData( db:SQLiteDatabase? ) : Int {

        val sql = "select show_data from projects"
        val cursor = db?.rawQuery(sql,null)

        var value = 0
        cursor?.also {
            it.moveToFirst()
            value = it.getInt( it.getColumnIndex("show_data") )
        }
        cursor?.close()

        return value
    }

}
