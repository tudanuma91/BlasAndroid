package com.v3.basis.blas.ui.viewparts.CardRecycler

import android.database.Cursor
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.ui.viewparts.Part

open class CardRecycler(act:FragmentActivity?, reView:RecyclerView) : Part(){
    private val act = act
    private val reView = reView

    open fun createRecyclerView(): RecyclerView {
        reView.setHasFixedSize(true)
        reView.layoutManager = LinearLayoutManager(act)
        return reView
    }

    open fun createStatusText(cursor:Cursor,projectMap: MutableMap<String,String>): String {
        val updateNote = cursor.getColumnIndex("update_date")
        val funcNameNote = cursor.getColumnIndex("func_name")
        val operationNote = cursor.getColumnIndex("operation")
        val projectNote = cursor.getColumnIndex("project_id")
        val aplNote = cursor.getColumnIndex("apl_code")
        var operationValue = cursor.getString(operationNote)
        var test = ""
        if(operationValue == null || operationValue == ""){
            operationValue = "表示"
        }
        if(cursor.getString(aplNote) == "0" ){
            test = "正常"
        }else{
            test = "異常"
        }
        val projectName = projectMap[cursor.getString(projectNote)]

        var value = ""
        value += "日時："
        value += "${cursor.getString(updateNote)}\n"
        value += "機能："
        value += "${cursor.getString(funcNameNote)}\n"
        value += "操作："
        value += "${operationValue}\n"
        value += "プロジェクト名："
        value += "${projectName}\n"
        value += "ステータス："
        value += "${test}\n"


        return value
    }
}