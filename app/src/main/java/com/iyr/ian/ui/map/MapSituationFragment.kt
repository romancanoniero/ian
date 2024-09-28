package com.iyr.ian.ui.map


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.AppConstants.Companion.ZOOM_TO_CITY
import com.iyr.ian.AppConstants.Companion.ZOOM_TO_STATE
import com.iyr.ian.AppConstants.Companion.ZOOM_TO_STREETS
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.models.LocationUpdate
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.dao.repositories.EventsRepository
import com.iyr.ian.databinding.FragmentMapSituationBinding
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.map.adapters.EventHeaderAdapter
import com.iyr.ian.ui.map.enums.CameraModesEnum
import com.iyr.ian.ui.map.enums.MapObjectsType
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.ui.map.helpers.MarkerUpdatesManager
import com.iyr.ian.ui.map.infowindow.CustomInfoWindowAdapter
import com.iyr.ian.ui.map.models.CameraMode
import com.iyr.ian.ui.map.models.EventMapObject
import com.iyr.ian.utils.animateMapCamera
import com.iyr.ian.utils.asSeconds
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.isMoving
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.models.ViewAttributes
import com.iyr.ian.utils.moveMapCamera
import com.iyr.ian.utils.px
import com.iyr.ian.utils.screenHeight
import com.iyr.ian.utils.screenWidth
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode


class MapConfigs : Serializable {
    var zoomLevel: Float = 0f
    var location: LatLng = LatLng(0.0, 0.0)
}


enum class ChatFragmentStatus {
    LOADING, OPEN, CLOSED
}


class MapSituationFragment() : Fragment(), EventHeaderCallback {

    val mainActivityViewModel: MainActivityViewModel by lazy {
        MainActivityViewModel.getInstance(
            requireContext(), UserViewModel.getInstance().getUser()?.user_key.toString() ?: "???"
        )
    }


    private var observing = false
    internal lateinit var viewModel: MapSituationFragmentViewModel

    var changesPending = ArrayList<String>()
    private lateinit var markerUpdatesManager: MarkerUpdatesManager
    var initialRenderDone = false
    private var mapConfigs: HashMap<String, MapConfigs> = HashMap<String, MapConfigs>()
    private var colors: ArrayList<Long>? = null

    // private var mapUpdatesJob: Job? = null
    private var remainingTimeUpdatesJob: Job? = null
    private var fabExpanded: Boolean = false
    internal var userFollowed: String? = null

    private var fragmentMessagesConfigMap: HashMap<String, Any> = HashMap()

    private var currentZoomLevel: Float? = null
    private val closestZoom = ZOOM_TO_STREETS
    private val farthestZoom = ZOOM_TO_STATE// 6.0f
    private val markerZoomDefault = ZOOM_TO_CITY
    private var markerIconHeight = 0
    private var usersIconHeight = 0

    private var eventsList = ArrayList<EventFollowed>()

    var messages: ArrayList<Message> = ArrayList<Message>()
    //  var chatFragment: MessagesInEventFragment? = null


    internal var isCameraSelectorOpen: Boolean = false
    var currentCameraMode: CameraMode = CameraMode(CameraModesEnum.CENTER_IN_EVENT_LOCATION)

    private var sheetBehavior: BottomSheetBehavior<View>? = null
    private lateinit var bottomSheetLayout: ConstraintLayout
    internal lateinit var binding: FragmentMapSituationBinding
    internal lateinit var mapView: SupportMapFragment
    internal var mMyLocation: LatLng? = null
    private var mEventsMarkersMap: HashMap<String?, Marker?>? = HashMap()
    internal var markersMap: HashMap<String, Marker?> = HashMap()
    internal var markersImagesMap: HashMap<String, Bitmap> = HashMap()
    internal var mapObjectsMap: HashMap<String, EventMapObject> = HashMap()
    private var mapRoutes: HashMap<String, Polyline> = HashMap()
    internal var mMap: GoogleMap? = null
    var currentEvent: Event? = null

    internal var toolbarIconWitdh: Int = 0
    internal var toolbarIconHeight: Int = 0

    private lateinit var customInfoWindowAdapter: CustomInfoWindowAdapter

    private var isFirstRun = true

    @SuppressLint("MissingPermission")
    private val mapSyncCallback: OnMapReadyCallback = OnMapReadyCallback { googleMap ->
        mMap = googleMap


        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
            if (!success) {
                Log.e("CUSTOM_MAP", "Style parsing failed.")
            }

        } catch (e: Resources.NotFoundException) {
            Log.e("CUSTOM_MAP", "Can't find style. Error: ", e)
        }
        markerUpdatesManager = MarkerUpdatesManager(requireActivity(), mMap!!)

