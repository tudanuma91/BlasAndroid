package com.v3.basis.blas.ui.item.item_search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.v3.basis.blas.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ItemSearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ItemSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemSearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_item_search, container, false)
    }

}
