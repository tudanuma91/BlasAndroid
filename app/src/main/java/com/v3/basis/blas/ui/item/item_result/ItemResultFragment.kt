package com.v3.basis.blas.ui.item.item_result

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.ui.ext.addTitle


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ItemResultFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ItemResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemResultFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_item_result, container, false)
        val btn_return = root.findViewById<Button>(R.id.btn_return)
       // val second2Fragment = ItemResultFragment()
        val itemActivity = activity as ItemActivity?
        btn_return.setOnClickListener{
            itemActivity?.deleteFragment(this)
            Log.d("aaa","よばれたよ！！")
        }

        return root
    }
}