        val markerSize: Int = resources.getDimension(R.dimen.marker_user_image_size).toInt()

        mMap!!.setPadding(
            markerSize, markerSize, markerSize, markerSize
        )

        mMap!!.isMyLocationEnabled = true
        mMap!!.uiSettings.setAllGesturesEnabled(true)
        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        mMap!!.uiSettings.isZoomGesturesEnabled = true
        mMap!!.uiSettings.isZoomControlsEnabled = false
        mMap!!.setMinZoomPreference(farthestZoom)
        mMap!!.setMaxZoomPreference(closestZoom)

        // infowindow
        eventsHeaderRecyclerAdapter.setMapRef(mMap!!)

        customInfoWindowAdapter = CustomInfoWindowAdapter(
            requireContext(), viewModel.infoWindowData, viewLifecycleOwner
        )
        mMap!!.setOnInfoWindowCloseListener { marker ->
            viewModel.resetInfoWindowData()
        }
        mMap!!.setInfoWindowAdapter(customInfoWindowAdapter)


        viewModel.onResetEvent.observe(viewLifecycleOwner) { event ->
           // viewModel.onResetEvent.removeObservers(this)
         //   viewModel.disconnectToCurrentEvent()

           resetMap()
            /// muevo el mapa
            val eventInfo = event
            val latLng = LatLng(
                eventInfo?.location?.latitude ?: 0.0, eventInfo?.location?.longitude ?: 0.0
            )

            mMap!!.moveMapCamera(latLng, farthestZoom)

            val location = LocationUpdate()
            location.location = latLng
            mMap!!.setOnCameraMoveStartedListener { reason ->
                // lifecycleScope.launch(Dispatchers.Main) {
                //stopRippleAll()
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    setCameraMode(CameraModesEnum.FREE_MODE)
                }
                // }
            }

