package com.v3.basis.blas.blasclass.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Room
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.ldb.LdbUserRecord
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import io.reactivex.subjects.PublishSubject
import java.io.FileNotFoundException
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

/**
 * [説明]
 * SQLiteのDBを管理するクラス。
 */
abstract class BaseController(
    private val context: Context
    ,val projectId: String
) {

    companion object {
        const val SYNC_STATUS_SYNC = 0      // 同期済み
        const val SYNC_STATUS_NEW = 1       // 仮新規追加
        const val SYNC_STATUS_EDIT = 2      // 仮編集
        const val SYNC_STATUS_DEL = 3       // 仮削除
        const val SYNC_STATUS_SEND_WAIT = 4       // 送信待ち
        const val SYNC_STATUS_SEND_FIN = 5       // 送信完了

        const val KENPIN_FIN = 0
        const val TAKING_OUT = 1
        const val SET_FIN = 2
        const val DONT_TAKE_OUT = 3
        const val RTN = 4
        const val REMOVE = 5

    }


        // 使うときはerrorMessageEvent.onNext("メッセージ")とする
    val errorMessageEvent: PublishSubject<String> = PublishSubject.create()

    var db:SQLiteDatabase?

    init {
        db = openSQLiteDatabase()
    }


    fun openDatabase(): BlasDatabase {

        val path = DownloadWorker.getSavedPath(projectId)
        return path?.let {
            Room.databaseBuilder(context, BlasDatabase::class.java, path).build()
        } ?: throw FileNotFoundException("プロジェクト${projectId}のDBファイルが見つかりません。")
    }

    fun openSQLiteDatabase(): SQLiteDatabase? {

        val path = DownloadWorker.getSavedPath(projectId)
        val helper = path?.let { SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE) }

        Log.d("SqliteDB Path:",path.toString())
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
                Log.d("setProperty()","propName:" + prop.name )
                val value = cursor.getString( cursor.getColumnIndex(prop.name) )
                Log.d("setProperty()","value:" + value)

                if( value.isNullOrEmpty() ) {
                    return@forEach
                }

                setPropExec(instance,prop,value)
            }

        return instance
    }


    // [参考]https://www.javadrive.jp/android/sqlite_data/index6.html
    protected fun createConvertValue(  instance : Any ,exceptList : List<String>? = null) : ContentValues {
        Log.d("createConvertValue()","start")

        val cv = ContentValues()
        instance::class.memberProperties.forEach {

            Log.d(it.name,it.getter.call(instance).toString())

            if( null != exceptList ) {
                if( exceptList.contains(it.name) ) {
                    return@forEach
                }
            }

            if( null != it.getter.call(instance) ) {
                cv.put(it.name,it.getter.call(instance).toString())
            }
        }
        return cv
    }

    /**
     * ClassのプロパティにMapの値を格納する
     * @param in instance データを格納するクラスのインスタンス
     * @param in map MutableMap
     */
    fun setProperty(instance : Any, map : Map<String, String?>) : Any {

        instance::class.memberProperties
            .filter{ it.visibility == KVisibility.PUBLIC }
            //.filter{ it.returnType.isSubtypeOf(String::class.starProjectedType) }
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { prop ->

                if( !map.containsKey(prop.name) ) {
                    return@forEach
                }
                val value = map[prop.name]

                if( value.isNullOrEmpty() ) {
                    return@forEach
                }

                setPropExec(instance,prop,value)
            }

        return instance
    }


    private fun setPropExec(instance : Any, prop: KMutableProperty<*>, value:String) {

        if( prop.returnType.isSubtypeOf(String::class.starProjectedType)
            or prop.returnType.isSupertypeOf(String::class.starProjectedType)
        ) {
            prop.setter.call(instance,value)
        } else if (prop.returnType.isSubtypeOf(Float::class.starProjectedType)
            or prop.returnType.isSupertypeOf(Float::class.starProjectedType)
        ) {
            prop.setter.call(instance,value.toFloat())
        } else if (prop.returnType.isSubtypeOf(Long::class.starProjectedType)
            or prop.returnType.isSupertypeOf(Long::class.starProjectedType)
        ){
            prop.setter.call(instance,value.toLong())
        } else if (prop.returnType.isSubtypeOf(Int::class.starProjectedType)
            or prop.returnType.isSupertypeOf(Int::class.starProjectedType)
        ){
            prop.setter.call(instance,value.toInt())
        }

    }

    /**
     * ClassのプロパティにMapの値を格納する
     * @param in instance データを格納するクラスのインスタンス
     * @param in cursor DBのカーソル
     */
    protected fun getMapValues(cursor: Cursor) : MutableMap<String, String?> {

        val map = mutableMapOf<String, String?>()
        cursor.columnNames.forEach { name ->
            val value = cursor.getString( cursor.getColumnIndex(name) )
            map[name] = value ?: ""
        }
        return map
    }

    /**
     * ユーザーテーブルから指定したカラムの値を取得する
     * user_idはログインユーザーに固定される
     */
    protected fun getUserInfo() : LdbUserRecord? {

        val sql = "select * from users where user_id = ?"
        val cursor = db?.rawQuery(sql, arrayOf( BlasApp.userId.toString() ))

        if( 0 == cursor?.count ) {
            return null
        }

        var user : LdbUserRecord? = null
        var value : Int = 0
        cursor?.also {
            it.moveToFirst()
            user = setProperty( LdbUserRecord(),it ) as LdbUserRecord
        }
        cursor?.close()

        return user
    }

    /**
     * グループテーブルから指定したグループIDの指定カラムの値を取得する
     */
    protected  fun getGroupsValue(groupId:Int?, columnName:String) : Int {

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
    protected fun getProjectVlue(columnName:String ) : Int {

        val sql = "select "+ columnName + " from projects"
        val cursor = db?.rawQuery(sql,null)

        var value = 0
        cursor?.also {
            it.moveToFirst()
            value = it.getInt( it.getColumnIndex(columnName) )
        }
        cursor?.close()

        return value
    }

    protected fun createTempId() : Long {
        val unixTime = System.currentTimeMillis()
        Log.d("UnixTime",unixTime.toString())

//        val unxi9 =  unixTime.toString().substring( unixTime.toString().length - 9,  unixTime.toString().length)
//        Log.d("後ろ9桁",unxi9)

        return unixTime.toLong() * (-1)
    }

}
