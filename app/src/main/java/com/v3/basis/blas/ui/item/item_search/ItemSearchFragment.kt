package com.v3.basis.blas.ui.item.item_search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.Test3Activity
import com.v3.basis.blas.ui.item.item_result.ItemResultFragment
import com.v3.basis.blas.ui.test3.Test3Second.Test3Second2Fragment

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
        val root= inflater.inflate(R.layout.fragment_item_search, container, false)
        val btn_search = root.findViewById<Button>(R.id.btn_search)
        val second2Fragment = ItemResultFragment()
        val itemActivity = activity as ItemActivity?
        btn_search.setOnClickListener{
            itemActivity?.replaceFragment(second2Fragment)
            Log.d("aaa","よばれたよ！！")
        }
        return root
    }

}
