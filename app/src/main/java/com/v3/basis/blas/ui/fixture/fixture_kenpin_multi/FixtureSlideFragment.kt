package com.v3.basis.blas.ui.fixture.fixture_kenpin_multi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.v3.basis.blas.R
import com.v3.basis.blas.ui.common.ARG_PROJECT_ID
import com.v3.basis.blas.ui.common.ARG_PROJECT_NAME
import com.v3.basis.blas.ui.common.ARG_TOKEN
import com.v3.basis.blas.ui.common.FixtureBaseFragment
import com.v3.basis.blas.ui.fixture.fixture_kenpin.FixtureKenpinFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
* バーコード一括読みのカメラフラグメントと、読み込んだ値を表示するリストフラグメントを
 * スライドビューで管理するフラグメント。
 */
class FixtureSlideFragment : FixtureBaseFragment() {
    private lateinit var viewPager: ViewPager2
    private val NUM_PAGES:Int = 2
    private val bundle = Bundle()

    lateinit var cameraFragment:FixtureKenpinMultiFragment

    companion object {
        fun newInstance() = FixtureSlideFragment()
        lateinit var listFragment:FixtureKenpinItemsFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bundle.putString("token", token)
        bundle.putString("project_id", projectId)
        bundle.putString("project_name", projectName)
        listFragment = FixtureKenpinItemsFragment.newInstance().apply { arguments = bundle}
        cameraFragment = FixtureKenpinMultiFragment.newInstance().apply { arguments = bundle}


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fixture_slide, container, false)
        viewPager = view.findViewById<ViewPager2>(R.id.pager)
        val pagerAdapter = ScreenSlidePagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter

        return view
    }

    override fun onResume() {
        //土台となるslideFragmentにonResumeのイベントは来るが、
        //さらにその上のcameraFragmentにはonResumeのイベントが来ないので
        //自家発電する
        cameraFragment.onResume()
        listFragment.onResume()
        super.onResume()
    }

    override fun onPause() {
        //土台となるslideFragmentにonPauseのイベントは来るが、
        //さらにその上のcameraFragmentにはonResumeのイベントが来ないので
        //自家発電する
        cameraFragment.onPause()
        listFragment.onPause()
        super.onPause()
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES
        override fun createFragment(position: Int): Fragment {
            if(position == 0) {
                return cameraFragment
            }
            else {
                return listFragment
            }
        }
    }
}