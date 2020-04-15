package com.v3.basis.blas.ui.terminal.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.v3.basis.blas.ui.logout.LogoutFragment
import com.v3.basis.blas.ui.terminal.dashboards.DashboardsFragment
import com.v3.basis.blas.ui.terminal.fixture.FixtureFragment
import com.v3.basis.blas.ui.terminal.project.ProjectFragment
import com.v3.basis.blas.ui.terminal.status.StatusFragment


class TerminalPagerAdapter(childFragmentManager: FragmentManager) : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 5
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return null
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> { DashboardsFragment() }
            1 -> { ProjectFragment() }
            2 -> { FixtureFragment() }
            3 -> { LogoutFragment() }
            4 -> { StatusFragment() }
            else -> { DashboardsFragment() }
        }
    }
}
