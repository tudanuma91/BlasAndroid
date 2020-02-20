package com.v3.basis.blas.ui.terminal.dashboards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.v3.basis.blas.R

class DashboardsFragment : Fragment() {

    private lateinit var dashboardsViewModel: DashboardsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardsViewModel =
            ViewModelProviders.of(this).get(DashboardsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboards, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboards)
        dashboardsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}