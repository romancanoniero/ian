package com.iyr.ian.ui.events.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.databinding.FragmentEventRealTimeTrackerSelectorBinding
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.events.EventsFragmentViewModel
import com.iyr.ian.ui.events.OnPostFragmentInteractionCallback
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.getEventTypeDrawable
import com.iyr.ian.utils.getEventTypeName
import com.iyr.ian.viewmodels.MainActivityViewModel


class EventRealTimeTrackingFragment(
    // var thisEvent: Event,
    val callback: OnPostFragmentInteractionCallback,
    val eventsFragmentViewModel: EventsFragmentViewModel,
    val mainActivityViewModel: MainActivityViewModel
) :
    Fragment() {
    private lateinit var binding: FragmentEventRealTimeTrackerSelectorBinding

    private fun setupObservers() {
        eventsFragmentViewModel.eventType.observe(this) { eventTypeName ->
            (AppClass.instance.getCurrentActivity() as MainActivity).setTitleBarTitle(
                requireContext().getEventTypeName(
                    eventTypeName!!
                )
            )
            binding.avatarImage.setImageDrawable(requireContext().getEventTypeDrawable(eventTypeName))
        }
    }


    private fun cancelObservers() {
        eventsFragmentViewModel.eventType.removeObservers(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            FragmentEventRealTimeTrackerSelectorBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments
        setupUI()
    }


    private fun setupUI() {
        binding.buttonFixedLocation.setOnClickListener {
            eventsFragmentViewModel.setEventLocationMode(EventLocationType.FIXED)
            arguments?.putString("event_location_type", EventLocationType.FIXED.toString())
            requireContext().handleTouch()
            callback.OnSwitchFragmentRequest(
                R.id.event_fragment_location_read_only_selector,
                arguments
            )
        }


        binding.buttonRealtimeLocation.setOnClickListener {
            eventsFragmentViewModel.setEventLocationMode(EventLocationType.REALTIME)
            arguments?.putString("event_location_type", EventLocationType.REALTIME.toString())
            arguments?.remove("location")
            requireContext().handleTouch()
            callback.OnSwitchFragmentRequest(
                R.id.event_fragment_aditional_media_selector,
                arguments
            )
        }
    }

    private fun updateUI() {

        /*
                val eventType = eventsFragmentViewModel.eventType.value.toString()
                mainActivityViewModel.setTitle(requireContext().getEventTypeName(eventType))
                binding.avatarImage.setImageDrawable(requireContext().getEventTypeDrawable(eventType))
        */
    }

    fun newInstance(
        thisEvent: Event,
        callback: OnPostFragmentInteractionCallback
    ): EventRealTimeTrackingFragment {
        val fragment =
            EventRealTimeTrackingFragment(callback, eventsFragmentViewModel, mainActivityViewModel)
        val args = Bundle()
        fragment.arguments = args
        return fragment
    }

    companion object;


    override fun onResume() {
        super.onResume()
        setupObservers()
    }

    override fun onPause() {
        super.onPause()
        cancelObservers()
    }

}