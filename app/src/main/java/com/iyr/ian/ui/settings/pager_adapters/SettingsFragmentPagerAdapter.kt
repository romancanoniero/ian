package com.iyr.ian.ui.settings.pager_adapters

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.ui.settings.SettingsFragmentViewModel
import com.iyr.ian.ui.settings.SettingsFragmentsEnum
import com.iyr.ian.ui.settings.landing_fragment.SettingsLandingFragment
import com.iyr.ian.ui.settings.press_or_tap_setup.PressOrTapSetupFragment
import com.iyr.ian.ui.settings.profile_settings.ProfileSettingsFragment
import com.iyr.ian.viewmodels.MainActivityViewModel

class SettingsFragmentPagerAdapter(
    mainActivityViewModel: MainActivityViewModel,
    viewModel: SettingsFragmentViewModel,
    val mainActivity: Activity,
    _interface: ISettingsFragment,
    fragmentManager: FragmentManager
) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val mSettingsLandingFragment by lazy { SettingsLandingFragment(
        mainActivityViewModel,
        viewModel,
        _interface
    ) }
    private val mPressOrTapFragment by lazy { PressOrTapSetupFragment(
        mainActivityViewModel,
        viewModel,
        _interface
    ) }
    private val mProfileSettingsFragment by lazy { ProfileSettingsFragment(
        mainActivityViewModel,
        viewModel,
        _interface
    ) }
    private var count = 3
    private var fragmentsArray = ArrayList<Fragment>()


    override fun getCount(): Int {
        return count
    }

    override fun getItem(position: Int): Fragment {

        when (position) {
            SettingsFragmentsEnum.LANDING.ordinal -> return mSettingsLandingFragment
            SettingsFragmentsEnum.SOS_SETTINGS.ordinal -> return mPressOrTapFragment
            SettingsFragmentsEnum.PROFILE_SETTINGS.ordinal -> return mProfileSettingsFragment
        }
        return mSettingsLandingFragment
    }

    fun getFragments(): ArrayList<Fragment> {
        return fragmentsArray
    }

    fun getFragmentAt(index: Int): Fragment {
        when (index) {
            SettingsFragmentsEnum.LANDING.ordinal -> return mSettingsLandingFragment
            SettingsFragmentsEnum.SOS_SETTINGS.ordinal -> return mPressOrTapFragment
            SettingsFragmentsEnum.PROFILE_SETTINGS.ordinal -> return mProfileSettingsFragment
        }
        return mSettingsLandingFragment
    }


}