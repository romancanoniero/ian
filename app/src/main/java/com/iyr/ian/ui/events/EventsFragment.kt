package com.iyr.ian.ui.events


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.events.fragments.EventAdditionalMediaFragment
import com.iyr.ian.ui.interfaces.EventsPublishingCallback
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.viewmodels.EventsFragmentViewModel
import com.iyr.ian.viewmodels.MainActivityViewModel


interface OnPostFragmentInteractionCallback {
  //  fun OnSwitchFragmentRequest(fragmentId: Int, arguments: Bundle? = null)
    fun resetCurrentEvent()
    fun onPublishEventDone(event: Event?)
}

class EventsFragment(val callback: EventsPublishingCallback? = null, val mainActivityViewModel: MainActivityViewModel ? = null) : Fragment(),
    OnPostFragmentInteractionCallback {


    private val viewModel: EventsFragmentViewModel by lazy { EventsFragmentViewModel.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel.initializeEvent(SessionForProfile.getInstance(requireContext()).getUserId())
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetCurrentEvent()

        showButtons()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setTitleBarTitle(R.string.action_publish_event)
        AppClass.instance.setCurrentFragment(this)
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.action_publish_event))
        }
        startObservers()
    }



    override fun onPause() {
        super.onPause()
        stopObservers()
    }


    private fun startObservers() {

    }

    private fun stopObservers() {

    }

    private fun showButtons() {

    }



    override fun resetCurrentEvent() {
        viewModel.initializeEvent(SessionForProfile.getInstance(requireContext()).getUserId())
    }


    override fun onPublishEventDone(event: Event?) {
        callback?.onPublishEventDone(event)
    }

    companion object;

    fun getMediaFragment(): EventAdditionalMediaFragment {
        return EventAdditionalMediaFragment()
    }


    fun getEvent(): Event {
        return viewModel.event.value!!
    }
}