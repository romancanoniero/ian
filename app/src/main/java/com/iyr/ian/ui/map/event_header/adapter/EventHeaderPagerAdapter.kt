package com.iyr.ian.ui.map.event_header.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.iyr.ian.dao.models.Event
import com.iyr.ian.ui.map.MapSituationFragment
import com.iyr.ian.ui.map.event_header.EventHeaderFragment
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


interface EventHeaderCallback {
    fun showUsersParticipatingFragment() {}
    fun showUsersWhoCalledFragment() {}
    fun showUsersGoingFragment() {}
    fun onFragmentFromBottomClose() {}
    fun setCalledButtonLocationRB(location: IntArray)
}

class EventHeaderPagerAdapter(
    val parentFragment: MapSituationFragment,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val mapSituationFragmentViewModel: MapSituationFragmentViewModel
) :
    FragmentStateAdapter(fragmentManager, lifecycle), EventHeaderCallback {

     var fragments = ArrayList<EventHeaderFragment>()



    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    fun addEvent(event: Event) {
        fragments.add(EventHeaderFragment(this, event, mapSituationFragmentViewModel))
        notifyItemInserted(fragments.size - 1)
    }

    fun updateEvent(event: Event) {
        var fragmentIndex = -1
        fragments.forEach { fragment ->
            fragmentIndex++
            var eventID = fragment.getEventData().event_key
            if (event.event_key.compareTo(eventID) == 0) {
                return@forEach
            }

        }

        try {

            fragments.get(fragmentIndex).updateEventData(event)
            GlobalScope.launch(Dispatchers.Main) {
                this@EventHeaderPagerAdapter.notifyItemChanged(fragmentIndex)

            }

        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

fun getEventsKeys(): ArrayList<String> {
    var toReturn = ArrayList<String>()
    fragments.forEach { event ->
        toReturn.add(event.getEventData().event_key)
    }
    return toReturn
}


    fun setEventData(eventFull: Event) {
        fragments.forEach { fragment ->
            if (fragment.getEventData().event_key == eventFull.event_key) {
                fragment.setEventData(eventFull)
                if (fragment.isVisible) {
                    fragment.updateUI()
                }
                return@forEach
            }
        }

        notifyDataSetChanged()
    }



    fun isEventExists(eventKey: String): Boolean {
        var toReturn: Boolean = false
        fragments.forEach { event ->
            if (event.getEventData().event_key == eventKey) {
                toReturn = true
                return@forEach
            }
        }
        return toReturn
    }

    fun getEventByKey(eventKey: String): Event? {
        var toReturn: Event? = null
        fragments.forEach { event ->
            if (event.getEventData().event_key == eventKey) {
                toReturn = event.getEventData()
                return@forEach
            }
        }
        return toReturn
    }


    override fun showUsersParticipatingFragment() {
        parentFragment.showUsersParticipatingFragment()
    }

    override fun showUsersWhoCalledFragment() {
        parentFragment.showUsersWhoCalledFragment()
    }

    override fun showUsersGoingFragment() {
        parentFragment.showUsersGoingFragment()
    }

    override fun setCalledButtonLocationRB(location: IntArray) {
        parentFragment.setCalledButtonLocationRB(location)
    }
}