package com.iyr.ian.ui.events.fragments

//@file:Suppress("ControlFlowWithEmptyBody")

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.models.EventType
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.dialogs.DialogFunctionEnum
import com.iyr.ian.ui.events.OnPostFragmentInteractionCallback
import com.iyr.ian.ui.events.fragments.adapters.EventTypeAdapter
import com.iyr.ian.ui.events.fragments.adapters.EventTypeSelectorCallback
import com.iyr.ian.utils.LocationRequirementsCallback
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.requestLocationRequirements
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.EventsFragmentViewModel
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EventTypeSelectorFragment() :
    Fragment(),
    EventTypeSelectorCallback {

    private var accessLevel: Int = AccessLevelsEnum.FREE.ordinal
    lateinit var viewModel: EventsFragmentViewModel
    private var eventTypeAdapter: EventTypeAdapter? = null
    private var callback: OnPostFragmentInteractionCallback? = null

    private var recyclerEventTypes: RecyclerView? = null


    fun newInstance(): EventTypeSelectorFragment {
//        return EventTypeSelectorFragment(parentFragment, viewModel, mainActivityViewModel)
        return EventTypeSelectorFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventTypeAdapter = EventTypeAdapter(requireActivity(), this as EventTypeSelectorCallback)

        viewModel = EventsFragmentViewModel.getInstance(requireContext())
        initializeEventTypesAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerEventTypes = view.findViewById(R.id.recycler_event_types)
        setupUI()
    }

    private fun setupUI() {
        recyclerEventTypes?.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerEventTypes?.adapter = eventTypeAdapter

        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val itemWidth = (screenWidth * .348837)
        val spacing = (screenWidth - (itemWidth * 2)) / 3
        recyclerEventTypes?.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                spacing.toInt(),
                true
            )
        )
    }


    private fun initializeEventTypesAdapter() {
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.SEND_POLICE.name,
                R.drawable.poli,
                R.string.notify_the,
                R.string.police
            )
        )
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.SEND_AMBULANCE.name,
                R.drawable.ambulance_big,
                R.string.notify_the,
                R.string.ambulance
            )
        )
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.SEND_FIREMAN.name,
                R.drawable.fireman_big,
                R.string.notify_the,
                R.string.firemans
            )
        )
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.ROBBER_ALERT.name,
                R.drawable.suspicius_big,
                R.string.notify,
                R.string.robbery
            )
        )
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.SCORT_ME.name,
                R.drawable.scortme_big,
                R.string.ask_for,
                R.string.scorting
            )
        )
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.KID_LOST.name,
                R.drawable.kid_lot_big,
                R.string.notify,
                R.string.kid_lost
            )
        )
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.PET_LOST.name,
                R.drawable.pet_lost_big,
                R.string.notify,
                R.string.pet_lost
            )
        )
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.PERSECUTION.name,
                R.drawable.persecution_big,
                R.string.notify,
                R.string.persecution
            )
        )
        eventTypeAdapter?.getData()?.add(
            EventType(
                EventTypesEnum.MECANICAL_AID.name,
                R.drawable.mecanical_aid_big,
                R.string.ask_for,
                R.string.mechanical_aid
            )
        )
    }

    override fun onResume() {
        super.onResume()
        val appToolbar = (requireActivity() as MainActivity).appToolbar
        appToolbar.enableBackBtn(true)
        appToolbar.updateTitle(getString(R.string.event_publish_type_selector))

        val bottomToolBar = (requireActivity() as MainActivity).binding.bottomToolbar
        bottomToolBar.visibility = View.GONE
        resetCurrentEvent()
        startObservers()
    }


    override fun onPause() {
        super.onPause()
        stopObservers()
    }


    private fun startObservers() {
        MainActivityViewModel.getInstance().subscriptionType.observe(viewLifecycleOwner, {
            if (it != null) {
                this@EventTypeSelectorFragment.accessLevel = it.access_level
            }
        })
    }


    private fun stopObservers() {
        MainActivityViewModel.getInstance().subscriptionType.removeObservers(viewLifecycleOwner)
    }


    private fun resetCurrentEvent() {
        callback?.resetCurrentEvent()
    }

    @JvmName("setCallback1")
    fun setCallback(callback: OnPostFragmentInteractionCallback) {
        this.callback = callback
    }


    override fun onEventTypeSelected(eventType: EventTypesEnum) {

        requireContext().handleTouch()

        val mainActivityViewModel = (requireActivity() as MainActivity).viewModel

        if (mainActivityViewModel.isInPanic.value == true) {
            requireActivity().showErrorDialog(
                getString(R.string.not_possible),
                getString(R.string.error_panic_event_is_running_close_first),
                getString(R.string.close),
                null
            )
        } else {


            if (accessLevel ?: 0 >= AccessLevelsEnum.SOLIDARY.ordinal) {

                viewModel.setEventType(eventType.toString())

                lifecycleScope.launch(Dispatchers.Main)
                {
                    requireActivity().requestLocationRequirements(object :
                        LocationRequirementsCallback {
                        override fun onRequirementsComplete() {
                            //  thisEvent!!.event_type = eventType.name
                            if (eventType == EventTypesEnum.KID_LOST
                                || eventType == EventTypesEnum.PET_LOST
                                || eventType == EventTypesEnum.SCORT_ME
                            ) {
                                viewModel.setEventLocationMode(EventLocationType.FIXED)
                                /*
                                                                callback!!.OnSwitchFragmentRequest(
                                                                    R.id.event_fragment_location_read_only_selector
                                                                )
                                  */
                                findNavController().navigate(R.id.event_fragment_location_read_only_selector)
                            } else {

                                if (AppClass.instance.isUserAddressStorageEnabled) {
                                    /*
                                    callback!!.OnSwitchFragmentRequest(
                                        R.id.event_fragment_location_selector,
                                        arguments
                                    )*/
                                    findNavController().navigate(R.id.event_fragment_location_selector)
                                } else {
                                    /*
                                    callback!!.OnSwitchFragmentRequest(
                                        R.id.event_fragment_realtime_selector, arguments
                                    )
*/

                                    val action =
                                        EventTypeSelectorFragmentDirections.actionEventTypeSelectorFragmentToEventRealTimeTrackingFragment()
                                    findNavController().navigate(action)

                                }
                            }
                        }
                    })

                }

            } else {
                val action =
                    EventTypeSelectorFragmentDirections.actionEventTypeSelectorFragmentToEventTypeExplanationDialog(
                        eventType,
                        DialogFunctionEnum.SUBSCRIPTION_REQUIRED
                    )
                findNavController().navigate(action)
            }


        }
    }
}