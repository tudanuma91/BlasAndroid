package com.v3.basis.blas.ui.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.ext.setActionBarTitle
import com.v3.basis.blas.ui.terminal.adapter.TerminalPagerAdapter
import kotlinx.android.synthetic.main.fragment_terminal.*
import kotlinx.android.synthetic.main.fragment_terminal.view.*

class TerminalFragment : Fragment() {

    private lateinit var vm: TerminalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        vm = ViewModelProviders.of(this).get(TerminalViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)
        view.pager.adapter = TerminalPagerAdapter(requireFragmentManager()) // childFragmentManager?
        view.pager.offscreenPageLimit = 5
        view.pager.setCurrentItem(0, false)

        view.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                BottomNavButton.find(position).apply {
                    nav_view.menu.findItem( this.id ).isChecked = true
                    setTitle(this)
                }
            }
        })

        view.nav_view.setOnNavigationItemSelectedListener {
            val item = BottomNavButton.findById(it.itemId)
            switchScreen(item)
            true
        }

       /*
       バッジを表示する処理。values/stylesの変更をしたらできる。
       view.nav_view.menu.getItem(4).itemId.let {
            view.nav_view.getOrCreateBadge(it).apply {
                number = 10
            }

        }*/

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchScreen(BottomNavButton.DASH_BOARD)
    }

    private fun switchScreen(item: BottomNavButton) {
        pager.setCurrentItem(item.ordinal, false)
        setTitle(item)
    }

    private fun setTitle(item: BottomNavButton) {
        (requireActivity() as AppCompatActivity).setActionBarTitle(item.title)
    }

    private enum class BottomNavButton(val id: Int, val title: Int) {

        DASH_BOARD(R.id.navigation_dashboard, R.string.navi_title_terminal_dashboard),
        DATA_MANAGE(R.id.navigation_data_management, R.string.navi_title_terminal_item),
        EQUIPMENT_MANAGE(R.id.navigation_equipment_management, R.string.navi_title_terminal_fixture),
        LOGOUT(R.id.navigation_logout, R.string.navi_title_terminal_logout),
        STATUS(R.id.navigation_status,R.string.navi_title_terminal_status);

        companion object {
            fun find(position: Int): BottomNavButton {
                return values().get(position)
            }
            fun findById(id: Int): BottomNavButton {
                return values().first { id == it.id }
            }
        }
    }
}
