package com.iyr.ian.ui.map.adapters

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.iyr.ian.AppConstants
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.databinding.ItemEventHeaderAdapterBinding
import com.iyr.ian.enums.EventStatusEnum
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.utils.assignFileImageTo
import com.iyr.ian.utils.geo.GeoFunctions
import com.iyr.ian.utils.getEventTypeDrawable
import com.iyr.ian.utils.getEventTypeName
import com.iyr.ian.utils.openNavigatorTo
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale


class EventHeaderAdapter(
    val activity: AppCompatActivity,
    val _mapRef: GoogleMap?,
    val callback: EventHeaderCallback
) :
    RecyclerView.Adapter<EventHeaderAdapter.EventViewHolder>() {

    private var mapRef: GoogleMap? = null
    var events: java.util.ArrayList<EventFollowed> = ArrayList()
    var viewers: java.util.ArrayList<EventFollower> = ArrayList()

    private lateinit var binding: ItemEventHeaderAdapterBinding

    init {
        mapRef = _mapRef
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): EventViewHolder {


        binding = ItemEventHeaderAdapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding, activity)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    var currentEvent: EventFollowed? = null
    private var eventData: Event? = null
    private var eventLocationRef: LatLng? = null

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        eventData?.let { event ->
            val viewModel = MapSituationFragmentViewModel.getInstance()
            holder.bind(event)

            viewModel.eventFlow.observe(activity) { resource ->
                val event = resource.data

                if (event?.event_location_type == EventLocationType.FIXED.name) {
                    eventLocationRef =
                        LatLng(event.location?.latitude!!, event.location?.longitude!!)
holder.eventLocationRef = eventLocationRef
                }
            }

            viewModel.followers.observe(activity) { followers ->
                viewers = followers
                updateFollowesCounter(viewers.toTypedArray())

                //-- Actualizo el nivel de bateria
                binding.batteryStatusSection.visibility = View.INVISIBLE
                val authorInfo = followers.find { it.is_author }
                authorInfo?.let { info ->
                    if (authorInfo.battery_percentage != 0.0) {
                        // En algún lugar de tu código donde necesites actualizar el progreso
                        updateProgressBarColor(
                            binding.progressBattery,
                            info.battery_percentage.toInt()
                        ) // Ejemplo con un progreso del 30%
                        binding.batteryStatusSection.visibility = VISIBLE
                    }
                    if (event?.event_location_type == EventLocationType.REALTIME.name) {
                        eventLocationRef = LatLng(authorInfo.l[0], authorInfo.l[0])
                        holder.eventLocationRef = eventLocationRef
                    }
                }
                val meAsFollowerInfo = followers.find {
                    it.user_key == UserViewModel.getInstance().getUser()?.user_key
                }
                eventLocationRef?.let { eventLocation ->
                    meAsFollowerInfo?.let { thisUser ->
                        updateDistanceToEvent(
                            LatLng(thisUser.l[0], thisUser.l[1]),
                            eventLocation

                        )

                    }
                }
            }
            /*
            viewModel.followers.value?.let { followers ->
                viewers = followers
                updateFollowesCounter(viewers.toTypedArray())
            }
            */
        }
        enableButtons()
    }

    private fun updateDistanceToEvent(latLng: LatLng, eventRef: LatLng) {
        if (mapRef != null) {
            binding.distanceFromYou.visibility = View.VISIBLE

            mapRef?.let { map ->

                val distance = GeoFunctions.getDistanceTo(
                    eventLocationRef!!,
                    latLng
                )
                binding.distanceFromYou.text = GeoFunctions.formatDistance(distance)
            }
        } else {
            binding.distanceFromYou.visibility = View.GONE
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        MapSituationFragmentViewModel.getInstance().followers.removeObservers(activity)
    }

    fun setData(events: ArrayList<EventFollowed>) {
        this.events = events
    }

    fun setEventData(event: Event) {
        this.eventData = event
        val index = events.indexOfFirst { it.event_key == event.event_key }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    fun isEventExists(eventKey: String): Boolean {
        var toReturn = false
        this.events.forEach { event ->
            if (event.event_key == eventKey) {
                toReturn = true
                return@forEach
            }
        }
        return toReturn
    }

    private fun updateProgressBarColor(progressBar: ProgressBar, progress: Int) {
        val drawable = progressBar.progressDrawable as LayerDrawable
        val progressDrawable =
            drawable.findDrawableByLayerId(android.R.id.progress) as ClipDrawable

        when {
            progress > 50 -> {
                val colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                progressDrawable.drawable?.colorFilter = colorFilter
//                progressDrawable.drawable?.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
            }

            progress in 15..50 -> {
                val ratio = (progress - 15) / 35.0f
                val red = (255 * (1 - ratio)).toInt()
                val yellow = (255 * ratio).toInt()
                val colorFilter =
                    PorterDuffColorFilter(Color.rgb(red, yellow, 0), PorterDuff.Mode.SRC_IN)
                progressDrawable.drawable?.colorFilter = colorFilter

                /* progressDrawable.drawable?.setColorFilter(

                     Color.rgb(red, yellow, 0),
                     PorterDuff.Mode.SRC_IN
                 )*/
            }

            else -> {
                val colorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                progressDrawable.drawable?.colorFilter = colorFilter
            }
        }
        progressBar.progress = progress
    }

    /**
     * Actualiza los contadores de la pantalla
     * @param followersList Lista de seguidores del evento
     */

    private fun updateFollowesCounter(followersList: Array<EventFollower>) {
        val myUserKey = UserViewModel.getInstance().getUser()?.user_key
        var viewersCount = followersList.count { it.user_key != myUserKey && it.is_author == false }
        val goingCount = followersList.count { it.going_time != null && it.user_key != myUserKey }
        val calledAuthoritiesCount =
            followersList.count { it.call_time != null && it.user_key != myUserKey }

        val thisUser = followersList.find { it.user_key == myUserKey }


        val buttonsVisibles = (SessionForProfile.getInstance(activity)
            .getProfileProperty("map_fragment_button_expanded") ?: false) as Boolean
        expandCompressActionButtons(buttonsVisibles)

        binding.expandCollaps.setOnClickListener {
            val isExpanded = binding.expandCollaps.rotation != 180f
            SessionForProfile.getInstance(activity)
                .setProfileProperty("map_fragment_button_expanded", isExpanded)

            expandCompressActionButtons(isExpanded)
        }

        binding.viewersCount.apply {
            val caption = StringBuilder()
            when (thisUser?.following_start_time) {
                null -> {
                }

                else -> {
                    if (thisUser.is_author == false) {
                        caption.append(activity.resources.getString(R.string.you))
                    }
                }
            }

            when (viewersCount) {
                0 -> {
                    if (thisUser?.following_start_time == null) {
                        caption.append(activity.resources.getString(R.string.nobody))
                    }
                }

                else -> {
                    caption.append(viewersCount.toString())
                }
            }
            text = caption.toString()


        }

        binding.goingCount.apply {

            val caption = StringBuilder()
            when (thisUser?.going_time) {
                null -> {
                }

                else -> {
                    caption.append(activity.resources.getString(R.string.you))
                }
            }
            when (goingCount) {
                0 -> {
                    if (thisUser?.going_time == null) {
                        caption.append(activity.resources.getString(R.string.nobody))
                    }
                }

                else -> {
                    if (thisUser?.going_time != null) {
                        caption.append(" ")
                        caption.append(activity.resources.getString(R.string.and))
                    }
                    caption.append(" ")

                    caption.append(goingCount.toString())
                    if (thisUser?.going_time != null) {
                        caption.append(" ")
                        caption.append(activity.resources.getString(R.string.more))
                    }
                }
            }
            text = caption.toString()
        }

        binding.calledCount.apply {

            val caption = StringBuilder()
            when (thisUser?.call_time) {
                null -> {
                }

                else -> {
                    caption.append(activity.resources.getString(R.string.you))
                }
            }
            when (calledAuthoritiesCount) {
                0 -> {
                    if (thisUser?.call_time == null) {
                        caption.append(activity.resources.getString(R.string.nobody))
                    }
                }

                else -> {
                    if (thisUser?.call_time != null) {
                        caption.append(" ")
                        caption.append(activity.resources.getString(R.string.and))
                    }
                    caption.append(" ")

                    caption.append(goingCount.toString())
                    if (thisUser?.call_time != null) {
                        caption.append(" ")
                        caption.append(activity.resources.getString(R.string.more))
                    }
                }
            }
            text = caption.toString()
        }
    }

    private fun expandCompressActionButtons(isExpanded: Boolean) {
        if (isExpanded) {
            binding.expandCollaps.rotation = 180f
            binding.viewersCount.visibility = VISIBLE
            binding.goingCount.visibility = VISIBLE
            binding.calledCount.visibility = VISIBLE
        } else {
            binding.expandCollaps.rotation = 0f
            binding.viewersCount.visibility = View.GONE
            binding.goingCount.visibility = View.GONE
            binding.calledCount.visibility = View.GONE
        }
    }


    fun updateFollowerByEventKey(follower: EventFollower) {
        val index = viewers.indexOf(follower)
        if (index != -1) {
            viewers[index] = follower
        } else {
            viewers.add(follower)
        }
        if (::binding.isInitialized) {
            updateFollowesCounter(viewers.toTypedArray())
        }
    }

    fun disableButtons() {
        binding.viewersCount.setOnClickListener(null)
        binding.goingCount.setOnClickListener(null)
        binding.calledCount.setOnClickListener(null)
    }

    fun enableButtons() {
        binding.viewersCount.setOnClickListener {
            callback.showUsersParticipatingFragment()
        }
        binding.goingCount.setOnClickListener {
            callback.showUsersGoingFragment()
        }
        binding.calledCount.setOnClickListener {
            callback.showUsersWhoCalledFragment()
        }

    }

    fun setMapRef(map: GoogleMap) {
        this.mapRef = map
    }

    class EventViewHolder(
        private val binding: ItemEventHeaderAdapterBinding, val activity: Activity
    ) : RecyclerView.ViewHolder(binding.root) {

        var eventLocationRef: LatLng? = null
            set(value) {
                field = value
            }
            get() {
                return field
            }

        fun bind(event: Event) {

            val fileName = event.author?.profile_image_path.toString()

            binding.openRouteButton.setOnClickListener {
                eventLocationRef?.let { eventLocation ->
                    activity.openNavigatorTo(eventLocation)
                }
            }

            if (binding.userImage.tag != fileName) {
                /*
                              GlobalScope.launch {

                                  try {

                                      Log.d("UPDATEUI", "Refresco la imagen")

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


                              }
              */
                binding.userImage.visibility = View.INVISIBLE

                GlobalScope.launch(Dispatchers.Main) {

                    activity.assignFileImageTo(
                        fileName,
                        "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${event.author?.author_key!!}",
                        binding.userImage
                    )
                }
                binding.userImage.tag = fileName
            }



            binding.location.text = event.location?.formated_address

            val localeBylanguageTag: Locale =
                Locale.forLanguageTag(Locale.getDefault().toLanguageTag())
            val timeAgoLocale: TimeAgoMessages =
                TimeAgoMessages.Builder().withLocale(localeBylanguageTag).build()
            binding.timeMark.text = String.format("(%s)", TimeAgo.using(event.time, timeAgoLocale))

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
            binding.eventStatus.text = AppClass.instance.getEventStatus(event.status)

            /*
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

            if (goingCount == 0 && (event.author?.author_key != FirebaseAuth.getInstance().uid.toString() && event.event_location_type.compareTo(
                    EventLocationType.REALTIME.name
                ) == 0)
            ) {
                binding.goingCount.setTextColor(activity.getColor(R.color.light_gray))
                binding.goingCount.iconTint =
                    ColorStateList.valueOf(activity.getColor(R.color.light_gray))
                binding.goingCount.setBackgroundColor(activity.getColor(R.color.material_red300))
                binding.goingCount.text = activity.resources.getString(R.string.nobody)
                binding.goingCount.setOnClickListener(null)
            } else {
                binding.goingCount.setTextColor(activity.getColor(R.color.white))
                binding.goingCount.iconTint =
                    ColorStateList.valueOf(activity.getColor(R.color.white))
                binding.goingCount.background =
                    AppCompatResources.getDrawable(activity, R.drawable.primary_button_border)
                binding.goingCount.text = goingCount.toString()
            }

            if (calledAuthoritiesCount == 0) binding.calledCount.text =
                activity.resources.getString(R.string.nobody)
            else binding.calledCount.text = calledAuthoritiesCount.toString()
*/
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