            mMap!!.setOnCameraIdleListener {

                synchronized(lock) {

                    //              lifecycleScope.launch(Dispatchers.Main) {
                    if (!isFirstRun) {
                        isFirstRun = false
                        Thread {
                            remainingTimeUpdatesJob =
                                remainingTimeStartRepeatingJob(5.asSeconds)
                        }.start()
                    }

                    if (currentZoomLevel == null || currentZoomLevel != mMap!!.cameraPosition.zoom) {
                        updateMarkersSize()
                        viewModel.auxEventKey.value?.let { eventKey ->
                            if (mapConfigs.containsKey(eventKey)) {
                                val config = mapConfigs[eventKey]!!
                                config.location = mMap!!.cameraPosition.target
                                config.zoomLevel = mMap!!.cameraPosition.zoom
                                mapConfigs.set(eventKey, config)
                            }
                        }
                    }
                }
            }
        }
        /*
                mMap!!.setOnMarkerClickListener { marker ->
                    requireContext().handleTouch()
                    val markerKey = (marker.tag as Bundle).getString("key").toString()
                    val mapObject = mapObjectsMap[markerKey]
                  /*
                    customInfoWindowAdapter.getInfoWindow(marker)
                    viewModel.onMarkerClicked(markerKey)
        */
                    customInfoWindowAdapter.setSelectedMarker(marker)
                    marker.showInfoWindow()
                    if (mapObject?.type != MapObjectsType.EVENT_MARKER) {
                        val eventKey = MapSituationFragmentViewModel.getInstance().auxEventKey.value ?: ""
                      /*
                        lifecycleScope.launch(Dispatchers.Main) {
                            marker.showInfoWindow()
                        }
                        viewModel.onConnectToInfoWindowData(
                            eventKey, markerKey
                        )

                       */
                    }


                    true
                }
          */


        val locationButton = (mapView.requireView()
            .findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
        val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
        rlp.addRule(RelativeLayout.ALIGN_END, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        rlp.setMargins(80, 0, 0, 200.px)
        locationButton.requestLayout()

        setCameraMode(CameraModesEnum.MY_LOCATION)

        mMap!!.setOnMapClickListener {
            if (chatFragmentStatus == ChatFragmentStatus.OPEN) {
                animateChatFragmentOut()
            }
        }

        if (!observing) {
            startObservers()
        }
    }


    private fun createRoutesColors() {
        colors = ArrayList<Long>()
        colors?.add(R.color.material_amber500.toLong())
        colors?.add(R.color.material_red500.toLong())
        colors?.add(R.color.material_blue500.toLong())
        colors?.add(R.color.material_green500.toLong())
        colors?.add(R.color.material_purple500.toLong())
        colors?.add(R.color.material_yellow500.toLong())
    }


    private var chatButtonAttrs: ViewAttributes? = null

    var refreshTimes = 0
    val fabLoc = IntArray(2) // Ubicacion del boton de chat


    val titleBar by lazy { requireActivity().findViewById<ConstraintLayout>(R.id.title_bar) }
    val bottomToolbar: MaterialCardView by lazy { requireActivity().findViewById<MaterialCardView>(R.id.bottom_toolbar) }


    private val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

            // Tomo los datos de la pantalla total.
            val r = Rect()
            binding.root.getLocalVisibleRect(r)

            val titleBarRec = Rect()
            titleBar?.getLocalVisibleRect(titleBarRec)

            val bottomToolbarRect = Rect()
            bottomToolbar.getGlobalVisibleRect(bottomToolbarRect)
            val bottomToolbarLoc = IntArray(2) // Ubicacion del boton de chat
            bottomToolbar.getLocationOnScreen(bottomToolbarLoc)

            // Ajusto la ubicacion del boton de chat
            val buttonCalls = binding.root.findViewById<MaterialButton>(R.id.called_count)
            if (buttonCalls != null) {

                val buttonCallsLoc = IntArray(2) // Ubicacion del boton de chat

                // chatButton

                buttonCalls.getLocationOnScreen(buttonCallsLoc)
                val chatButtonLayoutParams =
                    (binding.fabSection.layoutParams as LinearLayout.LayoutParams)

                val marginBottom = r.height() - buttonCallsLoc[1] - buttonCalls.height
                chatButtonLayoutParams.setMargins(0, 0, 0, marginBottom)

                binding.fabSection.requestLayout()
                requireActivity().window.decorView.context.resources.displayMetrics.heightPixels

                binding.fabSection.getLocationInWindow(fabLoc)

                if (chatButtonAttrs == null) {
                    chatButtonAttrs = ViewAttributes(
                        fabLoc[0], fabLoc[1], binding.fabSection.width, binding.fabSection.height
                    )
                } else {
                    chatButtonAttrs?.x = fabLoc[0]
                    chatButtonAttrs?.y = fabLoc[1]
                    chatButtonAttrs?.width = binding.fabSection.width
                    chatButtonAttrs?.height = binding.fabSection.height
                }

            }


            val titleBar = (requireActivity() as MainActivity).getLayoutView(R.id.top_section)
            val topMargin = titleBar?.height
                ?: (0 + AppClass.instance.resources.getDimension(R.dimen.marker_user_image_size))
                ?: 10.px


            var eventPager = binding.root.findViewById<ConstraintLayout>(R.id.event_pager_section)
            var eventPagerRect = Rect()
            val locationArray = IntArray(2)
            //    var eventPagerInfo = eventPager.getLocationInWindow(locationArray)
            val bottomMargen = r.height() - locationArray.get(1)

            mMap?.setPadding(2.px, topMargin.toInt(), 2.px, bottomMargen ?: 10.px)

            binding.fabChat.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.fabChat.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val fabChatDimsArray = IntArray(2)
                    binding.fabChat.getLocationOnScreen(fabChatDimsArray)
                    val toolbarDimsArray = IntArray(2)
                    val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
                    toolbar.getLocationOnScreen(toolbarDimsArray)
                    val fabChatY = fabChatDimsArray[1] + binding.fabChat.height
                    val toolbarY = toolbar.y + toolbar.height

                    val displayMetrics = requireContext().resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    fragmentMessagesConfigMap.put("fab_chat_bottom", fabChatY)
                    fragmentMessagesConfigMap.put("toolbar_bottom", toolbarY)
                    // obtener el ancho de pantalla accediendo a sysmetrics
                    fragmentMessagesConfigMap.put("screen_width", screenWidth)

                    viewModel.onScreenDimensionsChanged(fragmentMessagesConfigMap)
                }
            })

        }
    }

    lateinit var eventsHeaderRecyclerAdapter : EventHeaderAdapter

    private val args: MapSituationFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EVENT_CREATION", this.javaClass.name)

        val parameters = arguments

        viewModel = MapSituationFragmentViewModel.getInstance()

        initializeEventsListAdapter()
        args.eventKey.let { eventKey ->
            changeToEvent(eventKey!!)
        }





        lifecycle.addObserver(viewModel)
        //      parameters?.getString("eventKey")?


