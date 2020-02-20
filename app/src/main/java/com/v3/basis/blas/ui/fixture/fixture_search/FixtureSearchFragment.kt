package com.v3.basis.blas.ui.fixture.fixture_search

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.v3.basis.blas.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FixtureSearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FixtureSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FixtureSearchFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fixture_search, container, false)
    }

}
