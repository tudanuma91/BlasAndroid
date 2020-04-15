package com.v3.basis.blas.ui.viewparts.CardRecycler

import android.database.Cursor
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.ui.terminal.status.UnRead.AlreadyRowModel
import com.v3.basis.blas.ui.terminal.status.UnRead.AlreadyViewAdapter

open class CardRecyclerStatusAlready(act: FragmentActivity?,
                                     reView: RecyclerView,
                                     adapter: AlreadyViewAdapter,
                                     cursor: Cursor,
                                     projectMap:MutableMap<String,String>):CardRecycler(act,reView){
    private val adapter = adapter
    private val cursor = cursor
    private val dataList = mutableListOf<AlreadyRowModel>()
    private val projectMap = projectMap

    override fun createRecyclerView(): RecyclerView {
        val recycle = super.createRecyclerView()
        recycle.adapter = adapter
        return recycle
    }

    open fun createStatusList(): MutableList<AlreadyRowModel> {
        Log.d("デバック用","${cursor}")
        while (cursor.moveToNext()){val idxNote = cursor.getColumnIndex("id")
            val text = createStatusText(cursor,projectMap)
            val data: AlreadyRowModel =
                AlreadyRowModel().also {
                    it.title =  cursor.getString(idxNote)
                    it.detail = text
                }
            dataList.add(data)
        }
        return dataList
    }

}