//        createRoutesColors()
        mEventsMarkersMap = HashMap()

        toolbarIconWitdh = requireContext().resources.getDimension(R.dimen.box_small).toInt()
        toolbarIconHeight = requireContext().resources.getDimension(R.dimen.box_small).toInt()
        markerIconHeight = context?.resources?.getDimension(R.dimen.box_xbig)?.toInt()!!
        usersIconHeight = context?.resources?.getDimension(R.dimen.box_normal)?.toInt()!!
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapSituationBinding.inflate(layoutInflater, container, false)
        mapView = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        binding.unreadCounterText.visibility = GONE

        binding.eventsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.eventsRecyclerView.adapter = eventsHeaderRecyclerAdapter
        binding.eventsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //   super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = layoutManager.findFirstVisibleItemPosition()
                    val event = eventsHeaderRecyclerAdapter.events[position]
                  if (viewModel.getEventSelectedKey() != event.event_key) {
                      this@MapSituationFragment.changeToEvent(event.event_key)

                    }
                }
            }
        })

        binding.dotsIndicator.attachTo(binding.eventsRecyclerView, true)
        eventsHeaderRecyclerAdapter.notifyDataSetChanged()
        binding.dotsIndicator.numberOfIndicators = eventsHeaderRecyclerAdapter.events.size


        binding.elapsedPeriod.maxValue = 180f
        setupToolbar()

        binding.elapsedPeriod.visibility = GONE

        //   chatFragment?.setViewReference(binding.fabChat)
        binding.cameraModeSection.setOnClickListener {
            toggleCameraModeSelector(isCameraSelectorOpen)
        }

        setupFABs()

        setupCameraModesUI()
        //finishSetupOfChatFragment()


        binding.root.viewTreeObserver.addOnGlobalLayoutListener(
            globalLayoutListener
        )
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        refreshTimes = 0
        MainActivityViewModel.getInstance().currentFragment = R.id.mapSituationFragment
        val mainActivityBindings = (requireActivity() as MainActivity).binding
        (requireActivity() as MainActivity).expandNavigationFragmentToTop()

        if (findNavController().currentDestination?.id == R.id.mapSituationFragment) {
            val appToolbar = (requireActivity() as MainActivity).appToolbar
            appToolbar.enableBackBtn(true)
            appToolbar.updateTitle(getString(R.string.action_events_map_title))
            mainActivityBindings.includeCustomToolbar.root.visibility = View.VISIBLE
            mainActivityBindings.bottomToolbar.visibility = View.GONE
        }

        if (userVisibleHint) {
            if (mMap == null) {
                mapView.getMapAsync(
                    mapSyncCallback
                )
            } else {
                if (!observing) {
                    startObservers()
                }
            }
        }


        /*
        ojo con esto.. 26/9/2024 esto hay que adaptarlo
        if (viewModel.auxEventKey.value != null) {
            startObserveEvent(viewModel.auxEventKey.value!!)
        } else {
            if (AppClass.instance.eventsMap.value?.size == 0) {
                findNavController().popBackStack()
            } else {
                AppClass.instance.eventsMap.value?.values?.first()?.let { event ->
//                    startObserveEvent(event.event_key)
                    viewModel.onEventSelected(event.event_key)
                }
            }
        }
        */
