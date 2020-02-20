package com.v3.basis.blas.ui.item.item_edit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.v3.basis.blas.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ItemEditFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ItemEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemEditFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_item_edit, container, false)
    }

}
