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


    fun getFieldRecords() : List<LdbFieldRecord> {
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

        val cursor = db?.rawQuery(sql, selection)
        val ret = mutableListOf<LdbFieldRecord>()

        cursor?.also {
            var notLast = it.moveToFirst()
            while (notLast) {
                val field = setProperty(LdbFieldRecord(), it) as LdbFieldRecord

                if( ItemsController.FIELD_TYPE_SINGLE_SELECT == field.type
                    &&  0 != field.parent_field_id
                ) {
                    // ?????????????????????(???????????????????????????????????????????????????????????????)
                    var new_choice = ""
                    val choice = field.choice?.replace("\\\"", "\"")

                    field.choice = choice
                }

                ret.add(field)
                notLast = it.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    companion object {
        fun getChildFields(myId:Int, fieldsRecordList:List<LdbFieldRecord>) : MutableList<LdbFieldRecord> {
            val childFieldList = mutableListOf<LdbFieldRecord>()

            fieldsRecordList.forEach {record->
                if(record.parent_field_id == myId) {
                    childFieldList.add(record)
                }
            }
            return childFieldList
        }
    }


}