//        resumeConnections()

        viewModel.onFragmentResume()

    }

    private fun resumeConnections() {
    }


    override fun onPause() {
        super.onPause()

        viewModel.onFragmentPaused()
        remainingTimeUpdatesJob?.cancel()
        remainingTimeUpdatesJob = null
        viewModel.onDisconnectionRequested()
        stopObservers()
        MainActivityViewModel.getInstance().currentFragment = null

        (requireActivity() as MainActivity).restoreNavigationFragment()
        (requireActivity() as MainActivity).binding.root.forceLayout()
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetEventKey()
    }

    private fun startObservers() {


        viewModel.goToEvent.observe(this) { eventKey ->
            if (eventKey != null) {
                //      viewModel.onEventSelected(eventKey)
            }
        }

        viewModel.auxEventKey.observe(this) { eventKey ->
            if (eventKey != null) {/*
                        Toast.makeText(
                            requireContext(),
                            "Evento seleccionado: $eventKey",
                            Toast.LENGTH_SHORT
                        ).show()
                        */

            }

        }


        viewModel.eventFlow.observe(this) { resource ->

            when (resource) {
                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(
                        "Error", resource.message ?: "Error desconocido"
                    )
                }

                is Resource.Loading -> {}

                is Resource.Success -> {


                    val event = resource.data
                    currentEvent = event

                    val eventLocation =
                        LatLng(event?.location?.latitude ?: 0.0, event?.location?.longitude ?: 0.0)
                    customInfoWindowAdapter.updateEventLocation(eventLocation)

                    // b) Escucho a los seguidores del evento.
                    val eventKey = event?.event_key.toString()

//                    if (viewModel.getEventSelectedKey() != eventKey) // Cambio de evento
//                    {
                    selectCurrentDot(eventKey)
                    // }

                    viewModel.setEventTimeOfLastView(
                        FirebaseAuth.getInstance().uid.toString(), event?.event_key ?: "error"
                    )
                    // dedicarme a que el siguiente metodo se transforme en MVVM
                    //       setCurrentEventData(event!!)
                    updateElapsedTimeIndicator()
                    // actualizo la ubicacion del marker del evento
                    if (!mapObjectsMap.containsKey(viewModel.auxEventKey.value)) // Si el Marker del Evento no existe
                    {
                        var eventMarkerLocation: LatLng? = null
                        val currentEvent = viewModel.lastEventUpdate
                        if (currentEvent.event_location_type == EventLocationType.FIXED.toString()) {

                            eventMarkerLocation = LatLng(
                                (currentEvent.location!!.latitude)!!,
                                (currentEvent.location!!.longitude)!!
                            )

                            //   markersMap.put(event.event_key, null)
                            val newObject = EventMapObject(
                                currentEvent.event_key,
                                MapObjectsType.EVENT_MARKER,
                                null,
                                currentEvent.event_type,
                                eventMarkerLocation
                            )
                            mapObjectsMap[currentEvent.event_key] = newObject
                            createAndAddMapElement(newObject)
                        }
                    } else {
                        if (this.markersMap.get(currentEvent?.event_key) != null) {
                            //             moveEventMarker(event)
                        }
                    }

                    // Actualizo el adapter de los eventos
                    eventsHeaderRecyclerAdapter.setEventData(currentEvent!!)

                }

            }
        }
        viewModel.eventFollowersFlow.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(
                        "Error", resource.message ?: "Error desconocido"
                    )
                }

                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    val event = resource.data

                    var follower: EventFollower?
                    when (event) {
                        is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                            follower = event.data
                            if (follower.following && follower.is_author == false || currentEvent?.event_location_type == EventLocationType.REALTIME.toString()) {
                                val marker = markersMap[follower.user_key]
                                // si el marker no existe, lo crea y agrega al mapa
                                if (marker == null) {
                                    if (markersMap.containsKey(follower.user_key) == false) {
                                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                            addNewMarkerToMap(follower!!)
                                        }
                                    }
                                } else {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        val newLocation = LatLng(follower!!.l[0], follower!!.l[1])
                                        addNewPosition(marker, newLocation)
                                    }
                                }

                            }
                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {
                            follower = event.data

                            val marker = getMapMarker(follower.user_key)
                            // si el marker no existe, lo crea y agrega al mapa
                            if (marker == null) {
                                addNewMarkerToMap(follower)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    val newLocation = LatLng(
                                        BigDecimal(follower.l[0]).setScale(
                                            7, RoundingMode.HALF_UP
                                        ).toDouble(), BigDecimal(follower.l[1]).setScale(
                                            7, RoundingMode.HALF_UP
                                        ).toDouble()
                                    )

                                    //                                    var newLocation = LatLng(follower!!.l[0], follower!!.l[1])
                                    addNewPosition(marker, newLocation)

                                    if (!marker.isMoving()) {
                                        moveMarker(marker)
                                    }
                                }
                            }


