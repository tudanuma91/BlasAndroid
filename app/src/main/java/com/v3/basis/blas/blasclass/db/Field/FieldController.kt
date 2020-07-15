package com.v3.basis.blas.blasclass.db.Field

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.ldb.LdbFixtureDispRecord

class FieldController(context: Context, projectId: String) : BaseController(context, projectId){

    // TODO:三代川！！！！
    fun searchDisp() : List<LdbFieldRecord> {
        Log.d("Field.search()","start!!!!!!!!!!!!!!!!!!!!!!!")

        // TODO:とりあえず
        val sql = "select * from fields order by col"

        val cursor = db?.rawQuery(sql,null)
        val ret = mutableListOf<LdbFieldRecord>()

        cursor?.also {
            var notLast = it.moveToFirst()
            while (notLast) {
                val field = setProperty(LdbFieldRecord(),it) as LdbFieldRecord
                ret.add(field)
                notLast = it.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }


}