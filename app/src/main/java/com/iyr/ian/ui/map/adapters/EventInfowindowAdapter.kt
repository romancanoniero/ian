package com.iyr.ian.ui.map.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.iyr.ian.R
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.ui.map.MapSituationFragment
import de.hdodenhof.circleimageview.CircleImageView

class EventInfowindowAdapter(context: Context, val fragment: MapSituationFragment) : GoogleMap.InfoWindowAdapter {

    var mContext = context
    private var mWindow: View =
        (context as Activity).layoutInflater.inflate(R.layout.infowindow_event, null)

    private fun rendowWindowText(marker: Marker, view: View) {

        val userName = view.findViewById<TextView>(R.id.user_name)
        val eventDate = view.findViewById<TextView>(R.id.event_date)
        val eventTypeImage = view.findViewById<CircleImageView>(R.id.event_type_image)
        val eventNumber = view.findViewById<TextView>(R.id.event_number)
        val location = view.findViewById<TextView>(R.id.location)
        val viewersCount = view.findViewById<TextView>(R.id.viewers_count)
        val goingCount = view.findViewById<TextView>(R.id.going_count)
        val alreadyCalledCount = view.findViewById<TextView>(R.id.called_count)

        val event = fragment.currentEvent

        if (event != null) {
            userName.text = event.author?.display_name
            when (event.event_type) {
                EventTypesEnum.SEND_POLICE.name -> {
                    eventTypeImage.setImageResource(R.drawable.police_hat)
                }
                EventTypesEnum.SEND_FIREMAN.name -> {
                    eventTypeImage.setImageResource(R.drawable.fireman_helmet)
                }
                EventTypesEnum.ROBBER_ALERT.name -> {
                    eventTypeImage.setImageResource(R.drawable.thief_mask)
                }
                EventTypesEnum.PANIC_BUTTON.name -> {
                    eventTypeImage.setImageResource(R.drawable.sos_big)
                }


            }
            location.text = event.location?.formated_address
           if (event.viewers!=null)
               viewersCount.text = event.viewers!!.size.toString()
            else
               viewersCount.text = "0"
        }

    }

    override fun getInfoContents(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoWindow(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }
}