package com.iyr.ian.ui.map.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventVisibilityTypes
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.utils.px
import de.hdodenhof.circleimageview.CircleImageView


interface EventsTrackingCallback {

    fun onSwitchToEvent(eventKey: String)
}

class EventsActiveAdapter(val con: Context) :
    RecyclerView.Adapter<EventsActiveAdapter.UserViewHolder>(), Filterable {

    // private val viewBinderHelper = ViewBinderHelper()
    private var mContext: Context = con
    private var mList: java.util.ArrayList<EventFollowed> = ArrayList<EventFollowed>()
    private var eventsFilterList: ArrayList<EventFollowed> = ArrayList<EventFollowed>()


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_events_active_adapter, parent, false)
    )

    override fun getItemCount() = eventsFilterList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val record: EventFollowed = eventsFilterList[position]
        if (record.author.author_key != FirebaseAuth.getInstance().uid) {
            holder.userName.text = record.author.display_name
            holder.userName.visibility = View.VISIBLE
        } else
            holder.userName.visibility = View.INVISIBLE



        // TODO: Pasarlo a corutina
        val storageReference = FirebaseStorage.getInstance()
            .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
            .child(record.author.author_key)
        //     .getReference(record.user!!.image.file_name)

        try {

Log.d("GLIDEAPP","1")
            GlideApp.with(con)
                .asBitmap()
                .load(storageReference)
                .placeholder(con.getDrawable(R.drawable.progress_animation))
                .error(con.getDrawable(R.drawable.ic_error))
                .into(holder.userImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }





        if (record.selected) {
            holder.userImage.borderWidth = 3.px
            holder.userImage.borderColor = con.getColor(android.R.color.holo_red_dark)
            val face = ResourcesCompat.getFont(con, R.font.muli_black)
            holder.userName.typeface = face
        } else {
            holder.userImage.borderWidth = 2.px
            holder.userImage.borderColor = con.getColor(R.color.darkGray)
            val face = ResourcesCompat.getFont(con, R.font.muli)
            holder.userName.typeface = face
        }


        var eventIconRes = -1
        when (record.event_type) {
            EventTypesEnum.SEND_POLICE.name -> {
                eventIconRes = R.drawable.ic_police_small
            }
            EventTypesEnum.SEND_FIREMAN.name -> {
                eventIconRes = R.drawable.ic_firefighter_small
            }
            EventTypesEnum.ROBBER_ALERT.name -> {
                eventIconRes = R.drawable.ic_robbery_small
            }
            EventTypesEnum.SEND_AMBULANCE.name -> {
                eventIconRes = R.drawable.ic_doctor_small
            }
            EventTypesEnum.PERSECUTION.name -> {
                eventIconRes = R.drawable.ic_persecution
                holder.eventTypeIcon.circleBackgroundColor = mContext.getColor(R.color.darkGray)
            }
            EventTypesEnum.SCORT_ME.name -> {
                eventIconRes = R.drawable.ic_follow_small
            }
            EventTypesEnum.KID_LOST.name -> {
                eventIconRes = R.drawable.ic_kid_lost_small
            }
            EventTypesEnum.PET_LOST.name -> {
                eventIconRes = R.drawable.ic_pet_lost_small
            }

            EventTypesEnum.PANIC_BUTTON.name -> {
                eventIconRes = R.drawable.sos_big
            }


            EventTypesEnum.FALLING_ALARM.name -> {
                eventIconRes = R.drawable.ic_falling
            }
        }
        Glide.with(con)
            .asBitmap()
            .load(eventIconRes)
            .into(holder.eventTypeIcon)

        holder.layout.setOnClickListener {
            (con as EventsTrackingCallback).onSwitchToEvent(record.event_key)
        }

    }

    fun getData(): java.util.ArrayList<EventFollowed> {
        return mList
    }

    fun setData(events: ArrayList<EventFollowed>) {
        mList = events
        filter.filter("")
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var layout: View = view.findViewById<View>(R.id.layout)
        var userImage: CircleImageView = view.findViewById<CircleImageView>(R.id.user_image)
        var userName: TextView = view.findViewById<TextView>(R.id.user_name)
        var eventTypeIcon: CircleImageView =
            view.findViewById<CircleImageView>(R.id.event_type_icon)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                eventsFilterList = ArrayList<EventFollowed>()
                for (row in mList) {
                    if (row.visibility == EventVisibilityTypes.VISIBLE.name ||
                        (row.visibility == EventVisibilityTypes.HIDDEN_FOR_AUTHOR.name && row.author.author_key != FirebaseAuth.getInstance().uid.toString())
                    ) {
                        eventsFilterList.add(row)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = eventsFilterList
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {


                eventsFilterList = results?.values as ArrayList<EventFollowed>
                notifyDataSetChanged()
            }
        }
    }
}