package com.v3.basis.blas.blasclass.models

import android.content.ContentValues
import com.v3.basis.blas.blasclass.controller.LDBHelper
import com.v3.basis.blas.blasclass.db.BlasSQLDataBaseHelper
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

abstract class LDBModel(val dbHelper: LDBHelper) {

    /**
     * 引数に指定したBlasRecordクラスをmap形式に変換する
     * [引数]
     * LDBRecordを継承したデータクラス
     * [戻り値]
     * MutableMap<String, Any?>: LDBRecordのメンバ変数をキーに、メンバ変数の値が設定されたmapを返却する
     */
    public fun mapOf(record: LDBRecord):MutableMap<String, Any?> {
        val members = record::class.memberProperties
        var map:MutableMap<String, Any?> = mutableMapOf()
        members.forEach {
            var k = it.name
            var v = it.getter.call(record)
            map[k] = v
        }
        return map
    }

    /**
     * 引数に指定したテーブルを保存する
     * [引数]
     * LDBRecordを継承したデータクラス
     * [戻り値]
     * なし
     * [例外]
     * 保存に失敗した場合、例外を返す
     */
     public fun save(record: LDBRecord) {
        /* 主キーを取得する */
        var map = this.mapOf(record)
        var id = map[record.primary_key]
        var db = dbHelper.writableDatabase
        val values = ContentValues()

        //カラム名と値のペアを作成する
        map.forEach{
            if(it.value is String) {
                values.put(it.key, it.value as String)
            }
            else if(it.value is Int) {
                values.put(it.key, it.value as Int)
            }
            else {
                //ここに型を追加していく
            }
        }
        //insertかupdateを識別する
        if(id == 0) {
            //IDの指定がないので新規追加
            db.insertOrThrow(record.tableName, null, values)
        }
        else {
            val whereClauses = "${record.primary_key} = ?"
            val whereArgs = arrayOf(id as String)
            db.update(record.tableName, values, whereClauses, whereArgs)
        }
    }

    /**
     * レコードを削除する
     * [引数]
     * LDBRecordを継承したデータクラス
     * [戻り値]
     * なし
     * [例外]
     * 保存に失敗した場合、例外を返す
     */
    public fun deleteById(record: LDBRecord) {
        var db = dbHelper.writableDatabase
        val whereclauses ="${record.primary_key} = ?"
        val whereArgs = arrayOf(record.primary_key as String)
        db.delete(record.tableName, whereclauses, whereArgs)
    }

    abstract fun find(conditions:MutableMap<String, String>,
                      order:String="desc",
                      page:Int=0, limit:Int=20):List<LDBRecord>
    /**
     * pageは0から指定する
     * 文字列は部分一致で検索したい。 大なり小なりの記号も使いたい
     * conditions = {"serial_number like  to "'%'シリアルナンバー'%'",
     *               "fixture_id to "fixture_id",
     *               "data >"  to "100"}
     */
    /*
    open public fun find(cls: KClass<*>,
                    conditions:MutableMap<String, String>,
                    order:String="desc",
                    page:Int=0, limit:Int=20) {
        var db = dbHelper.readableDatabase
        var offset = page * limit
        var whereclauses:String = ""
        var whereArgs = emptyArray<String>()
        conditions.forEach{

            var tokens = it.key.split(" ")
            if(tokens.count() == 1) {
                whereclauses += "${it.key} = ? "
            }
            else {
                //serial_number >
                //serial_number >=
                //serial_number likeなどの場合
                whereclauses += "${it.key}  ? "
            }
            whereArgs += it.value
        }
        //ここはテスト必要
        val members = cls.declaredMemberProperties
        var columns = ""
        var columnsNum = members.count()
        members.forEach{
            columns += "${it.name},"
        }
        var len = columns.count()
        columns = columns.substring(0, len-1)
        var tableName = ""
        if(cls.simpleName == "LDBFixtureModel") {
            tableName = "fixtures"
        }

        val sql = "select ${columns} from ${tableName} where ${whereclauses} limit ${limit} offset ${offset} order by ${order}"

        val cur = db.rawQuery(sql, whereArgs)
        if(cur.count > 0) {
            //
            var members = this::class.declaredMemberProperties
            members.forEach {
                val colName = it.name
                val prop = members.find{it.name == colName} as KMutableProperty<*>

            }
            while(!cur.isAfterLast) {
                for(i in 0 until columnsNum) {
                    cur.getString(i)
                }
            }
        }
    }

    */
}