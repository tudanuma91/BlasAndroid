package com.v3.basis.blas.ui.fixture.fixture_return


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.v3.basis.blas.R

/**
 * A simple [Fragment] subclass.
 */
class FixtureReturnFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fixture_return, container, false)
    }


}
