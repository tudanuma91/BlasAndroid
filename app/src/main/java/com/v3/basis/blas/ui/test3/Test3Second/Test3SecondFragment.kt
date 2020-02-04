package com.v3.basis.blas.ui.test3.Test3Second

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.v3.basis.blas.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Test3SecondFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Test3SecondFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Test3SecondFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test3_second, container, false)
    }

}