//saque esto y lo pase al event header adapter
                            viewModel.eventFlow.value?.data?.let { event ->
                                eventsHeaderRecyclerAdapter.updateFollowerByEventKey(
                                    follower
                                )
                            }

                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> {
                            Toast.makeText(
                                requireContext(),
                                "Se fue " + event.data.user_key + " - Implementar!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {}
                    }
                }
            }
        }

        viewModel.resetEvent.observe(viewLifecycleOwner) { reset ->
            when (reset) {
                true -> {
                    //                 Toast.makeText(requireContext(), "Reset", Toast.LENGTH_SHORT).show()
                    resetMap()

                    viewModel.onResetEventDone()
                }

                else -> {}
            }
        }

        viewModel.shimmmerVisible.observe(viewLifecycleOwner) { visible ->
            if (visible == true) {
                startShimmer()
            } else {
                stopShimmer()
            }
        }

        viewModel.spinner.observe(viewLifecycleOwner) { value ->
            value.let { show ->
                if (show == true) mainActivityViewModel.showLoader()
                else mainActivityViewModel.hideLoader()
            }
        }

        viewModel.onCloseEventAction.observe(viewLifecycleOwner) { action ->
            when (action) {
                MapSituationFragmentViewModel.OnEventClosedActionsEnum.LOAD_FIRST_EVENT -> {
                    val nextEvent = eventsList.first()
                    changeToEvent(nextEvent.event_key)
                }

                MapSituationFragmentViewModel.OnEventClosedActionsEnum.GO_MAIN -> {
                    findNavController().popBackStack()
                }

                null -> {}
            }

        }




        AppClass.instance.eventsListFlow.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is EventsRepository.DataEvent.OnChildAdded -> {
                    val event = resource.data
                    if (!eventsHeaderRecyclerAdapter.isEventExists(event.event_key)) {
                        eventsHeaderRecyclerAdapter.events.add(event)
                        eventsHeaderRecyclerAdapter.events.sortBy { it.event_creation_time }
                        eventsHeaderRecyclerAdapter.notifyItemInserted(
                            eventsHeaderRecyclerAdapter.events.size - 1
                        )
                        binding.dotsIndicator.numberOfIndicators =
                            eventsHeaderRecyclerAdapter.events.size
                        if (viewModel.getEventSelectedKey().isNullOrEmpty()) {
                            changeToEvent(event.event_key)
                        }
                    }
                }

                is EventsRepository.DataEvent.OnChildChanged -> {}
                is EventsRepository.DataEvent.OnChildMoved -> {}
                is EventsRepository.DataEvent.OnChildRemoved -> {
                    val existingEvents = AppClass.instance.eventsMap.value?.size ?: 0
                    binding.dotsIndicator.numberOfIndicators = existingEvents
                }

                is EventsRepository.DataEvent.OnError -> TODO()
            }

        }



        viewModel.isChatOpen.observe(viewLifecycleOwner) { isOpen ->

            if (isOpen == true == true) {


                // TODO : Pasarlo al viewmodel
                viewModel.resetUnreadMessages(
                    mainActivityViewModel.userKey.toString(), viewModel.auxEventKey.value.toString()
                )


            }


        }

        //----  Chatroom
        viewModel.isChatButtonVisible.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.fabOptionsLayout.isVisible = it
            }
        }


        viewModel.unreadMessages.observe(viewLifecycleOwner) { unreadMessages ->
            if (unreadMessages > 0) {
                binding.unreadCounterText.text = unreadMessages.toString()
                binding.unreadCounterText.visibility = VISIBLE
            } else {
                binding.unreadCounterText.visibility = GONE
            }
        }

        viewModel.bottomBarVisibilityStatus.observe(viewLifecycleOwner) { visible ->
            if (visible != null) {
                if (visible) mainActivityViewModel.onBottomBarVisibilityOnRequired()
                else mainActivityViewModel.onBottomBarVisibilityOffRequired()
            }
        }

        /*
                viewModel.infoWindowData.observe(viewLifecycleOwner) { data ->


                    customInfoWindowAdapter.updateInfoWindowContent(data)
                }
        */
        observing = true
    }

    /***
     * Cambia el punto del evento seleccionado
     */
    private fun selectCurrentDot(eventKey: String) {
        val index =
            eventsHeaderRecyclerAdapter.events.indexOfFirst { it.event_key == eventKey }
        binding.dotsIndicator.selectedPosition = index
    }

    private fun initializeEventsListAdapter() {
        eventsHeaderRecyclerAdapter = EventHeaderAdapter((requireActivity() as AppCompatActivity), mMap, this, lifecycleScope)

        val list = AppClass.instance.eventsMap.value ?: HashMap<String, EventFollowed>()
        eventsHeaderRecyclerAdapter.events.clear()
        eventsList.clear()
        list.values.sortedBy { it.event_creation_time }.forEach { record ->
            val event = record
            eventsHeaderRecyclerAdapter.events.add(event)
            eventsList.add(event)
        }

    }

    private fun moveRecyclerEventTo(position: Int) {
        if (position in 0 until eventsHeaderRecyclerAdapter.events.size) {
            (activity as? AppCompatActivity)?.runOnUiThread {
                binding.eventsRecyclerView.scrollToPosition(position)
            }
        }
    }

    private fun changeToEvent(eventKey: String) {

Log.d("EVENT_CREATION", "changeToEvent = $eventKey")

        viewModel.connectToEventRequest(eventKey)
    }


    private fun stopObservers() {

        viewModel.messageIncomming.removeObservers(this)
        viewModel.eventFollowersFlow.removeObservers(this)
        viewModel.listEventFlowCancel()
        viewModel.spinner.removeObservers(this)
        //viewModel.resetMap.removeObservers(this)
        viewModel.onCloseEventAction.removeObservers(this)
        AppClass.instance.eventsMap.removeObservers(this)
        viewModel.auxEventKey.removeObservers(this)
        viewModel.currentEvent.removeObservers(this)
        viewModel.isChatOpen.removeObservers(this)
        //viewModel.showChatFragment.removeObservers(this)
        viewModel.isChatButtonVisible.removeObservers(this)
        viewModel.isCounterVisible.removeObservers(this)
        viewModel.bottomBarVisibilityStatus.removeObservers(this)
        viewModel.eventFlow.removeObservers(this)
        mainActivityViewModel.isKeyboardOpen.removeObservers(this)
        //      viewModel.infoWindowData.removeObservers(this)
        observing = false

    }


    private fun startShimmer() {
        binding.shimmerLayout.visibility = VISIBLE
        binding.shimmerLayout.startShimmer()
        binding.eventsRecyclerView.visibility = GONE
    }

    private fun stopShimmer() {
        binding.shimmerLayout.apply {
            stopShimmer()
            visibility = View.GONE
        }
        binding.eventsRecyclerView.visibility = VISIBLE
    }



