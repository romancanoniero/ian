package com.iyr.ian.ui.map.event_header

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import com.iyr.ian.AppConstants
import com.iyr.ian.Constants.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.callbacks.ViewersActionsCallback
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.databinding.FragmentBottomSheetEventHeaderBinding
import com.iyr.ian.enums.EventStatusEnum
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.activity_contracts.MakePhoneCallActivityContract
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.formatDateTime
import com.iyr.ian.utils.geo.GeoFunctions
import com.iyr.ian.utils.getEventTypeDrawable
import com.iyr.ian.utils.getEventTypeName
import com.iyr.ian.utils.makeAPhoneCall
import com.iyr.ian.utils.permissionsForVoiceCall
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale


class EventHeaderFragment(
    val callback: EventHeaderCallback,
    event: Event,
    val mapSituationFragmentViewModel: MapSituationFragmentViewModel
) : Fragment() {


    private var layoutDrawn = false
    private var oldEventData: Event? = null
    private lateinit var eventData: Event

    var calledButtonLoc = IntArray(2)

    private val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            Log.d("REFRESH", "Entro al global layout")

            // Tomo los datos de la pantalla total.
            var r = Rect()
            binding.root.getLocalVisibleRect(r)

            binding.calledCount.getLocationOnScreen(calledButtonLoc)
            var calledButtonTop = calledButtonLoc[1]

            var location = IntArray(2)
            location[0] = calledButtonLoc[0] + binding.calledCount.width
            location[1] = calledButtonLoc[1] + (binding.calledCount.height * 1.5).toInt()
            callback.setCalledButtonLocationRB(location)
            binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

            layoutDrawn = true
        }
    }


    private val fetchDataFromPhoneCallActivity =
        registerForActivityResult(MakePhoneCallActivityContract()) { data ->
            // data is the processed result from SecondActivityContract
            var pp = 3
        }
    private lateinit var binding: FragmentBottomSheetEventHeaderBinding

    //    private lateinit var adapter: FriendsAdapter
    //   private var usersSearchAdapter: CustomCompleteTextViewAdapter? = null

    fun EventHeaderFragment() {}

    init {
        eventData = event
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetEventHeaderBinding.inflate(layoutInflater)
        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {


        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            callback: EventHeaderCallback,
            event: Event,
            mapSituationFragmentViewModel: MapSituationFragmentViewModel,
            param1: String,
            param2: String
        ) = EventHeaderFragment(callback, event, mapSituationFragmentViewModel).apply {}
    }


    override fun onResume() {
        super.onResume()
        updateUI()
        setupObservers()

    }

    var jobEventFlowObserver: Job? = null
    var jobViewersFlowObserver: Job? = null
    var viewersList = ArrayList<EventFollower>()
    private fun setupObservers() {

        jobEventFlowObserver = lifecycleScope.launch {
            mapSituationFragmentViewModel.eventFlow.observe(viewLifecycleOwner, { resource ->
                when (resource) {
                    is Resource.Error -> {
                        // Implementar
                    }

                    is Resource.Loading -> {
                        // Implementar
                    }

                    is Resource.Success -> {

                        var eventData = resource.data!!

                        binding.creationTime.text = eventData.time.formatDateTime()


                        if (ActivityCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                LOCATION_PERMISSION_REQUEST_CODE
                            )
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                        } else {

                            /*
                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        SmartLocation.with(requireContext()).location().oneFix()
                                                            .start { location ->
                                                                if (location != null) {
                                                                    var myPosition =
                                                                        LatLng(location.latitude, location.longitude)
                                                                    if (eventData.event_location_type == EventLocationType.FIXED.toString()) {
                                                                        var distance = GeoFunctions.getDistanceBetweenTwoPoints(
                                                                            eventData.location!!.latitude!!,
                                                                            eventData.location!!.longitude!!,
                                                                            location.latitude,
                                                                            location.longitude
                                                                        )
                                                                        binding.distanceFromYou.text =
                                                                            GeoFunctions.formatDistance(distance)
                                                                    } else {
                                                                        mapSituationFragmentViewModel.followers.value?.forEach { follower ->
                                                                            if (follower.user_key == eventData.author_key) {
                                                                                var distance =
                                                                                    GeoFunctions.getDistanceBetweenTwoPoints(
                                                                                        follower.l[0],
                                                                                        follower.l[1],
                                                                                        location.latitude,
                                                                                        location.longitude
                                                                                    )
                                                                                binding.distanceFromYou.text =
                                                                                    GeoFunctions.formatDistance(distance)
                                                                                return@forEach
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                binding.distanceToEventLayout.visibility = View.VISIBLE
                                                            }
                                                    }
                            */

                        }

                        var fileName = eventData.author?.profile_image_path.toString()
                        if (binding.userImage.tag != fileName) {
                            lifecycleScope.launch {


                                var storageReferenceCache =
                                    (StorageRepositoryImpl().generateStorageReference("${AppConstants.PROFILE_IMAGES_STORAGE_PATH}${eventData.author_key}/${fileName}") as StorageReference)
                                        .downloadUrlWithCache(requireContext())

                                if (binding.userImage.tag != storageReferenceCache) {
                                    GlideApp.with(requireContext()).asBitmap()
                                        .load(storageReferenceCache)
                                        .placeholder(requireActivity().getDrawable(R.drawable.progress_animation))
                                        .error(requireActivity().getDrawable(R.drawable.ic_error))
                                        .into(binding.userImage)
                                } else {
                                    Log.d("UPDATEUI", "No refresco la imagen")
                                }

                            }
                            binding.userImage.tag = fileName
                        }


                        binding.location.text = eventData.location?.formated_address

                        val LocaleBylanguageTag: Locale =
                            Locale.forLanguageTag(Locale.getDefault().toLanguageTag())
                        val timeAgoLocale: TimeAgoMessages =
                            TimeAgoMessages.Builder().withLocale(LocaleBylanguageTag).build()
                        binding.timeMark.text =
                            "( " + TimeAgo.using(eventData.time, timeAgoLocale) + " )"

                        binding.userName.text = eventData.author!!.display_name

                        binding.avatarImage.setImageDrawable(
                            requireContext().getEventTypeDrawable(
                                eventData.event_type
                            )
                        )

                        binding.eventType.text = requireContext().getEventTypeName(
                            eventData.event_type
                        )

                        if (eventData.status == EventStatusEnum.DANGER.name || eventData.status == EventStatusEnum.USER_IN_TROUBLE.name) {
                            binding.eventType.setTextColor(requireContext().getColor(R.color.red))/*
                                            binding.EventTypesEnum.setShadowLayer(
                                                3f,
                                                -4f,
                                                -4f,
                                                requireContext().getColor(R.color.colorRed)
                                            )

                             */
                        } else {
                            binding.eventType.setTextColor(requireContext().getColor(R.color.text_color))
                        }
                        binding.eventType.text = AppClass.instance.getEventStatus(eventData.status)


                    }
                }
            })

        }


        jobViewersFlowObserver = lifecycleScope.launch {
            mapSituationFragmentViewModel.viewersUpdatesFlow.collect { resource ->
                viewersList.clear()
                when (resource) {
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                    }

                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        var pp = 3
                    }
                }
            }
        }

        /*
                mapSituationFragmentViewModel.followers.observe(viewLifecycleOwner) { followers ->
                    updateFollowesCounter(followers)
                }
        */
        AppClass.instance.lastLocation.observe(viewLifecycleOwner) { location ->

            if (location != null) {
                var myPosition = LatLng(location.latitude, location.longitude)
                var distance = 0.0f
                if (eventData.event_location_type == EventLocationType.FIXED.name) {
                    distance = GeoFunctions.getDistanceBetweenTwoPoints(
                        eventData.location!!.latitude!!,
                        eventData.location!!.longitude!!,
                        location.latitude,
                        location.longitude
                    )

                } else {

                    mapSituationFragmentViewModel.getFollower(eventData.author_key!!)
                        ?.let { follower ->
                            distance = GeoFunctions.getDistanceBetweenTwoPoints(
                                follower.l[0],
                                follower.l[1],
                                location.latitude,
                                location.longitude
                            )
                        }

                }
                binding.distanceFromYou.text = GeoFunctions.formatDistance(distance)
            } else {
                binding.distanceFromYou.text = getString(R.string.not_avalaible)
            }


        }


        jobViewersFlowObserver?.start()
    }

    /**
     * Actualiza los contadores de la pantalla
     * @param followersList Lista de seguidores del evento
     */
    private fun updateFollowesCounter(followersList: ArrayList<EventFollower>) {
        var viewersCount = 0
        var goingCount = 0
        var calledAuthoritiesCount = 0
        val event = mapSituationFragmentViewModel.thisEvent

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
            binding.viewersCount.setTextColor(requireContext().getColor(R.color.light_gray))
            binding.viewersCount.iconTint =
                ColorStateList.valueOf(requireContext().getColor(R.color.light_gray))
            binding.viewersCount.setBackgroundColor(requireContext().getColor(R.color.material_red300))
            binding.viewersCount.text = getString(R.string.nobody)
            binding.viewersCount.setOnClickListener(null)
        } else {
            binding.viewersCount.setTextColor(requireContext().getColor(R.color.white))
            binding.viewersCount.background =
                requireContext().getDrawable(R.drawable.primary_button_border)

            binding.viewersCount.text = viewersCount.toString()
            binding.viewersCount.setOnClickListener {
                callback.showUsersParticipatingFragment()
            }

        }
        if (goingCount == 0 && (event?.author?.author_key != FirebaseAuth.getInstance().uid.toString() && event?.event_location_type?.compareTo(
                EventLocationType.REALTIME.name
            ) == 0)

        ) {
            binding.goingCount.setTextColor(requireContext().getColor(R.color.light_gray))
            binding.goingCount.iconTint =
                ColorStateList.valueOf(requireContext().getColor(R.color.light_gray))
            binding.goingCount.setBackgroundColor(requireContext().getColor(R.color.material_red300))
            binding.goingCount.text = getString(R.string.nobody)
            binding.goingCount.setOnClickListener(null)

        } else {
            binding.goingCount.setTextColor(requireContext().getColor(R.color.white))
            binding.goingCount.iconTint =
                ColorStateList.valueOf(requireContext().getColor(R.color.white))
            binding.goingCount.background =
                requireContext().getDrawable(R.drawable.primary_button_border)
            binding.goingCount.text = goingCount.toString()
            binding.goingCount.setOnClickListener {
                callback.showUsersGoingFragment()
            }
        }

        if (calledAuthoritiesCount == 0) binding.calledCount.text = getString(R.string.nobody)
        else binding.calledCount.text = calledAuthoritiesCount.toString()


        var auxMe = EventFollower(FirebaseAuth.getInstance().uid.toString())
        var myIndex = mapSituationFragmentViewModel.followersList.indexOf(auxMe)
        if (myIndex != -1) {
            var meAsEventFollower = mapSituationFragmentViewModel.followersList[myIndex]

            if (meAsEventFollower.going_time != null) {
                binding.indicatorGoing.circleBackgroundColor =
                    requireContext().getColor(R.color.colorPrimary)
            } else {
                binding.indicatorGoing.circleBackgroundColor =
                    requireContext().getColor(R.color.light_gray)
            }


            binding.indicatorGoing.setOnClickListener {

                if (requireContext() is ViewersActionsCallback) {

                    val callback = object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {

                        }

                    }
                    (requireContext() as ViewersActionsCallback).onGoingStateChanged(
                        eventData.event_key, FirebaseAuth.getInstance().uid.toString(), callback
                    )
                    //     meAsViewer.going_time = !meAsViewer.going_time
                    updateUI()

                } else {
                    AppClass.instance.getCurrentActivity()
                        ?.showErrorDialog("No esta conectado a un callback")
                }
            }



            if (meAsEventFollower.call_time != null) {
                binding.indicatorAlreadyCalledAuthorities.circleBackgroundColor =
                    requireContext().getColor(R.color.quantum_googgreen500)
            } else {
                binding.indicatorAlreadyCalledAuthorities.circleBackgroundColor =
                    requireContext().getColor(R.color.light_gray)
            }
            binding.indicatorAlreadyCalledAuthorities.setOnClickListener {

                if (meAsEventFollower.call_time == null) {
                    if (requireActivity().permissionsForVoiceCall()) {
//this.phoneCallPending = true
                        //                   (this@EventHeaderFragment).fetchDataFromPhoneCallActivity.launch("911")
                        requireActivity().makeAPhoneCall("911")
                    }
                }
            }

        }


    }

    override fun onPause() {
        super.onPause()
        removeObservers()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {


        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener(
                    OnSuccessListener<Location> { location ->
                        if (location != null) {
                            var myPosition = LatLng(location.latitude, location.longitude)
                            var distance = 0.0f
                            if (eventData.event_location_type == EventLocationType.FIXED.name) {
                                distance = GeoFunctions.getDistanceBetweenTwoPoints(
                                    eventData.location!!.latitude!!,
                                    eventData.location!!.longitude!!,
                                    location.latitude,
                                    location.longitude
                                )

                            } else {

                                try {
                                    distance = GeoFunctions.getDistanceBetweenTwoPoints(
                                        eventData.location!!.latitude!!,
                                        eventData.location!!.longitude!!,
                                        location.latitude,
                                        location.longitude
                                    )

                                } catch (ex: Exception) {
                                    var pp = 33
                                }

                            }
                            binding.distanceFromYou.text = GeoFunctions.formatDistance(distance)
                        } else {
                            binding.distanceFromYou.text = getString(R.string.not_avalaible)
                        }


                    })


            }
        }
    }

    private fun removeObservers() {
        mapSituationFragmentViewModel.eventFlow.removeObservers(viewLifecycleOwner)


//        mapSituationFragmentViewModel.viewersUpdatesFlow.removeObservers(viewLifecycleOwner)

        jobViewersFlowObserver?.cancel()
    }

    @SuppressLint("MissingPermission")
    fun updateUI() {
        if (::binding.isInitialized) {

            lifecycleScope.launch(Dispatchers.Main) {


                /*
                pasar esto al observer de viewers o followers

                var viewersCount = 0
                var goingCount = 0
                var calledAuthoritiesCount = 0


                eventData.viewers?.forEach { (s, viewer) ->
                    if (s != FirebaseAuth.getInstance().uid.toString()) {
                        viewersCount++

                        if (viewer.going_time != null) {
                            goingCount++
                        }
                        if (viewer.call_time != null) {
                            calledAuthoritiesCount++
                        }

                    }
                }
                if (viewersCount == 0)
                    binding.viewersCount.text = getString(R.string.nobody)
                else
                    binding.viewersCount.text = viewersCount.toString()

                if (goingCount == 0)
                    binding.goingCount.text = getString(R.string.nobody)
                else
                    binding.goingCount.text = goingCount.toString()

                if (calledAuthoritiesCount == 0)
                    binding.calledCount.text = getString(R.string.nobody)
                else
                    binding.calledCount.text = calledAuthoritiesCount.toString()


                val meAsEventFollower =
                    eventData.viewers?.get(FirebaseAuth.getInstance().uid.toString()) as EventFollower
                if (meAsEventFollower.going_time != null) {
                    binding.indicatorGoing.circleBackgroundColor =
                        requireContext().getColor(R.color.colorPrimary)
                } else {
                    binding.indicatorGoing.circleBackgroundColor =
                        requireContext().getColor(R.color.light_gray)
                }
                binding.indicatorGoing.setOnClickListener {

                    if (requireContext() is ViewersActionsCallback) {

                        val callback = object : OnCompleteCallback {
                            override fun onComplete(success: Boolean, result: Any?) {

                            }

                        }
                        (requireContext() as ViewersActionsCallback).onGoingStateChanged(
                            eventData.event_key,
                            FirebaseAuth.getInstance().uid.toString(),
                            callback
                        )
                        //     meAsViewer.going_time = !meAsViewer.going_time
                        updateUI()

                    } else {
                        AppClass.instance.getCurrentActivity()
                            ?.showErrorDialog("No esta conectado a un callback")
                    }
                }

                if (meAsEventFollower.call_time != null) {
                    binding.indicatorAlreadyCalledAuthorities.circleBackgroundColor =
                        requireContext().getColor(R.color.quantum_googgreen500)
                } else {
                    binding.indicatorAlreadyCalledAuthorities.circleBackgroundColor =
                        requireContext().getColor(R.color.light_gray)
                }
                binding.indicatorAlreadyCalledAuthorities.setOnClickListener {

                    if (meAsEventFollower.call_time == null) {
                        if (requireActivity().permissionsForVoiceCall()) {
//this.phoneCallPending = true
                            //                   (this@EventHeaderFragment).fetchDataFromPhoneCallActivity.launch("911")
                            requireActivity().makeAPhoneCall("911")
                        }
                    }

                }
                 */

            }

        }


        binding.indicatorAlreadyCalledAuthorities.getLocationInWindow(calledButtonLoc)


    }


    private fun setupUI() {


        binding.calledCount.setOnClickListener {
            callback.showUsersWhoCalledFragment()
        }


    }


    @JvmName("setEventData1")
    fun setEventData(newEventData: Event) {
        if (::binding.isInitialized) {
            oldEventData = this.eventData
        }
        eventData = newEventData
        if (this.isVisible) {
            updateUI()
        }
    }

    @JvmName("getEventData1")
    fun getEventData(): Event {
        return this.eventData
    }

    fun updateEventData(event: Event) {
        eventData = event
        updateUI()
    }

}