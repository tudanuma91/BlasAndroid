package com.v3.basis.blas.ui.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.ui.ext.*
import com.v3.basis.blas.ui.terminal.adapter.TerminalPagerAdapter
import kotlinx.android.synthetic.main.fragment_terminal.*
import kotlinx.android.synthetic.main.fragment_terminal.view.*

class TerminalFragment : Fragment() {

    private val vm: TerminalViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

//        vm = ViewModelProviders.of(this).get(TerminalViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)
        view.pager.adapter = TerminalPagerAdapter(childFragmentManager) // childFragmentManager?
        view.pager.offscreenPageLimit = 5
        view.pager.setCurrentItem(0, false)

        view.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var disable = false
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                BottomNavButton.find(position).apply {
                    if (disable.not()) {
                        //  データ管理の場合、機器管理とスワップする
                        if (this == BottomNavButton.DATA_MANAGE) {
                            disable = true
                            switchScreen(BottomNavButton.EQUIPMENT_MANAGE)
                            (requireActivity() as TerminalActivity).beforeSelectedNavButton = BottomNavButton.DATA_MANAGE
                            nav_view.menu.findItem( BottomNavButton.DATA_MANAGE.id ).isChecked = true
                        } else {
                            nav_view.menu.findItem( this.id ).isChecked = true
                        }
                        setTitle(this.title)
                    } else {
                        disable = false
                    }
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

        (requireActivity() as AppCompatActivity).supportActionBar?.also {
            it.customView.findViewById<Button>(R.id.searchButton).setOnClickListener { v ->
                val text = it.customView.findViewById<EditText>(R.id.searchBox).text.toString()
                vm.filterProject(text)
            }
        }

        val act = requireActivity() as TerminalActivity
        switchScreen(act.beforeSelectedNavButton)
    }

    private fun switchScreen(_item: BottomNavButton) {

        val item = if (_item == BottomNavButton.DATA_MANAGE) {
            BottomNavButton.EQUIPMENT_MANAGE to BottomNavButton.DATA_MANAGE.title
        } else _item to _item.title

        pager.setCurrentItem(item.first.ordinal, false)
        setTitle(item.second)

        val act = requireActivity() as TerminalActivity
        act.beforeSelectedNavButton = if (_item == BottomNavButton.DATA_MANAGE) {
            nav_view.menu.findItem( BottomNavButton.DATA_MANAGE.id ).isChecked = true
            BottomNavButton.DATA_MANAGE
        } else item.first

        checkHideState(_item)
    }

    private fun checkHideState(item: BottomNavButton) {

        when (item) {
            BottomNavButton.DATA_MANAGE,
            BottomNavButton.EQUIPMENT_MANAGE -> {
                showViewForCustomActionBar(arrayOf(R.id.searchBox, R.id.searchButton))
            }
            else -> {
                hideViewForCustomActionBar(arrayOf(R.id.searchBox, R.id.searchButton))
            }
        }
    }

    private fun setTitle(title: Int) {
        setViewTitle(getString(title))
    }
}
