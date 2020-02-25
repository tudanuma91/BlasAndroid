package com.v3.basis.blas.ui.item.item_create


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity

/**
 * A simple [Fragment] subclass.
 */
class ItemCreateFragment : Fragment() {
    var fragmentTitle:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_item_create, container, false)
    }


}
