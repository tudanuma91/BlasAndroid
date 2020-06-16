package com.v3.basis.blas.blasclass.models

import com.v3.basis.blas.blasclass.controller.LDBHelper
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaSetter


class LDBFixtureModel(dbHelper: LDBHelper):LDBModel(dbHelper) {
    //ローカルDBのFixutureに関する処理を書く

    override public fun find(conditions:MutableMap<String, String>,
                             order:String,
                             page:Int,
                             limit:Int):MutableList<LDBFixtureRecord>{

        var db = dbHelper.readableDatabase
        var offset = page * limit
        var whereclauses:String = ""
        var whereArgs = emptyArray<String>()
        var rtnList:MutableList<LDBFixtureRecord> = mutableListOf()

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
        val members = LDBFixtureRecord::class.declaredMemberProperties
        var columns = ""
        var columnsNum = members.count()
        members.forEach{
            columns += "${it.name},"
        }
        var len = columns.count()
        columns = columns.substring(0, len-1)

        val sql = "select ${columns} from fixtures where ${whereclauses} limit ${limit} offset ${offset} order by ${order}"

        val cur = db.rawQuery(sql, whereArgs)
        if(cur.count > 0) {
            while(!cur.isAfterLast) {
                var record = LDBFixtureRecord()
                var i = 0
                members.forEach {
                    val colName = it.name
                    val prop = members.find{it.name == colName} as KMutableProperty<*>
                    val setter = prop.javaSetter
                    val paramType = setter?.parameterTypes?.get(0)?.simpleName
                    if(paramType == "Int") {
                        setter.invoke(record, cur.getInt(i))
                    }
                    else {
                        if(setter != null) {
                            setter.invoke(record, cur.getString(i))
                        }
                    }
                    i+=1

                }
                rtnList.add(record)
            }
        }

        return rtnList
    }
}