// MAP & MARKERS

    /**
     * Agrega un nuevo movimiento al marker
     */
    suspend fun addNewPosition(marker: Marker, position: LatLng) {
        val channel = pendingPositions.getOrPut(marker) { Channel(Channel.UNLIMITED) }
        channel.send(position)
    }

    /**
     * Devuelve un objeto Marker a partir de la clave o null si no existe
     */
    private fun getMapMarker(key: String): Marker? {
        return markersMap[key]
    }


    internal val lock = Any()


    val pendingPositions = mutableMapOf<Marker, Channel<LatLng>>()


    override fun onStop() {
        super.onStop()
        resetMap()
    }

    private fun setupUI() {

        //--- adapter de eventos


    }

    private fun setupFABs() {
        binding.fabChat.setOnClickListener {

            val navOptions = NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_right).setPopEnterAnim(R.anim.slide_in_right)
                .setPopExitAnim(R.anim.slide_out_right).build()

            val action =
                MapSituationFragmentDirections.actionMapSituationFragmentToMessagesInEventFragment(
                    fragmentMessagesConfigMap
                )
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.mapSituationFragment) {
                findNavController().navigate(action, navOptions)
            }
        }
        binding.fabChat.imageTintList = ColorStateList.valueOf(
            getColor(
                requireContext(), R.color.colorPrimary
            )
        )





        binding.fabMain.setOnClickListener {
            if (fabExpanded) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
        }
        closeSubMenusFab()
    }


    private fun setupToolbar() {
        binding.cameraModeButton.setOnClickListener {
            toggleCameraModeSelector(isCameraSelectorOpen)
        }
        binding.cameraModeButton.imageTintList = ColorStateList.valueOf(
            getColor(
                requireContext(), R.color.white
            )
        )
        setupBottomSheetLayout()
    }

    private fun resetMap() {
        mEventsMarkersMap?.clear()
        markersMap.clear()
        mapObjectsMap.clear()
        mapRoutes.clear()
        mMap?.clear()
        if (mapConfigs.containsKey(viewModel.auxEventKey.value)) {
            val config = mapConfigs[viewModel.auxEventKey.value]!!
            mMap?.moveMapCamera(config.location, config.zoomLevel)
        } else {
            mMap?.animateMapCamera(farthestZoom)
        }
    }


    //------ FAB
    private fun closeSubMenusFab() {
        binding.fabMain.setImageResource(R.drawable.ic_squares)
        fabExpanded = false
    }

    //Opens FAB submenus
    private fun openSubMenusFab() {
        binding.fabMain.setImageResource(R.drawable.ic_close)
        fabExpanded = true
    }


    private var chatFragmentStatus: ChatFragmentStatus? = ChatFragmentStatus.CLOSED


    var listener: FragmentManager.OnBackStackChangedListener? = null

    override fun showUsersParticipatingFragment() {
        val action =
            MapSituationFragmentDirections.actionMapSituationFragmentToUsersParticipatingFragment()
        findNavController().navigate(action)

    }

    override fun showUsersWhoCalledFragment() {

        val action =
            MapSituationFragmentDirections.actionMapSituationFragmentToUsersThatCalledFragment()
        findNavController().navigate(action)

    }

    override fun showUsersGoingFragment() {
        val action = MapSituationFragmentDirections.actionMapSituationFragmentToUsersGoingFragment()
        findNavController().navigate(action)


    }


    private fun setupBottomSheetLayout() {
        bottomSheetLayout = binding.root.findViewById(R.id.bottom_sheet_layout)!!
        bottomSheetLayout.maxHeight = (resources.displayMetrics.heightPixels * 0.8).toInt()
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
    }


    //----- DataAccess
    fun disconnectFromEvent() {
        viewModel.onDisconnectionRequested()
    }


    private val nextEvent: EventFollowed?
        get() {
            if (getEventsFollowed().size > 0) return getEventsFollowed()[0]
            return null
        }

    private fun hideEventsToolbar() {
        binding.eventsToolbar.visibility = GONE
    }


    fun getMyTrackingEvent(): EventFollowed? {

        getEventsFollowed().forEach { event ->
            if (event.author.author_key == FirebaseAuth.getInstance().uid.toString() && event.event_type == EventTypesEnum.SCORT_ME.name) {
                return event
            }
        }
        return null
    }


    fun resolveOnEventRemoved(eventKey: String) {
        if (currentEvent != null) {
            if (eventKey == currentEvent?.event_key) {
                val nextEvent = nextEvent
                if (nextEvent == null) {
                    resetMap()
                    hideEventsToolbar()
                    //  mainActivity.switchToModule(0, "home")
//                    mainActivity.goHome()
                    findNavController().popBackStack()

                } else {
//                    selectEvent(nextEvent.event_key)
                    Log.d(
                        "FLOW_CONNECT_VIEWMODEL",
                        "Llamo a la conexcion del evento {$eventKey} desde resolveOnEventRemoved"
                    )

                    changeToEvent(nextEvent.event_key)
                }
            }
        }


    }


    // TODO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {

            //  if (requestCode === com.github.dhaval2404.imagepicker.ImagePicker.REQUEST_CODE) {

        }
    }


    private fun remainingTimeStartRepeatingJob(timeInterval: Long): Job {
        return viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                // add your task here
                updateElapsedTimeIndicator()
                delay(timeInterval)
            }
        }
    }

    private fun updateElapsedTimeIndicator() {
        currentEvent?.let { event ->
            binding.elapsedPeriod.value =
                (((System.currentTimeMillis() - event.time).toFloat() / 1000)) / 60

            binding.elapsedPeriod.maxValue =
                (((event.expires_at - event.time) / 1000) / 60).toFloat()

            if (chatFragmentStatus == ChatFragmentStatus.CLOSED && binding.elapsedPeriod.visibility == GONE) {
                requireActivity().runOnUiThread {
                    binding.elapsedPeriod.visibility = VISIBLE
                }
            }

        }
    }


    private fun getEventsFollowed(): ArrayList<EventFollowed> {
        return (requireActivity() as MainActivity).getEventsFollowed()
    }


    @JvmName("getEventKey1")
    fun getEventKey(): String? {
        return viewModel.auxEventKey.value
    }

    private fun enableAdapterButtons() {
        eventsHeaderRecyclerAdapter.enableButtons()
    }

    private fun animateChatFragmentOut() {
        chatFragmentStatus = ChatFragmentStatus.LOADING
        val frameLayout = binding.chatLayout

// Crea una animación de transición
        val slide = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT,
            0.0f,
            Animation.RELATIVE_TO_PARENT,
            1.0f,
            Animation.RELATIVE_TO_PARENT,
            0.0f,
            Animation.RELATIVE_TO_PARENT,
            0.0f
        ).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
        }

        slide.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                chatFragmentStatus = ChatFragmentStatus.CLOSED
                binding.fabOptionsLayout.isVisible = true
                binding.elapsedPeriod.isVisible = true
                viewModel.onChatClosed()
                //           chatFragment?.disableControls()
//                frameLayout.x= 1000f

                frameLayout.visibility = GONE

                binding.root.requestLayout()
                AppClass.instance.setCurrentFragment(this@MapSituationFragment)
                enableAdapterButtons()

            }

            override fun onAnimationRepeat(animation: Animation?) {
                //  TODO("Not yet implemented")
            }
        })

// Aplica la animación al FrameLayout
        frameLayout.startAnimation(slide)

        // Asegúrate de que el FrameLayout tenga un margen derecho de 12dp
        val params = frameLayout.layoutParams as ViewGroup.MarginLayoutParams
        params.rightMargin = 12.px
        frameLayout.layoutParams = params


    }

    override fun setCalledButtonLocationRB(location: IntArray) {
        // ajusta el margen de binding.fabOptionsLayout.marginBottom con el valor de location[1]
        val params = binding.fabOptionsLayout.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(
            0,
            0,
            requireActivity().screenWidth() - location[0],
            location[1] - requireActivity().screenHeight()
        )
        binding.fabOptionsLayout.layoutParams = params

        binding.fabOptionsLayout.requestLayout()
    }


}




