package com.iyr.ian.ui.map.adapters

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.databinding.ItemEventHeaderAdapterBinding
import com.iyr.ian.enums.EventStatusEnum
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.getEventTypeDrawable
import com.iyr.ian.utils.getEventTypeName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class EventHeaderAdapter(val activity: Activity, val callback: EventHeaderCallback) :
    RecyclerView.Adapter<EventHeaderAdapter.EventViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()

    var events: java.util.ArrayList<EventFollowed> = ArrayList<EventFollowed>()
    var viewers: java.util.ArrayList<EventFollower> = ArrayList<EventFollower>()
    private var resultsFromSearches: ArrayList<Contact> = ArrayList<Contact>()
    private var compoundList: java.util.ArrayList<Contact> = ArrayList<Contact>()

    var binding: ItemEventHeaderAdapterBinding? = null

    init {

    }


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): EventViewHolder {
        binding = ItemEventHeaderAdapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding!!, activity)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    var currentEvent: EventFollowed? = null
    private var eventData: Event? = null

    private var eventRepositoryImpl: EventRepositoryImpl = EventRepositoryImpl()
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        eventData?.let { event ->
            holder.bind(event, viewers.toTypedArray())
        }

        enableButtons()


        //-------------------------------------------------------
        /*

                var viewersCount = 0
                var goingCount = 0
                var calledAuthoritiesCount = 0

                Toast.makeText(activity, "Actualizando contadores - Cant. de  Seguidores ${followersList.size}", Toast.LENGTH_SHORT).show()


                followersList.forEach { follower ->
                    if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                        viewersCount++

                        if (follower.going_time != null) {
                            goingCount++
                        }
                        if (follower.call_time != null) {
                            calledAuthoritiesCount++
                        }
                    }
                }
                if (viewersCount == 0) {
                    binding?.viewersCount?.setTextColor(activity.getColor(R.color.light_gray))
                    binding?.viewersCount?.iconTint =
                        ColorStateList.valueOf(activity.getColor(R.color.light_gray))
                    binding?.viewersCount?.setBackgroundColor(activity.getColor(R.color.material_red300))
                    binding?.viewersCount?.text = activity.resources.getString(R.string.nobody)
                    binding?.viewersCount?.setOnClickListener(null)
                } else
                {
                    binding?.viewersCount?.setTextColor(activity.getColor(R.color.white))
                    binding?.viewersCount?.background =
                        activity.getDrawable(R.drawable.primary_button_border)

                    binding?.viewersCount?.text = viewersCount.toString()
                    /*
                    binding?.viewersCount?.setOnClickListener {
                        callback.showUsersParticipatingFragment()
                    }

                     */

                }
                if (goingCount == 0 && (eventData?.author?.author_key != FirebaseAuth.getInstance().uid.toString() && eventData?.event_location_type?.compareTo(
                        EventLocationType.REALTIME.name
                    ) == 0)
                ) {
                    binding?.goingCount?.setTextColor(activity.getColor(R.color.light_gray))
                    binding?.goingCount?.iconTint =
                        ColorStateList.valueOf(activity.getColor(R.color.light_gray))
                    binding?.goingCount?.setBackgroundColor(activity.getColor(R.color.material_red300))
                    binding?.goingCount?.text = activity.resources.getString(R.string.nobody)
                    binding?.goingCount?.setOnClickListener(null)
                } else
                {
                    binding?.goingCount?.setTextColor(activity.getColor(R.color.white))
                    binding?.goingCount?.iconTint =
                        ColorStateList.valueOf(activity.getColor(R.color.white))
                    binding?.goingCount?.background =
                        activity.getDrawable(R.drawable.primary_button_border)
                    binding?.goingCount?.text = goingCount.toString()
                    /*
                       binding?.goingCount?.setOnClickListener {
                           callback.showUsersGoingFragment()
                       }

                     */
                }

                if (calledAuthoritiesCount == 0) binding?.calledCount?.text =
                    activity.resources.getString(R.string.nobody)
                else binding?.calledCount?.text = calledAuthoritiesCount.toString()

                var auxMe = EventFollower(FirebaseAuth.getInstance().uid.toString())
                Toast.makeText(activity, "Actualizando contadores - Cant. de  Seguidores ${followersList.size} ---- ${viewersCount} - ${goingCount} - ${calledAuthoritiesCount}", Toast.LENGTH_SHORT).show()

                *//*
                if (currentEvent != events[position]) {
                    currentEvent = events[position]

                    eventData?.let { event ->
        //                eventData = null
                        holder.bind(event)

                    }

                    /*
                    GlobalScope.launch(Dispatchers.IO) {
                        eventRepositoryImpl.listenEventFlow(currentEvent!!.event_key).collect {
                            when (it) {
                                is Resource.Success -> {
                                    it.data?.let { data ->
                                        eventData = data
                                        holder.bind(data)
                                    }

                                }

                                is Resource.Error -> {
                                    Log.d("ERROR", "Error al obtener el evento")
                                }

                                is Resource.Loading -> {
                                    Log.d("LOADING", "Cargando evento")
                                }
                            }
                        }


                    }

        */
                }
        */
    }


    fun setData(events: ArrayList<EventFollowed>) {
        this.events = events
    }

    fun setEventData(event: Event) {
        this.eventData = event
        var index = events.indexOfFirst { it.event_key == event.event_key }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    fun isEventExists(eventKey: String): Boolean {
        var toReturn: Boolean = false
        this.events.forEach { event ->
            if (event.event_key == eventKey) {
                toReturn = true
                return@forEach
            }
        }
        return toReturn
    }


    /**
     * Actualiza los contadores de la pantalla
     * @param followersList Lista de seguidores del evento
     */
    private fun updateFollowesCounter(
        followersList: Array<EventFollower>
    ) {
        var viewersCount = 0
        var goingCount = 0
        var calledAuthoritiesCount = 0

        followersList.forEach { follower ->
            if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                viewersCount++

                if (follower.going_time != null) {
                    goingCount++
                }
                if (follower.call_time != null) {
                    calledAuthoritiesCount++
                }
            }
        }
        if (viewersCount == 0) {
            binding?.viewersCount?.text = activity.resources.getString(R.string.nobody)
            //    binding?.viewersCount?.setOnClickListener(null)
        } else {
            binding?.viewersCount?.setTextColor(activity.getColor(R.color.white))
            binding?.viewersCount?.background =
                activity.getDrawable(R.drawable.primary_button_border)

            binding?.viewersCount?.text = viewersCount.toString()/*
            binding?.viewersCount?.setOnClickListener {
                callback.showUsersParticipatingFragment()
            }

             */

        }
        if (goingCount == 0 && (eventData?.author?.author_key != FirebaseAuth.getInstance().uid.toString() && eventData?.event_location_type?.compareTo(
                EventLocationType.REALTIME.name
            ) == 0)
        ) {
            binding?.goingCount?.setTextColor(activity.getColor(R.color.light_gray))
            binding?.goingCount?.iconTint =
                ColorStateList.valueOf(activity.getColor(R.color.light_gray))
            binding?.goingCount?.setBackgroundColor(activity.getColor(R.color.material_red300))
            binding?.goingCount?.text = activity.resources.getString(R.string.nobody)
            binding?.goingCount?.setOnClickListener(null)
        } else {
            binding?.goingCount?.setTextColor(activity.getColor(R.color.white))
            binding?.goingCount?.iconTint = ColorStateList.valueOf(activity.getColor(R.color.white))
            binding?.goingCount?.background = activity.getDrawable(R.drawable.primary_button_border)
            binding?.goingCount?.text = goingCount.toString()
        }

        if (calledAuthoritiesCount == 0) binding?.calledCount?.text =
            activity.resources.getString(R.string.nobody)
        else binding?.calledCount?.text = calledAuthoritiesCount.toString()

        var auxMe = EventFollower(FirebaseAuth.getInstance().uid.toString())
        //     Toast.makeText(activity, "Actualizando contadores - Cant. de  Seguidores ${followersList.size} ---- ${viewersCount} - ${goingCount} - ${calledAuthoritiesCount}", Toast.LENGTH_SHORT).show()

    }

    fun updateFollowerByEventKey(eventKey: String?, follower: EventFollower) {

        var index = viewers.indexOf(follower)
        if (index != -1) {
            viewers[index] = follower
        } else {
            viewers.add(follower)
        }

        updateFollowesCounter(viewers.toTypedArray())


    }

    fun disableButtons() {
        binding?.viewersCount?.setOnClickListener(null)
        binding?.goingCount?.setOnClickListener(null)
        binding?.calledCount?.setOnClickListener(null)
    }

    fun enableButtons() {
        binding?.viewersCount?.setOnClickListener {
            callback.showUsersParticipatingFragment()
        }
        binding?.goingCount?.setOnClickListener {
            callback.showUsersGoingFragment()
        }
        binding?.calledCount?.setOnClickListener {
            callback.showUsersWhoCalledFragment()
        }

    }

    class EventViewHolder(
        private val binding: ItemEventHeaderAdapterBinding, val activity: Activity
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event, followersList: Array<EventFollower>) {

            var fileName = event.author?.profile_image_path.toString()

            if (binding.userImage.tag != fileName) {
                GlobalScope.launch {

                    try {

                        Log.d("UPDATEUI", "Refresco la imagen")
                        var storageReferenceCache = FirebaseStorage.getInstance()
                            .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                            .child(event.author?.author_key!!).child(fileName)
                            .downloadUrlWithCache(activity)

                        withContext(Dispatchers.Main) {
                            if (binding.userImage.tag != storageReferenceCache) {
                                GlideApp.with(activity)

                                    .asBitmap()

                                    .load(storageReferenceCache)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(activity.getDrawable(R.drawable.ic_sand_clock))
                                    .error(activity.getDrawable(R.drawable.ic_error))
                                    .into(binding.userImage)
                            } else {
                                Log.d("UPDATEUI", "No refresco la imagen")
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("ERROR", "Error al cargar la imagen")
                    }
                    binding.userImage.tag = fileName
                }

            }
            binding.location.text = event.location?.formated_address

            val LocaleBylanguageTag: Locale =
                Locale.forLanguageTag(Locale.getDefault().toLanguageTag())
            val timeAgoLocale: TimeAgoMessages =
                TimeAgoMessages.Builder().withLocale(LocaleBylanguageTag).build()
            binding.timeMark.text = "( " + TimeAgo.using(event.time, timeAgoLocale) + " )"

            binding.userName.text = event.author!!.display_name

            binding.avatarImage.setImageDrawable(
                activity.getEventTypeDrawable(
                    event.event_type
                )
            )

            binding.eventType.text = activity.getEventTypeName(
                event.event_type
            )

            if (event.status == EventStatusEnum.DANGER.name || event.status == EventStatusEnum.USER_IN_TROUBLE.name) {
                binding.eventType.setTextColor(activity.getColor(R.color.red))
            } else {
                binding.eventType.setTextColor(activity.getColor(R.color.text_color))
            }
            binding.eventType.text = AppClass.instance.getEventStatus(event.status)

            var viewersCount = 0
            var goingCount = 0
            var calledAuthoritiesCount = 0

            //         Toast.makeText(activity, "Actualizando contadores - Cant. de  Seguidores ${followersList.size}", Toast.LENGTH_SHORT).show()


            followersList.forEach { follower ->
                if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    viewersCount++

                    if (follower.going_time != null) {
                        goingCount++
                    }
                    if (follower.call_time != null) {
                        calledAuthoritiesCount++
                    }
                }
            }/*
                        if (viewersCount == 0) {
                            binding?.viewersCount?.setTextColor(activity.getColor(R.color.light_gray))
                            binding?.viewersCount?.iconTint =
                                ColorStateList.valueOf(activity.getColor(R.color.light_gray))
                            binding?.viewersCount?.setBackgroundColor(activity.getColor(R.color.material_red300))
                            binding?.viewersCount?.text = activity.resources.getString(R.string.nobody)
                            binding?.viewersCount?.setOnClickListener(null)
                        } else {
                            binding?.viewersCount?.setTextColor(activity.getColor(R.color.white))
                            binding?.viewersCount?.background =
                                activity.getDrawable(R.drawable.primary_button_border)

                            binding?.viewersCount?.text = viewersCount.toString()
                        }
                        */
            if (goingCount == 0 && (event?.author?.author_key != FirebaseAuth.getInstance().uid.toString() && event?.event_location_type?.compareTo(
                    EventLocationType.REALTIME.name
                ) == 0)
            ) {
                binding?.goingCount?.setTextColor(activity.getColor(R.color.light_gray))
                binding?.goingCount?.iconTint =
                    ColorStateList.valueOf(activity.getColor(R.color.light_gray))
                binding?.goingCount?.setBackgroundColor(activity.getColor(R.color.material_red300))
                binding?.goingCount?.text = activity.resources.getString(R.string.nobody)
                binding?.goingCount?.setOnClickListener(null)
            } else {
                binding?.goingCount?.setTextColor(activity.getColor(R.color.white))
                binding?.goingCount?.iconTint =
                    ColorStateList.valueOf(activity.getColor(R.color.white))
                binding?.goingCount?.background =
                    activity.getDrawable(R.drawable.primary_button_border)
                binding?.goingCount?.text = goingCount.toString()
            }

            if (calledAuthoritiesCount == 0) binding?.calledCount?.text =
                activity.resources.getString(R.string.nobody)
            else binding?.calledCount?.text = calledAuthoritiesCount.toString()

            var auxMe = EventFollower(FirebaseAuth.getInstance().uid.toString())
            //          Toast.makeText(activity, "Actualizando contadores - Cant. de  Seguidores ${followersList.size} ---- ${viewersCount} - ${goingCount} - ${calledAuthoritiesCount}", Toast.LENGTH_SHORT).show()


            //       updateFollowesCounter(viewers)


            if (ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constants.LOCATION_PERMISSION_REQUEST_CODE
                )
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

        }


    }

}