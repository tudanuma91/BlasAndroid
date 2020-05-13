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
import com.v3.basis.blas.activity.TerminalActivity
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
            BottomNavButton.findById(it.itemId)?.also { item ->
                switchScreen(item)
            }
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

        val act = requireActivity() as TerminalActivity
        switchScreen(act.beforeSelectedNavButton)
    }

    private fun switchScreen(item: BottomNavButton) {
        pager.setCurrentItem(item.ordinal, false)
        setTitle(item)

        val act = requireActivity() as TerminalActivity
        act.beforeSelectedNavButton = item
    }

    private fun setTitle(item: BottomNavButton) {
        (requireActivity() as AppCompatActivity).setActionBarTitle(item.title)
    }
}
