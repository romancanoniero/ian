package com.iyr.ian.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.iyr.ian.R
import com.iyr.ian.databinding.FragmentSettingsBinding
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.pager_adapters.SettingsFragmentPagerAdapter
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.UserViewModel
import kotlin.math.abs


interface ISettingsFragment {
    fun goToFragment(index: Int, extras: Bundle? = null)
}


enum class SettingsFragmentsEnum {
    LANDING, SOS_SETTINGS, PROFILE_SETTINGS, NOTIFICATION_GROUPS, NOTIFICATION_LIST, PUSH_BUTTONS_SETTINGS, PLAN_SETTINGS
}


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment() : Fragment() {


    private val mainActivityViewModel: MainActivityViewModel by lazy {
        MainActivityViewModel.getInstance(
            requireContext(),
            UserViewModel.getInstance().getUser()?.user_key.toString()
        )
    }

    private var viewModel: SettingsFragmentViewModel = SettingsFragmentViewModel()

    private fun setupObservers() {
        mainActivityViewModel.user.observe(this) { user ->
            viewModel.onUserChanged(user)
        }
    }

    private fun removeObservers() {
        viewModel.fragmentVisible.removeObservers(viewLifecycleOwner)
    }


    var currentFragment: SettingsFragmentsEnum = SettingsFragmentsEnum.LANDING
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var pagerAdapter: SettingsFragmentPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        setupUI()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            context: Context,
            mainActivityViewModel: MainActivityViewModel
        ) =
            SettingsFragment()
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.action_settings))
        }
        val appToolbar = (requireActivity() as MainActivity).appToolbar
        appToolbar.enableBackBtn(true)
        appToolbar.updateTitle(getString(R.string.action_settings))
        val bottomToolBar = (requireActivity() as MainActivity).binding.bottomToolbar
        bottomToolBar.visibility = View.GONE
        setupObservers()
    }

    override fun onPause() {
        removeObservers()
        super.onPause()
    }

    private fun setupUI() {
        pagerAdapter = SettingsFragmentPagerAdapter(
            mainActivityViewModel,
            viewModel,
            requireActivity(),
            this as ISettingsFragment,
            childFragmentManager
        )
        // binding.pager.adapter = pagerAdapter
        //  binding.pager.setPageTransformer(false, FadePageTransformer())
        //   binding.pager.offscreenPageLimit = 3
//    binding.pager.setCurrentItem(0, true)
        //  goToFragment(0)

        //  val action  = SettingsFragmentDirections.

    }

    /*
    fun getFragment(index: Int): Fragment? {
        when (index) {
            SettingsFragmentsEnum.LANDING.ordinal -> {
                return mSettingsLandingFragment
            }

            SettingsFragmentsEnum.SOS_SETTINGS.ordinal -> {
                return mPressOrTapFragment
            }

            SettingsFragmentsEnum.PROFILE_SETTINGS.ordinal -> {
                return mProfileSettingsFragment
            }

            SettingsFragmentsEnum.NOTIFICATION_GROUPS.ordinal -> {
                return mContactsGroupsFragment
            }

            SettingsFragmentsEnum.PUSH_BUTTONS_SETTINGS.ordinal -> {
                return mPushButtonConfigFragment
            }


            SettingsFragmentsEnum.PLAN_SETTINGS.ordinal -> {
                return mSubscriptionsPlanFragment
            }

        }
        return null
    }

    override fun goToFragment(index: Int, extras: Bundle?) {
        // binding.pager.setCurrentItem(index, true)

        val transaction: FragmentTransaction =
            childFragmentManager.beginTransaction()

        when (index) {

            SettingsFragmentsEnum.LANDING.ordinal -> {
                extras?.let { data ->
                    mSettingsLandingFragment.arguments = data
                }
                transaction.replace(
                    R.id.container,
                    mSettingsLandingFragment
                )
            }

            SettingsFragmentsEnum.SOS_SETTINGS.ordinal -> {

                extras?.let { data ->
                    mPressOrTapFragment.arguments = data
                }
                transaction.replace(
                    R.id.container,
                    mPressOrTapFragment
                )
            }

            SettingsFragmentsEnum.PUSH_BUTTONS_SETTINGS.ordinal -> {

                extras?.let { data ->
                    mPushButtonConfigFragment.arguments = data
                }

                transaction.replace(
                    R.id.container,
                    mPushButtonConfigFragment
                )
            }

            SettingsFragmentsEnum.NOTIFICATION_GROUPS.ordinal -> {

                extras?.let { data ->
                    mContactsGroupsFragment.arguments = data
                }

                transaction.replace(
                    R.id.container,
                    mContactsGroupsFragment
                )
            }

            SettingsFragmentsEnum.NOTIFICATION_LIST.ordinal -> {
                extras?.let { data ->
                    mNotificationListFragment.arguments = data
                }
                transaction.replace(
                    R.id.container,
                    mNotificationListFragment
                )
            }


            SettingsFragmentsEnum.PROFILE_SETTINGS.ordinal -> {
                extras?.let { data ->
                    mProfileSettingsFragment.arguments = data
                }

                transaction.add(
                    R.id.container,
                    mProfileSettingsFragment
                )
            }

            SettingsFragmentsEnum.PLAN_SETTINGS.ordinal -> {
                extras?.let { data ->
                    mSubscriptionsPlanFragment.arguments = data
                }

                transaction.add(
                    R.id.container,
                    mSubscriptionsPlanFragment
                )
            }
        }
        transaction.addToBackStack(null)
        transaction.commit()
        try {
            childFragmentManager.executePendingTransactions()

        } catch (ex: Exception) {
            var pp = 3
        }
        currentFragment = SettingsFragmentsEnum.values()[index]
    }
*/

    fun pagerAdapter(): SettingsFragmentPagerAdapter {
        return pagerAdapter
    }
    /*
        fun pager(): NonSwipeViewPager {
            return binding.pager

        }
    */

    inner class FadePageTransformer : ViewPager.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            view.translationX = view.width * -position

            if (position <= -1.0f || position >= 1.0f) {
                view.alpha = 0.0f
                view.visibility = View.GONE
            } else if (position == 0.0f) {
                view.alpha = 1.0f
                view.visibility = View.VISIBLE
            } else {
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                view.alpha = 1.0f - abs(position)
                view.visibility = View.GONE
            }
        }
    }

}
