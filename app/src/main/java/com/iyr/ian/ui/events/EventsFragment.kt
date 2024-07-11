package com.iyr.ian.ui.events


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.events.fragments.EventAdditionalMediaFragment
import com.iyr.ian.ui.events.fragments.EventLocationReadOnlyFragment
import com.iyr.ian.ui.events.fragments.EventRealTimeTrackingFragment
import com.iyr.ian.ui.events.fragments.EventTypeSelectorFragment
import com.iyr.ian.ui.interfaces.EventsPublishingCallback
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.viewmodels.MainActivityViewModel
import kotlinx.coroutines.launch


interface OnPostFragmentInteractionCallback {
    fun OnSwitchFragmentRequest(fragmentId: Int, arguments: Bundle? = null)
    fun resetCurrentEvent()
    fun onPublishEventDone(event: Event?)
}

class EventsFragment(val callback: EventsPublishingCallback, val mainActivityViewModel: MainActivityViewModel) : Fragment(),
    OnPostFragmentInteractionCallback {

    fun EventsFragment() {}


    private var locationSelectionLayout: MotionLayout? = null
    private val mFragmentEventSelection: EventTypeSelectorFragment by lazy {
        EventTypeSelectorFragment(
           this, viewModel,mainActivityViewModel
        )
    }
    private val mFragmentLocationTypeSelector by lazy { EventRealTimeTrackingFragment(this,  viewModel,mainActivityViewModel) }
    private val mFragmentLocationReadOnly by lazy { EventLocationReadOnlyFragment(this,  viewModel,mainActivityViewModel) }
    private val mFragmentAditionalMediaSelector: EventAdditionalMediaFragment by lazy {
        EventAdditionalMediaFragment(
            this,
            viewModel,
            mainActivityViewModel
        )
    }


    private var functionTitle: TextView? = null
    private var atMyLocationButton: View? = null
    private var mainButtonsSection: View? = null
    private var mainButtosScrollview: View? = null
    private var mainScreen: ConstraintLayout? = null
    private var persecutionSection: ConstraintLayout? = null
    private var locationSelectorTitle: TextView? = null
  //  private lateinit var thisEvent: Event

    private val viewModel: EventsFragmentViewModel by lazy { EventsFragmentViewModel() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        viewModel.initializeEvent(SessionForProfile.getInstance(requireContext()).getUserId())
        mFragmentEventSelection.setCallback(this)
        requireContext().broadcastMessage(
            null,
            AppConstants.ServiceCode.BROADCAST_ACTION_HIDE_FOOTER_TOOLBAR
        )
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchToFragment(R.id.event_fragment_event_type_selector, arguments)
        resetCurrentEvent()
        setupUI()
        showButtons()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setTitleBarTitle(R.string.action_publish_event)
        AppClass.instance.setCurrentFragment(this)

        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.action_publish_event))
        }
       // AppClass.instance.addViewToStack(IANModulesEnum.EVENTS_TRACKING, this)
    }


    override fun onPause() {
        super.onPause()
      //  AppClass.instance.removeViewFromStack( this)
    }


    fun switchToFragment(fragmentId: Int, arguments: Bundle? = null) {

        lifecycleScope.launch {
            when (fragmentId) {
                R.id.event_fragment_event_type_selector -> {
                    arguments?.let { args ->
                        mFragmentEventSelection.arguments = args
                    }
                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, mFragmentEventSelection)
                        .addToBackStack(null)
                        .commit()
                }

                R.id.event_fragment_realtime_selector -> {
                    arguments?.let { args ->
                        mFragmentLocationTypeSelector.arguments = args
                    }

                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, mFragmentLocationTypeSelector)
                        .addToBackStack(null)
                        .commit()
                }

                R.id.event_fragment_location_read_only_selector -> {
                    arguments?.let { args ->
                        mFragmentLocationReadOnly.arguments = args
                    }

                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, mFragmentLocationReadOnly)
                        .addToBackStack(null)
                        .commit()
                }

                R.id.event_fragment_aditional_media_selector -> {
                    arguments?.let { args ->
                        mFragmentAditionalMediaSelector.arguments = args
                    }

                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, mFragmentAditionalMediaSelector)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    private fun showButtons() {

    }

    private fun setupUI() {

    }

    private fun showNotifyButton() {
    }

    //--------------- EVENTS ------------------------------------------------------
    override fun resetCurrentEvent() {
   //     thisEvent = createEvent()
        viewModel.initializeEvent(SessionForProfile.getInstance(requireContext()).getUserId())
    }

    private fun createEvent(): Event {
        SessionForProfile.getInstance(requireContext()).getUserProfile()
        val event = Event()
        return event
    }

    override fun onPublishEventDone(event: Event?) {
        callback.onPublishEventDone(event)
    }


    fun onPublishEventDone() {
        if (isVisible) {
            resetCurrentEvent()
            switchToFragment(R.id.event_fragment_event_type_selector, arguments)
        }
    }

    companion object;


    override fun OnSwitchFragmentRequest(fragmentId: Int, arguments: Bundle?) {
        switchToFragment(fragmentId, arguments)
    }


    fun getMediaFragment(): EventAdditionalMediaFragment {
        return mFragmentAditionalMediaSelector
    }

    fun updateUI() {
        mFragmentEventSelection.updateUI()
    }

    fun getEvent(): Event {
        return viewModel.event.value!!
    }
}