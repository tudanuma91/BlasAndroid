package com.v3.basis.blas.blasclass.db.field

import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.ItemsController
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import net.sqlcipher.database.SQLiteDatabase
import org.json.JSONObject

class FieldController(context: Context, projectId: String) : BaseController(context, projectId){

    fun test() {

        SQLiteDatabase.loadLibs(context);
        val path = DownloadWorker.getSavedPath(projectId)

        Log.d("path", path)
        val testPath = path + ".test.db"

        val database: SQLiteDatabase =
            SQLiteDatabase.openOrCreateDatabase(testPath, "test123", null)

        database.execSQL("create table t1(a, b)");
        database.execSQL(
            "insert into t1(a, b) values(?, ?)", arrayOf<Any>(
                "one for the money",
                "two for the show"
            )
        )

        Log.d("test()","end")
    }

    fun test2() {

        val database: SQLiteDatabase =
            SQLiteDatabase.openOrCreateDatabase(
                "/data/user/0/com.v3.basis.blas/databases/441_200827_091209_2.sql.db"
                , "aaa", null)

        val sql = "select fields.*,2 as edit_id  from fields order by col"
        val cursor = database?.rawQuery(sql, null)

        cursor?.also {
            var notLast = it.moveToFirst()

            while(notLast) {

                val field = setProperty(LdbFieldRecord(),it) as LdbFieldRecord

                Log.d("field_name",field.name)
                notLast = it.moveToNext()

            }

        }

        cursor?.close()

    }


    fun searchDisp() : List<LdbFieldRecord> {
        Log.d("Field.search()", "start!!!!!!!!!!!!!!!!!!!!!!!")

//        test()
//        test2()

        val user = getUserInfo()
        var user_id = 1
        var group_id = 1
        if(null != user) {
            user_id = user.user_id
            group_id = user.group_id
        }

        val groupRight = getGroupsValue(group_id, "data_disp_hidden_column")

        var sql = ""
        var selection = arrayOf<String>()
        if( 1 == groupRight ) {
            sql = "select fields.*,2 as edit_id  from fields order by col"
        }
        else {
            sql = "select fields.*,right_fields.edit_id as edit_id " +
                    "from fields " +
                    "join(" +
                    "select right_id,col,edit_id " +
                    "from right_fields " +
                    "where edit_id != 0 " +
                    ") as right_fields " +
                    "on fields.col = right_fields.col " +
                    "join( " +
                    "select right_id " +
                    "from right_users " +
                    "where user_id = ? " +
                    ") as right_users " +
                    "on right_fields.right_id = right_users.right_id "
            selection  += user_id.toString()
        }
        Log.d("field sql", sql)

        val cursor = db?.rawQuery(sql, selection)
        val ret = mutableListOf<LdbFieldRecord>()

        cursor?.also {
            var notLast = it.moveToFirst()
            while (notLast) {
                val field = setProperty(LdbFieldRecord(), it) as LdbFieldRecord

                if( ItemsController.FIELD_TYPE_SINGLE_SELECT == field.type
                    &&  0 != field.parent_field_id
                ) {
                    // 連動パラメータ(とりあえず文字列だけが表示されるようにした)
                    var new_choice = ""
                    val choice = field.choice?.replace("\\\"", "\"")
                    val jsonChoice = JSONObject(choice)

                    var first = true
                    val parents = jsonChoice.names()

                    for( i in 0 until parents.length() ) {
                        val child = jsonChoice.getString(parents[i].toString())

                        if( !first ) {
                            new_choice += ","
                        }
                        new_choice += child
                        first = false

                    }
                    field.choice = new_choice
                }

                ret.add(field)
                notLast = it.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }


}