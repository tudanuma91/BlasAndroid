package com.v3.basis.blas.ui.itemlist


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

import com.v3.basis.blas.R

/**
 * A simple [Fragment] subclass.
 */
class ItemListFragment : Fragment() {
    private var token:String? = null
    private var project_id:String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val extras = activity?.intent?.extras
        if(extras?.getString("token") != null) {
            token = extras?.getString("token")
        }
        if(extras?.getString("project_id") != null) {
            project_id = extras?.getString("project_id")
        }


        val root = inflater.inflate(R.layout.fragment_item_list, container, false)
        val textView: TextView = root.findViewById(R.id.item_list_text)
        textView.text = "token(${token})\np_id(${project_id})"

        return root
    }

    override fun onStart() {
        super.onStart()
        val layout = LinearLayout(activity)
        val button = Button(activity)
        button.text = "send"
        layout.addView(button)
    }

}
