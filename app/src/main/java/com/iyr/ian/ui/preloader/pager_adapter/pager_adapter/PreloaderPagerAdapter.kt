package com.iyr.ian.ui.preloader.pager_adapter.pager_adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.iyr.ian.ui.preloader.pager_adapter.pager_adapter.fragment.PreloaderPageFourFragment
import com.iyr.ian.ui.preloader.pager_adapter.pager_adapter.fragment.PreloaderPageLastFragment
import com.iyr.ian.ui.preloader.pager_adapter.pager_adapter.fragment.PreloaderPageOneFragment
import com.iyr.ian.ui.preloader.pager_adapter.pager_adapter.fragment.PreloaderPageThreeFragment
import com.iyr.ian.ui.preloader.pager_adapter.pager_adapter.fragment.PreloaderPageTwoFragment

class PreloaderPagerAdapter(
    val context: Context,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    //   private lateinit var participantsInfoFragment: BottomSheetViewersFragment
    var fragments = ArrayList<Fragment>()

    init {

        fragments.add(PreloaderPageOneFragment(context))
        fragments.add(PreloaderPageTwoFragment(context))
        fragments.add(PreloaderPageThreeFragment(context))
        fragments.add(PreloaderPageFourFragment(context))
        fragments.add(PreloaderPageLastFragment(context))
    }

    override fun createFragment(position: Int): Fragment {

        return fragments[position]
    }

    override fun getItemCount() = fragments.size
    fun setData(fragments: ArrayList<Fragment>) {
        this.fragments.clear()
        this.fragments.addAll(fragments)
    }


}