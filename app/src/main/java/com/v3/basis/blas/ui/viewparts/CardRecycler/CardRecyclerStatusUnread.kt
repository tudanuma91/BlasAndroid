package com.v3.basis.blas.ui.viewparts.CardRecycler

import android.database.Cursor
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.v3.basis.blas.ui.terminal.status.UnRead.UnReadRowModel
import com.v3.basis.blas.ui.terminal.status.UnRead.UnReadViewAdapter

open class CardRecyclerStatusUnread(act:FragmentActivity?,
                                    reView:RecyclerView,
                                    adapter: UnReadViewAdapter,
                                    cursor: Cursor,
                                    projectMap:MutableMap<String,String>):CardRecycler(act,reView) {

    private val adapter = adapter
    private val cursor:Cursor = cursor
    private val dataList = mutableListOf<UnReadRowModel>()
    private val projectMap = projectMap

    override fun createRecyclerView(): RecyclerView {
        val recycle = super.createRecyclerView()
        recycle.adapter = adapter
        return recycle
    }

    open fun createStatusList(): MutableList<UnReadRowModel> {
        Log.d("デバック用","${cursor}")
        while (cursor.moveToNext()){
            val idxNote = cursor.getColumnIndex("id")
            val text = createStatusText(cursor,projectMap)
            val data: UnReadRowModel =
                UnReadRowModel().also {
                    it.title =  cursor.getString(idxNote)
                    it.detail = text
                    it.statusId = cursor.getString(idxNote)
                }
            dataList.add(data)
        }
        return dataList
    }


}