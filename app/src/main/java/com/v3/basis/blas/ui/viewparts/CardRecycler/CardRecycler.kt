package com.v3.basis.blas.ui.viewparts.CardRecycler

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
}