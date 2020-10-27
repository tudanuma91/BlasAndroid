package com.v3.basis.blas.ui.fixture

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.controller.FixtureController

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
public const val ARG_TOKEN = "token"
public const val ARG_PROJECT_ID = "project_id"
public const val ARG_PROJECT_NAME = "project_name"

/**
 * A simple [Fragment] subclass.
 * Use the [FixtureBaseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class FixtureBaseFragment : Fragment() {
    protected lateinit var token: String
    protected lateinit var projectId:String
    protected lateinit var  projectName:String
    protected lateinit var fixtureController: FixtureController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString(ARG_TOKEN).toString()
            projectId = it.getString(ARG_PROJECT_ID).toString()
            projectName = it.getString(ARG_PROJECT_NAME).toString()
        }

        //コントローラーの生成
        fixtureController = FixtureController(BlasApp.applicationContext(), projectId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fixture_base, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FixtureBaseFragment.
         */
        @JvmStatic
        fun newInstance(token: String, projectId: String, projectName:String) =
            FixtureBaseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TOKEN, token)
                    putString(ARG_PROJECT_NAME, projectId)
                    putString(ARG_PROJECT_ID, projectName)
                }
            }
    }
}