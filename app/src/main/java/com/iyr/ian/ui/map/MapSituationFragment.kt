package com.iyr.ian.ui.map

//import com.google.android.gms.maps.model.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.DisplayMetrics
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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.scale
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.iyr.fewtouchs.utils.osrm.DownloadCallback
import com.iyr.fewtouchs.utils.osrm.OSRMResponse
import com.iyr.fewtouchs.utils.osrm.getTripBetweenCoordsAsync
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.ZOOM_TO_CITY
import com.iyr.ian.AppConstants.Companion.ZOOM_TO_STATE
import com.iyr.ian.AppConstants.Companion.ZOOM_TO_STREETS
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.callbacks.ViewersActionsCallback
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.models.LocationUpdate
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.dao.repositories.EventRepository
import com.iyr.ian.dao.repositories.EventsRepository
import com.iyr.ian.databinding.FragmentMapSituationBinding
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.UserTypesEnum
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.chat.ChatWindowStatus
import com.iyr.ian.ui.chat.MessagesFragmentInterface
import com.iyr.ian.ui.chat.MessagesInEventFragment
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.map.adapters.EventHeaderAdapter
import com.iyr.ian.ui.map.enums.CameraModesEnum
import com.iyr.ian.ui.map.enums.MapObjectsType
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.ui.map.fragments.UsersGoingFragment
import com.iyr.ian.ui.map.fragments.UsersParticipatingFragment
import com.iyr.ian.ui.map.fragments.UsersThatCalledFragment
import com.iyr.ian.ui.map.helpers.MarkerUpdatesManager
import com.iyr.ian.ui.map.models.CameraMode
import com.iyr.ian.ui.map.models.EventMapObject
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.MathUtils
import com.iyr.ian.utils.animateMapCamera
import com.iyr.ian.utils.asSeconds
import com.iyr.ian.utils.calculateMarkerSize
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.dp
import com.iyr.ian.utils.drawBounds
import com.iyr.ian.utils.geo.calcularLatitudNegativa
import com.iyr.ian.utils.geo.calcularLongitudNegativa
import com.iyr.ian.utils.getBitmapFromVectorDrawable
import com.iyr.ian.utils.getKey
import com.iyr.ian.utils.getMarkerFromView
import com.iyr.ian.utils.getUserMarkerOptions
import com.iyr.ian.utils.isMoving
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.markers_utils.MarkerAnimationCallback
import com.iyr.ian.utils.models.ViewAttributes
import com.iyr.ian.utils.moveMapCamera
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.px
import com.iyr.ian.utils.resizeDrawable
import com.iyr.ian.utils.screenHeight
import com.iyr.ian.utils.screenWidth
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.utils.turnMovingOn
import com.iyr.ian.utils.zoomToBounds
import com.iyr.ian.utils.zoomToFitMarkers
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.MessagesInEventFragmentViewModel
import com.utsman.smartmarker.googlemaps.toLatLngGoogle
import com.utsman.smartmarker.moveMarkerSmoothly
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import kotlin.coroutines.resume
import kotlin.math.abs


class MapConfigs : Serializable {
    var zoomLevel: Float = 0f
    var location: LatLng = LatLng(0.0, 0.0)
}


enum class ChatFragmentStatus {
    LOADING, OPEN, CLOSED
}


class MapSituationFragment(
    val mainActivity: MainActivity, val mainActivityViewModel: MainActivityViewModel
) : Fragment(), MessagesFragmentInterface, EventHeaderCallback {

    private lateinit var viewModel: MapSituationFragmentViewModel
    private lateinit var messagingViewModel: MessagesInEventFragmentViewModel

    var changesPending = ArrayList<String>()
    private lateinit var markerUpdatesManager: MarkerUpdatesManager
    var initialRenderDone = false
    private var mapConfigs: HashMap<String, MapConfigs> = HashMap<String, MapConfigs>()
    private var eventKeyShowing: String? = null

    //  private var eventKey: String? = null
    private var colors: ArrayList<Long>? = null
    private var mapUpdatesJob: Job? = null
    private var remainingTimeUpdatesJob: Job? = null
    private var fabExpanded: Boolean = false
    private var userFollowed: String? = null

    private var currentZoomLevel: Float? = null
    private val closestZoom = ZOOM_TO_STREETS
    private val farthestZoom = ZOOM_TO_STATE// 6.0f
    private val markerZoomDefault = ZOOM_TO_CITY
    private var markerIconHeight = 0
    private var usersIconHeight = 0

    private var eventsList = ArrayList<EventFollowed>()

    var messages: ArrayList<Message> = ArrayList<Message>()
    var chatFragment: MessagesInEventFragment? = null
    private val alreadyCalledFragment: UsersThatCalledFragment by lazy {
        UsersThatCalledFragment(
            this, viewModel
        )
    }
    private val usersGoingFragment: UsersGoingFragment by lazy {
        UsersGoingFragment(
            this, viewModel
        )
    }
    private val usersParticipatingFragment: UsersParticipatingFragment by lazy {
        UsersParticipatingFragment(
            this, viewModel
        )
    }

    val lifeCycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            openCameraModeSelector()
        }
    }

    //   private var cameraMode: CameraMode? = null

    private var isCameraSelectorOpen: Boolean = false
    var currentCameraMode: CameraMode = CameraMode(CameraModesEnum.CENTER_IN_EVENT_LOCATION)

    private lateinit var eventInfoTabLayout: TabLayout

    //  private lateinit var bottomSheetEventInfoAdapter: EventBodyPagerAdapter
    private lateinit var eventInfoPager: ViewPager2

    //private lateinit var eventsHeaderAdapter: EventHeaderPagerAdapter
    //private lateinit var eventsPager: ViewPager2
    private var sheetBehavior: BottomSheetBehavior<View>? = null
    private lateinit var bottomSheetLayout: ConstraintLayout
    private lateinit var binding: FragmentMapSituationBinding
    private lateinit var mapView: SupportMapFragment
    private var mMyLocation: LatLng? = null
    private var mEventsMarkersMap: HashMap<String?, Marker?>? = HashMap()
    private var markersMap: HashMap<String, Marker?> = HashMap()
//    private var rippleMap: HashMap<String, Marker?> = HashMap()

    private var mapObjectsMap: HashMap<String, EventMapObject> = HashMap()
    private var mapRoutes: HashMap<String, Polyline> = HashMap()
    private var mMap: GoogleMap? = null
    var currentEvent: Event? = null


    private var toolbarIconWitdh: Int = 0
    private var toolbarIconHeight: Int = 0


    private var isFirstRun = true

    @SuppressLint("MissingPermission")
    private val mapSyncCallback: OnMapReadyCallback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

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

        Log.d("MAPA", "Ahora, voy  a buscar la ubicacion del usuario.")

        lifecycleScope.launch(Dispatchers.IO) {
            SmartLocation.with(context).location().oneFix().start { location ->
                Log.d("MAPA", "Obtuve la ubicacion del usuario.")

                if (location != null) {

                    val latLng = LatLng(location.latitude, location.longitude)

                    mMap!!.moveMapCamera(latLng, farthestZoom)

                    val location = LocationUpdate()
                    location.location = latLng
                    mMap!!.setOnCameraMoveStartedListener { reason ->

                        lifecycleScope.launch(Dispatchers.Main) {
                            //stopRippleAll()
                            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                                setCameraMode(CameraModesEnum.FREE_MODE)
                            }
                        }
                    }

                    mMap!!.setOnCameraIdleListener {

                        synchronized(lock) {

                            lifecycleScope.launch(Dispatchers.Main) {

                                if (!isFirstRun) {
                                    isFirstRun = false
                                    //      configureEventObserver(eventKeyShowing!!)
                                    Thread {
                                        remainingTimeUpdatesJob =
                                            remainingTimeStartRepeatingJob(5.asSeconds)
                                    }.start()
                                }


                                if (currentZoomLevel == null || currentZoomLevel != mMap!!.cameraPosition.zoom) {
                                    val a = currentZoomLevel ?: mMap!!.cameraPosition.zoom
                                    val b = mMap!!.cameraPosition.zoom
                                    val difPercent = abs(((b - a) * 100) / a)
                                    if (currentZoomLevel == null || difPercent >= 1) {

                                        resizeMarkers(mMap!!.cameraPosition.zoom)
                                        currentZoomLevel = mMap!!.cameraPosition.zoom
                                    }
                                    eventKeyShowing?.let { eventKey ->
                                        if (mapConfigs.containsKey(eventKey)) {
                                            val config = mapConfigs[eventKey]!!
                                            config.location = mMap!!.cameraPosition.target
                                            config.zoomLevel = mMap!!.cameraPosition.zoom
                                            mapConfigs.set(eventKey, config)
                                        }

                                    }
                                }
                                binding.root.viewTreeObserver.addOnGlobalLayoutListener(
                                    globalLayoutListener
                                )
                            }


                        }

                    }
                }
            }


        }

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


    }


    fun getBottomSheetLayout(): BottomSheetBehavior<View>? {
        return sheetBehavior
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

    val chatButton: ConstraintLayout by lazy { binding.root.findViewById<ConstraintLayout>(R.id.fab_section) }
    val contentContainer: ConstraintLayout by lazy {
        binding.root.findViewById<ConstraintLayout>(
            R.id.layout_for_vertical_popups
        )
    }
    val chatWindow: FrameLayout by lazy { binding.root.findViewById<FrameLayout>(R.id.chat_layout) }
    val cardView: CardView by lazy { chatWindow.findViewById<CardView>(R.id.cardView) }

    //  val chatWindowCloseButton: ImageView by lazy { chatWindow.findViewById<ImageView>(R.id.close_button) }
    val chatWindowMaximizeButton by lazy { chatWindow.findViewById<ImageView>(R.id.full_screen_button) }
    val chatWindowRestoreNormalSizeButton by lazy { chatWindow.findViewById<ImageView>(R.id.restore_screen_button) }

    val titleBar by lazy { requireActivity().findViewById<ConstraintLayout>(R.id.title_bar) }
    val bottomToolbar: MaterialCardView by lazy { requireActivity().findViewById<MaterialCardView>(R.id.bottom_toolbar) }


    private val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {


            Log.d("REFRESH", "Entro al global layout")
            //   refreshTimes++
            //    Log.d("GLOBAL_LAYOUT", refreshTimes.toString())

            // Tomo los datos de la pantalla total.
            val r = Rect()
            binding.root.getLocalVisibleRect(r)


            val titleBarRec = Rect()
            titleBar?.getLocalVisibleRect(titleBarRec)

            val bottomToolbarRect = Rect()
            bottomToolbar.getGlobalVisibleRect(bottomToolbarRect)
            val bottomToolbarLoc = IntArray(2) // Ubicacion del boton de chat
            bottomToolbar.getLocationOnScreen(bottomToolbarLoc)

            if (!bottomToolbar.isVisible && mainActivityViewModel.isKeyboardOpen()) {

                //               whenKeyboardOpen()

            } else {
                //     viewModel.messageFragmentMode.value

                if (chatFragmentStatus != null && chatFragmentStatus == ChatFragmentStatus.OPEN) {
                    var pp = 3

                    //                  chatWindowResizeToNormalSize()

                }
            }

            // Ajusto la ubicacion del boton de chat
            val buttonCalls = binding.root.findViewById<MaterialButton>(R.id.called_count)
            if (buttonCalls != null) {
                //   if (binding.eventPagerSection.height > 0) {

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


            var titleBar = (requireActivity() as MainActivity).getLayoutView(R.id.top_section)
            var topMargin = titleBar?.height
                ?: 0 + AppClass.instance.resources.getDimension(R.dimen.marker_user_image_size)
                ?: 10.px


            var eventPager = binding.root.findViewById<ConstraintLayout>(R.id.event_pager_section)
            var eventPagerRect = Rect()
            var locationArray = IntArray(2)
            var eventPagerInfo = eventPager.getLocationInWindow(locationArray)
            var bottomMargen = r.height() - locationArray.get(1)

            mMap?.setPadding(2.px, topMargin.toInt(), 2.px, bottomMargen ?: 10.px)



            binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }


    val eventsHeaderRecyclerAdapter by lazy {
        EventHeaderAdapter(requireActivity(), this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EVENT_CREATION", this.javaClass.name)
        viewModel = MapSituationFragmentViewModel(mainActivityViewModel)
        //  viewModel.startListenSubscribedEvents(FirebaseAuth.getInstance().uid.toString())
        messagingViewModel = MessagesInEventFragmentViewModel()

        //   eventsHeaderRecyclerAdapter = EventHeaderAdapter(requireActivity(), this)

        createRoutesColors()
        mEventsMarkersMap = HashMap()
        Log.d("-TIMES 1", Date().toString())

        chatFragment = MessagesInEventFragment(
            requireContext(), mainActivityViewModel, viewModel, messagingViewModel
        )
        Log.d("*TIMES 2", Date().toString())


        toolbarIconWitdh = requireContext().resources.getDimension(R.dimen.box_small).toInt()
        toolbarIconHeight = requireContext().resources.getDimension(R.dimen.box_small).toInt()
        markerIconHeight = context?.resources?.getDimension(R.dimen.box_xbig)?.toInt()!!
        usersIconHeight = context?.resources?.getDimension(R.dimen.box_normal)?.toInt()!!


        //configureEventObserver()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapSituationBinding.inflate(layoutInflater, container, false)
        mapView = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        setupUI()
        finishSetupOfChatFragment()

        //-- Agrego el fragmento de chat
        lifecycleScope.launch {


            Log.d("-TIMES 3", Date().toString())

            Log.d("REFRESH", "1")

            val transaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.chat_layout, chatFragment!!)
            transaction.addToBackStack("CHAT")
            transaction.commit()

            Log.d("-TIMES 4", Date().toString())

        }


        /**
         * Vinculo los objetos de la pantalla
         */
        //    bindViewObjects()


        return binding.root
    }

    private fun finishSetupOfChatFragment() {
        chatFragment?.setParentReference(binding.chatLayout)
        chatFragment?.setMainParentReference(binding.layoutForVerticalPopups)
        chatFragment?.setOpenButtonReference(binding.fabChat)
    }


    override fun onResume() {
        super.onResume()
        refreshTimes = 0
        (requireActivity() as MainActivity).setTitleBarTitle(R.string.action_events_map)
        AppClass.instance.setCurrentFragment(this)

        Log.d("MAPFRAGMENT", "ONRESUME")
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.action_events_map))
        }
        if (userVisibleHint) {
            if (mMap == null) {
                mapView.getMapAsync(
                    mapSyncCallback
                )
            }
        }

        setupObservers()


        if (eventKeyShowing != null) {

            startObserveEvent(eventKeyShowing!!)
        } else {
            if (AppClass.instance.eventsMap.value?.size == 0) {
                Toast.makeText(requireContext(), "No hay eventos", Toast.LENGTH_SHORT).show()
                mainActivity.onBackPressed()
            } else {
                AppClass.instance.eventsMap.value?.values?.first()?.let { event ->
                    Log.d(
                        "FLOW_CONNECT_VIEWMODEL",
                        "Llamo a la conexcion del evento {${event.event_key}} desde onResume"
                    )

                    startObserveEvent(event.event_key)
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        mapUpdatesJob?.cancel()
        remainingTimeUpdatesJob?.cancel()
        disconnectFromEvent()
        cancelObservers()
        //  AppClass.instance.removeViewFromStack(this)

    }


    private fun setupObservers() {

        viewModel.spinner.observe(this) { value ->
            value.let { show ->
                if (show == true) mainActivityViewModel.showLoader()
                else mainActivityViewModel.hideLoader()
            }
        }

        viewModel.resetMap.observe(this) { newKey ->
            resetMap()
            if (mapConfigs.containsKey(eventKeyShowing)) {
                val config = mapConfigs[eventKeyShowing]!!
                mMap?.moveMapCamera(config.location, config.zoomLevel)
            } else {
                mMap?.animateMapCamera(farthestZoom)
            }
        }

        viewModel.onCloseEventAction.observe(this) { action ->
            when (action) {
                MapSituationFragmentViewModel.OnEventClosedActionsEnum.LOAD_FIRST_EVENT -> {
                    var nextEvent = eventsList.first()
                    viewModel.onEventSelected(nextEvent.event_key)
                }

                MapSituationFragmentViewModel.OnEventClosedActionsEnum.GO_MAIN -> {
                    mainActivity.onBackPressed()
                }

                null -> {}
            }

        }



        AppClass.instance.eventsMap.observe(this) { map ->
            startShimmer()


            eventsHeaderRecyclerAdapter.events.clear()
            eventsList.clear()

            map.values.forEach { event ->
                eventsHeaderRecyclerAdapter.events.add(event)
                eventsList.add(event)
            }
            eventsHeaderRecyclerAdapter.notifyDataSetChanged()
            binding.dotsIndicator.numberOfIndicators = eventsHeaderRecyclerAdapter.events.size
            //      startShimmer()
//------


            AppClass.instance.eventsMap.removeObservers(this)

            AppClass.instance.eventsListFlow.observe(this) { resource ->

                when (resource) {
                    is EventsRepository.DataEvent.OnChildAdded -> {
                        val event = resource.data!!


                        if (!eventsHeaderRecyclerAdapter.isEventExists(event.event_key)) {
                            eventsHeaderRecyclerAdapter.events.add(event)
                            eventsHeaderRecyclerAdapter.notifyItemInserted(
                                eventsHeaderRecyclerAdapter.events.size - 1
                            )
                            binding.dotsIndicator.numberOfIndicators =
                                eventsHeaderRecyclerAdapter.events.size


                            if (viewModel.visibleEventKey.isEmpty()) {

                                viewModel.onEventSelected(event.event_key)
                                chatFragment?.connectToEvent(event.event_key)
                            }
                        }
                    }

                    is EventsRepository.DataEvent.OnChildChanged -> {}
                    is EventsRepository.DataEvent.OnChildMoved -> {}
                    is EventsRepository.DataEvent.OnChildRemoved -> {
                        var existingEvents = AppClass.instance.eventsMap.value?.size ?: 0
                        binding.dotsIndicator.numberOfIndicators = existingEvents
                        /*
                                              if (existingEvents == 0) {
                                                  mainActivity.onBackPressed()
                                              } else {
                                                  //  eventsHeaderRecyclerAdapter.removeEvent(resource.data.event_key)




                                              }
                      */
                    }

                    is EventsRepository.DataEvent.OnError -> TODO()
                }

            }

        }



        viewModel.currentEventKey.observe(this) { eventKey ->
            messages.clear()
            eventKeyShowing = eventKey?.data.toString()
            chatFragment?.setChatroomKey(eventKeyShowing!!)
        }

        viewModel.currentEvent.observe(this) { dataEvent ->
            when (dataEvent) {
                is EventRepository.EventDataEvent.OnChildAdded -> {
                    // arreglar eventsHeaderAdapter.addEvent(dataEvent.data)
                    // if (viewModel.currentEventKey.value == null) {
                    val eventKey = dataEvent.data.event_key
                    viewModel.onEventSelected(eventKey)
                    chatFragment?.connectToEvent(eventKey)
                    // }
                }

                is EventRepository.EventDataEvent.OnChildChanged -> {
                    var pp = 3
                }

                is EventRepository.EventDataEvent.OnChildRemoved -> TODO()
                is EventRepository.EventDataEvent.OnChildMoved -> TODO()
                is EventRepository.EventDataEvent.OnError -> TODO()

            }

        }

        viewModel.isChatOpen.observe(this) { isOpen ->

            if (isOpen == true == true) {


                viewModel.resetUnreadMessages(
                    mainActivityViewModel.userKey.toString(), eventKeyShowing.toString()
                )


            }


        }

        //----  Chatroom

        viewModel.showChatFragment.observe(this) { visible ->
            if (chatFragmentStatus != ChatFragmentStatus.LOADING) {
                if (chatFragmentStatus == ChatFragmentStatus.CLOSED) {
                    animateChatFragmentIn()
                } else {
                    animateChatFragmentOut()
                }
            }
        }

        viewModel.isChatButtonVisible.observe(this) {
            if (it != null) {
                binding.fabOptionsLayout.isVisible = it
            }
        }


        viewModel.isCounterVisible.observe(this) { visible ->
            if (visible == true) {
                binding.elapsedPeriod.visibility = VISIBLE
            } else binding.elapsedPeriod.visibility = GONE

        }

        viewModel.bottomBarVisibilityStatus.observe(this) { visible ->
            if (visible != null) {
                if (visible) mainActivityViewModel.onBottomBarVisibilityOnRequired()
                else mainActivityViewModel.onBottomBarVisibilityOffRequired()
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

                is Resource.Loading -> {
                    //              requireActivity().showLoader()
                    startShimmer()
                }

                is Resource.Success -> {
                    //            requireActivity().hideLoader()
                    stopShimmer()
                    val event = resource.data
                    currentEvent = event
                    chatFragment?.setChatroomKey(event?.event_key.toString())
                    if (eventKeyShowing != event?.event_key.toString()) {
                        Log.d("EVENT_FLOWS", "Debo borrar los datos del evento anterior")
                        clearMap()

                        // b) Escucho a los seguidores del evento.
                        var eventKey = event?.event_key.toString()
                        connectToFollowersFlow(eventKey)
                        connectToChatFlow(eventKey)

                        eventKeyShowing = event?.event_key.toString()
                    }

                    //  Conecta al evento
                    // Conecta a los viewers
                    // conecta al chat

                    viewModel.setEventTimeOfLastView(
                        FirebaseAuth.getInstance().uid.toString(), event?.event_key ?: "error"
                    )
                    // dedicarme a que el siguiente metodo se transforme en MVVM
                    setCurrentEventData(event!!)
                    updateElapsedTimeIndicator()
                    // actualizo la ubicacion del marker del evento
                    if (!mapObjectsMap.containsKey(event.event_key)) // Si el Marker del Evento no existe
                    {
                        var eventMarkerLocation: LatLng? = null

                        if (event.event_location_type == EventLocationType.FIXED.toString()) {

                            eventMarkerLocation = LatLng(
                                (event.location!!.latitude)!!, (event.location!!.longitude)!!
                            )

                            //   markersMap.put(event.event_key, null)
                            val newObject = EventMapObject(
                                event.event_key,
                                MapObjectsType.EVENT_MARKER,
                                null,
                                event.event_type,
                                eventMarkerLocation
                            )
                            mapObjectsMap[event.event_key] = newObject
                            createAndAddMapElement(newObject)

                        }
                    } else {
                        if (markersMap.get(event.event_key) != null) {
                            //             moveEventMarker(event)
                        }
                    }

                    // Actualizo el adapter de los eventos
                    eventsHeaderRecyclerAdapter.setEventData(event)
                    eventKeyShowing = event?.event_key
                }

            }
        }

        mainActivityViewModel.isKeyboardOpen.observe(this) { isOpen ->
            //  viewModel.setIsKeyboardOpen(isOpen)
            if (this@MapSituationFragment.isInLayout) {
                when (isOpen) {
                    true -> {
                        // lo saco pepupi  adjustChatToKeyboardOpen()
                        this@MapSituationFragment.isVisible
                        mainActivityViewModel.onBottomBarVisibilityOffRequired()
                        whenKeyboardOpen()
                    }

                    false -> {

                        whenKeyboardClose()
                    }

                    null -> {}
                }

            }
        }

        //Implementar los mensajes no leidos desde la appclass y levantarlo desde aca


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


    private fun connectToFollowersFlow(eventKey: String) {

        viewModel.eventFollowersFlow(eventKey).observe(this) { resource ->
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
                    /*
                                        when (event) {
                                            is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                                                requireActivity().hideLoader()

                                                val event = resource.data!!
                      */
                    var follower: EventFollower? = null
                    when (event) {
                        is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                            follower = event.data

                            if (follower.is_author == false || currentEvent?.event_location_type == EventLocationType.REALTIME.toString()) {
                                val marker = markersMap[follower.user_key]
                                // si el marker no existe, lo crea y agrega al mapa
                                if (marker == null) {
                                    if (markersMap.containsKey(follower.user_key) == false) {
                                        Log.d(
                                            "FOLLOWERS",
                                            "nuevo follower = " + follower.user_key
                                        )
                                        if (follower.user_key.compareTo("pMDi1MrqDRRSRuyMNGaKqbiZixE3") == 0) {
                                            var pp = 3
                                        }
                                        addNewMarkerToMap(follower)
                                    }
                                } else {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        val newLocation =
                                            LatLng(follower!!.l[0], follower!!.l[1])
                                        addNewPosition(marker, newLocation)
                                    }
                                }
                            }
                            viewModel.eventFlow.value?.data?.let { event ->
                                eventsHeaderRecyclerAdapter.updateFollowerByEventKey(
                                    event.event_key, follower!!
                                )
                            }
                            //---- Actualizo los botones del Header

                        }

                        is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {
                            follower = event.data
                            Log.d(
                                "FOLLOWERS", "cambio en el  follower = " + follower.user_key
                            )
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


                            viewModel.eventFlow.value?.data?.let { event ->
                                eventsHeaderRecyclerAdapter.updateFollowerByEventKey(
                                    event.event_key, follower!!
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
    }


    private fun connectToChatFlow(eventKey: String) {
        chatFragment?.setChatroomKey(eventKey)
        viewModel.onConnectToChatFlow(eventKey).observe(this) { resource ->
            var pp = 3
        }
        AppClass.instance.unreadMessagesFlow.observe(this) { unreadMessages ->
            if (unreadMessages != null) {
                if (unreadMessages.isNotEmpty()) {
                    var unreads = 0
                    if (chatFragmentStatus == ChatFragmentStatus.CLOSED) {
                        unreadMessages.forEach { record ->
                            if (record.chat_room_key.toString() == eventKey) {
                                unreads = record.qty.toInt()
                                return@forEach
                            }
                        }
                        requireContext().playSound(R.raw.message_income)
                    }
                    if (unreads == 0) {
                        binding.unreadCounterText.visibility = GONE
                    } else {
                        binding.unreadCounterText.text = unreads.toString()
                        binding.unreadCounterText.visibility = VISIBLE
                    }
                } else {
                    binding.unreadCounterText.visibility = GONE
                }
            }
        }

    }


    fun startObserveEvent(eventKey: String) {
      if (::viewModel.isInitialized) {
          Log.d("FLOW_CONNECT_VIEWMODEL_INTENTA", "Will change to Event {$eventKey}")
          viewModel.onCurrentEventChange(eventKey)
          chatFragment?.clearMessages()
      }





        return
        //-------------------------------- EVENTOS ---------------------------------------------------------------
        // a) Escucho al Evento.

        // c) Escucho a los mensajes del evento.


        //   viewModel.startObserveEvent(eventKey)
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


    private val lock = Any()

    /**
     * Crea y agrega un marker al mapa
     */
    private fun addNewMarkerToMap(eventFollower: EventFollower) {

        synchronized(lock) {

            val markerKey = eventFollower.user_key
            val markerLocation = LatLng(eventFollower.l[0], eventFollower.l[1])

            var userType = MapObjectsType.COMMON_USER
            if (eventFollower.is_author) {
                userType = MapObjectsType.AUTHOR
            }
            if (markerKey.compareTo(FirebaseAuth.getInstance().uid.toString()) == 0) {
                userType = MapObjectsType.ME
            }
            val mapObject = EventMapObject(
                markerKey,
                userType,
                UserTypesEnum.valueOf(eventFollower.user_type.toString()),
                eventFollower.profile_image_path,
                markerLocation
            )
            mapObjectsMap[markerKey] = mapObject
            Log.d(
                "MARKERS", " Creo el Marker para el objeto ${markerKey}"
            )
            createAndAddMapElement(mapObject)


        }

    }

    /**
     * Uso actual
     * Desplaza un marker en el mapa
     */
    suspend fun moveMarker(marker: Marker) {

        //        marcar el marker cuando se esta moviendo y si se esta moviendo
        //        que solo agregue a la cola
        //        y sino que se ejecute
        marker.turnMovingOn()
        val channel = pendingPositions[marker]
        channel?.let {
            var lastPosition: LatLng? = null
            for (position in it) {
                marker.getKey()?.let { key ->
                    marker.moveMarkerSmoothly(position, false)
                    markersMap["ripple_" + key]!!.moveMarkerSmoothly(position, false)
                }
                while (BigDecimal(marker.position.latitude).setScale(
                        7, RoundingMode.HALF_UP
                    ) != BigDecimal(position.latitude).setScale(
                        7, RoundingMode.HALF_UP
                    ) || BigDecimal(marker.position.longitude).setScale(
                        7, RoundingMode.HALF_UP
                    ) != BigDecimal(position.longitude).setScale(7, RoundingMode.HALF_UP)
                ) {

                    delay(100)
                }
                marker.position = position


                lifecycleScope.launch(Dispatchers.Main) {
                    if (currentCameraMode.mode != CameraModesEnum.FREE_MODE) {
                        updateCameraAccordingMode(currentCameraMode)
                    }
                }

            }
            it.cancel()  // Cancela el Channel
            //marker.turnMovingOff()
        }
        pendingPositions.remove(marker)  // Elimina el registro de pendingPositions
    }


    private fun cancelObservers() {
        viewModel.eventFlow.removeObservers(this)
        // viewModel.viewersUpdatesFlow.removeObservers(this)
        //jobViewersFlowObserver?.cancel()
        viewModel.currentEvent.removeObservers(this)
        viewModel.resetMap.removeObservers(this)
        viewModel.subscriptedEvents.removeObservers(this)
        viewModel.showChatFragment.removeObservers(this)
    }


    private suspend fun getEventMarkerLatLng(event: Event): LatLng? {
        if (event.event_location_type == EventLocationType.FIXED.toString()) {
            return LatLng(event.location?.latitude!!, event.location?.longitude!!)
        } else {
            var authorLocation: LatLng? = null
            viewModel.followersList.forEach { follower ->
                if (follower.user_key == event.author_key) {
                    authorLocation = LatLng(follower.l[0], follower.l[1])
                    return@forEach
                }
            }
            return authorLocation
        }
    }


    val pendingPositions = mutableMapOf<Marker, Channel<LatLng>>()


    fun generateMarkerRipple(key: String, latLng: LatLng) {
        val rippleOptions = MarkerOptions().position(latLng).icon(
            BitmapDescriptorFactory.fromBitmap(
                requireContext().getBitmapFromVectorDrawable(R.drawable.ripple_svg_1)
                    .scale(128, 128)
            )
        ).anchor(.5f, .5f)

        val ripple = mMap?.addMarker(rippleOptions)
        val rippleBundle = Bundle()
        rippleBundle.putString("key", "ripple_" + key)
        ripple?.tag = rippleBundle
        markersMap.put("ripple_" + key, ripple!!)
        //markersMap[key] = ripple
    }


    override fun onStop() {
        super.onStop()

        clearMap()
    }

    private fun setupUI() {

        //--- adapter de eventos

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
                    if (viewModel.visibleEventKey != event.event_key) {

                        startShimmer()


                        startObserveEvent(event.event_key)

                    }
                }
            }
        })



        binding.dotsIndicator.attachTo(binding.eventsRecyclerView, true)
        //binding.dotsIndicator.numberOfIndicators = eventsHeaderRecyclerAdapter.itemCount


        binding.elapsedPeriod.maxValue = 180f
        setupToolbar()
        closeCameraModeSelector()
        binding.elapsedPeriod.visibility = GONE

        chatFragment?.setViewReference(binding.fabChat)
        binding.cameraModeSection.setOnClickListener {
            toggleCameraModeSelector(isCameraSelectorOpen)
        }

        setupFABs()

        setupCameraModesUI()
    }

    private fun setupFABs() {
        binding.fabChat.setOnClickListener {


            //     if (chatFragmentStatus != null) {
            if (chatFragmentStatus == ChatFragmentStatus.CLOSED) {
                //    showChatFragment()
                viewModel.setMessageFragmentMode(ChatWindowStatus.NORMAL)
                animateChatFragmentIn()
            } else {
                //   hideChatFragment()
                animateChatFragmentOut()
            }
            //   }
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
        markersMap.clear()
        mapObjectsMap.clear()
        mMap?.clear()
        mMap?.animateMapCamera(farthestZoom)
    }

    private fun moveMarkerAlongRoute(
        marker: Marker, points: ArrayList<LatLng>, callback: MarkerAnimationCallback
    ) {

        //     MarkerAnimationLot().animateLine(points, mMap!!, marker, context, callback);

    }


    private fun drawMarkerPath(markerKey: String) {

        return
        requireActivity().runOnUiThread(object : Runnable {
            override fun run() {
                Log.d("POLYLINES", "dibujo el camino de $markerKey")
                Log.d("POLYLINES", "------------ 1 -------------")
                var path: ArrayList<LatLng> = ArrayList<LatLng>()
                val polyLine: Polyline?

                polyLine = if (mapRoutes.containsKey(markerKey)) {
                    mapRoutes[markerKey]!!
                } else {
                    mMap?.addPolyline(PolylineOptions())
                }
                mapObjectsMap[markerKey]?.previousLocations?.values?.let {

                    polyLine?.points?.clear()
                    val movementPoints =
                        mapObjectsMap[markerKey]?.previousLocations?.values!!.toTypedArray()
                    val points: ArrayList<LatLng> = ArrayList<LatLng>()
                    movementPoints.forEach { movement ->
                        points.add(LatLng(movement.latitude!!, movement.longitude!!))
                    }
                    polyLine?.points = points
                    mapRoutes.put(markerKey, polyLine!!)
                }
            }
        })


    }


    private fun updateViewersSection(viewers: HashMap<String, EventFollower>?) {/*
        qqq
                viewers?.forEach { _, viewer ->
                    val index = viewersAdapter.getData().indexOf(viewer)
                    if (index == -1) {
                        viewersAdapter.getData().add(viewer)
                    }
                }
                viewersAdapter.notifyDataSetChanged()

         */
    }

    private fun setCurrentEventData(event: Event) {
        usersParticipatingFragment.eventData = event
        alreadyCalledFragment.eventData = event
        usersGoingFragment.eventData = event
        //eventsHeaderAdapter.setEventData(event)
        //  bottomSheetEventInfoAdapter.setData(event)
        currentEvent = event
        usersParticipatingFragment.setEventKey(event.event_key)
        alreadyCalledFragment.setEventKey(event.event_key)
        usersGoingFragment.setEventKey(event.event_key)
        broadcastEventChanges(event)

    }

    private fun broadcastEventChanges(event: Event): Boolean {
        //   Log.d(LocationService.TAG, "Emito el mensaje de cambios en el evento")

        val intent = Intent("CHANGES_IN_EVENT")
        intent.setPackage(requireContext().packageName)
        val data = event
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
            intent.putExtra(
                "data", data
            )
        )
        return true
    }


    private fun setupCameraModesUI() {

        binding.goToMyPositionButton.imageTintList = ColorStateList.valueOf(
            getColor(
                requireContext(), R.color.colorPrimary
            )
        )
        binding.goToMyPositionButton.setOnClickListener {
            setCameraMode(CameraModesEnum.MY_LOCATION)
        }

        binding.centerInEventLocationButton.imageTintList = ColorStateList.valueOf(
            getColor(
                requireContext(), R.color.colorPrimary
            )
        )
        binding.centerInEventLocationButton.setOnClickListener {
            setCameraMode(CameraModesEnum.CENTER_IN_EVENT_LOCATION)
        }

        binding.zoomToFitButton.imageTintList = ColorStateList.valueOf(
            getColor(
                requireContext(), R.color.colorPrimary
            )
        )
        binding.zoomToFitButton.setOnClickListener {
            setCameraMode(CameraModesEnum.SHOW_ALL_MARKERS)
        }

        val viewersButtonDrawableTop = requireContext().resizeDrawable(
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_eye_white)!!,
            toolbarIconWitdh,
            toolbarIconHeight
        )
    }


    //------ FAB
    private fun closeSubMenusFab() {

        //  binding?.fabChat?.setVisibility(View.INVISIBLE)
        binding.fabMain.setImageResource(R.drawable.ic_squares)
        fabExpanded = false
        //    subscribeToUnreadMessages()
    }

    //Opens FAB submenus
    private fun openSubMenusFab() {
        //binding?.fabChat?.setVisibility(VISIBLE)
        //Change settings icon to 'X' icon
        binding.fabMain.setImageResource(R.drawable.ic_close)
        fabExpanded = true
        //    unSubscribeToUnreadMessages()

    }


    private fun resetUnreadMessages() {
        Log.d("CHAT", "Pongo a 0 los mensajes leidos para este Room")

        val resetUnreadsCallback = object : OnCompleteCallback {

            override fun onError(exception: Exception) {
                super.onError(exception)
                requireActivity().showErrorDialog(exception.localizedMessage)
            }
        }

        requireActivity().showSnackBar(
            binding.root, "Implementar resetUnreadMessagesByRoom"
        )/*
        ChatWSClient.instance.resetUnreadMessagesByRoom(
            currentEvent?.event_key!!, resetUnreadsCallback
        )
        */

    }

    private fun showChatFragment() {
        animateChatFragmentIn()
    }

    private var chatFragmentStatus: ChatFragmentStatus? = ChatFragmentStatus.CLOSED


    private fun prepareChatFragmentLayout(): ConstraintLayout.LayoutParams {

        //var defaultViewSettings = ViewSettingsModel()
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        // Button Position
        val loc = IntArray(2)
        binding.fabChat.getLocationOnScreen(loc)
        binding.fabChat.getLocationInWindow(loc)
        val x = loc[0]
        val y = loc[1]

        val params: ConstraintLayout.LayoutParams =
            binding.chatLayout.layoutParams as ConstraintLayout.LayoutParams
        params.rightMargin = width - x
        params.bottomMargin = 40.dp
        return params
    }


    var listener: FragmentManager.OnBackStackChangedListener? = null

    override fun hideChatFragment() {
        // restoreChatFragmentToRegularSize()
        //requireActivity().supportFragmentManager.popBackStack()


        val transaction: FragmentTransaction =
            requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter_from_left,
            R.anim.exit_to_right,
            R.anim.enter_from_right,
            R.anim.exit_to_left
        )
        transaction.remove(chatFragment!!)
        transaction.addToBackStack(null)
        transaction.commit()

// Agrega el listener a la animación

        listener = FragmentManager.OnBackStackChangedListener {
            // Aquí puedes poner el código que quieres que se ejecute después de la animación
            binding.fabChat.setOnClickListener {
                requireActivity().supportFragmentManager.removeOnBackStackChangedListener(
                    listener!!
                )
                if (chatFragment?.isVisible == false) {
                    showChatFragment()
                }
            }

        }
        requireActivity().supportFragmentManager.addOnBackStackChangedListener(listener!!)

    }


    private fun adjustChatToKeyboardOpen() {
        val chatLayout: FrameLayout = binding.chatLayout
        //--------------------------------------------
        // Obtén la instancia del ConstraintLayout padre
        val parentConstraintLayout: ConstraintLayout = binding.layoutForVerticalPopups
        // Configura el ancho y alto del FrameLayout para que ocupe todo el ancho y alto disponible
        val layoutParams = chatLayout.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
        layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT

        // Establece el margen inferior deseado (puedes ajustar esto según tus necesidades)
        layoutParams.topMargin = 0
        layoutParams.bottomMargin = -10.dp
        layoutParams.marginEnd = 0
        chatLayout.layoutParams = layoutParams

    }


    //-----------------------------

    private fun isAnyFragmentVisible(): Boolean {
        return (requireActivity().supportFragmentManager.findFragmentByTag("ALREADY_CALLED") != null && requireActivity().supportFragmentManager.findFragmentByTag(
            "ALREADY_CALLED"
        )?.isVisible ?: false) || (requireActivity().supportFragmentManager.findFragmentByTag(
            "USERS_GOING"
        ) != null && requireActivity().supportFragmentManager.findFragmentByTag(
            "USERS_GOING"
        )?.isVisible ?: false) || (requireActivity().supportFragmentManager.findFragmentByTag(
            "USERS_PARTICIPATING"
        ) != null && requireActivity().supportFragmentManager.findFragmentByTag(
            "USERS_PARTICIPATING"
        )?.isVisible ?: false)
    }

    override fun showUsersParticipatingFragment() {

        if (!isAnyFragmentVisible()) {

            binding.fabChat.visibility = GONE
            val transaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(
                R.id.layout_for_vertical_popups, usersParticipatingFragment, "USERS_PARTICIPATING"
            )
            transaction.addToBackStack("USERS_PARTICIPATING")
            transaction.commit()
        }
    }

    override fun showUsersWhoCalledFragment() {
        if (!isAnyFragmentVisible()) {

            binding.fabChat.visibility = GONE
            val transaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(
                R.id.layout_for_vertical_popups, alreadyCalledFragment, "ALREADY_CALLED"
            )
            transaction.addToBackStack("ALREADY_CALLED")
            transaction.commit()
        }
    }

    override fun showUsersGoingFragment() {
        if (!isAnyFragmentVisible()) {

            binding.fabChat.visibility = GONE
            val transaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(
                R.id.layout_for_vertical_popups, usersGoingFragment, "USERS_GOING"
            )
            transaction.addToBackStack("USERS_GOING")
            transaction.commit()
        }
    }

    override fun onFragmentFromBottomClose() {
        if (isAnyFragmentVisible()) {

            binding.fabChat.visibility = VISIBLE
            val transaction: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            if (requireActivity().supportFragmentManager.findFragmentByTag("ALREADY_CALLED") != null && requireActivity().supportFragmentManager.findFragmentByTag(
                    "ALREADY_CALLED"
                )?.isVisible == true
            ) {
                transaction.remove(alreadyCalledFragment)
            } else if (requireActivity().supportFragmentManager.findFragmentByTag("USERS_GOING") != null && requireActivity().supportFragmentManager.findFragmentByTag(
                    "USERS_GOING"
                )?.isVisible == true
            ) {
                transaction.remove(usersGoingFragment)
            } else if (requireActivity().supportFragmentManager.findFragmentByTag("USERS_PARTICIPATING") != null && requireActivity().supportFragmentManager.findFragmentByTag(
                    "USERS_PARTICIPATING"
                )?.isVisible == true
            ) {
                transaction.remove(usersParticipatingFragment)
            }

            transaction.commit()
        }
    }


    //-------------


    private fun setupBottomSheetLayout() {
        bottomSheetLayout = binding.root.findViewById(R.id.bottom_sheet_layout)!!
        bottomSheetLayout.maxHeight = (resources.displayMetrics.heightPixels * 0.8).toInt()
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
    }


    //----- DataAccess
    fun disconnectFromEvent() {
        viewModel.disconect()
    }


    // -----MAP Management
    private fun clearMap() {
        mEventsMarkersMap?.clear()
        markersMap.clear()
        mapObjectsMap?.clear()
        mapRoutes.clear()
        if (mMap != null) {
            mMap?.clear()
        }
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


    fun getEventFollowedByKey(eventKey: String): EventFollowed? {

        getEventsFollowed().forEach { event ->
            if (event.event_key == eventKey) {
                return event
            }
        }

        return null
    }

//--- DATA HANDLERS


    fun resolveOnEventRemoved(eventKey: String) {
        if (currentEvent != null) {
            if (eventKey == currentEvent?.event_key) {
                val nextEvent = nextEvent
                if (nextEvent == null) {
                    clearMap()
                    hideEventsToolbar()
                  //  mainActivity.switchToModule(0, "home")
                    mainActivity.goHome()
                } else {
//                    selectEvent(nextEvent.event_key)
                    Log.d(
                        "FLOW_CONNECT_VIEWMODEL",
                        "Llamo a la conexcion del evento {$eventKey} desde resolveOnEventRemoved"
                    )

                    startObserveEvent(nextEvent.event_key)
                }
            }
        }


    }

    /*
    fun startObserveEvent(eventKey: String) {
        Log.d(
            "FLOW_CONNECT_VIEWMODEL",
            "Llamo a la conexcion del evento {$eventKey} desde startObserveEvent"
        )
        viewModel.startObserveEvent(eventKey)
    }
    */

    private fun closeBottomSheet() {
        usersParticipatingFragment.closeBottomSheet()
        //  usersGoingFragment.closeBottomSheet()
        alreadyCalledFragment.closeBottomSheet()
    }


//----------------- Markers en el Mapa

    private suspend fun resizeMarkers(zoom: Float) {
        lifecycleScope.launch(Dispatchers.Default) {
            //   var factor = ((zoom * iconHeight) / markerZoomDefault) / markerZoomDefault

            var newHeight = mMap?.calculateMarkerSize(markerIconHeight, zoom)!!
            val factor = (zoom / markerZoomDefault)


        }

    }

    private fun getVehicleMarkerOptions(
        ll: LatLng, drawableId: Int, width: Int, height: Int
    ): MarkerOptions {
        val bitmapFactory = BitmapFactory.decodeResource(resources, drawableId)
        val bitmap: Bitmap = Bitmap.createScaledBitmap(
            bitmapFactory, width, height, false
        )
        val bitmapDescriptor2 = BitmapDescriptorFactory.fromBitmap(bitmap)
        val viewerMarker: MarkerOptions =
            MarkerOptions().position(ll).flat(true).icon(bitmapDescriptor2)
        return viewerMarker
    }

    private fun getUserMarkerOptions(
        ll: LatLng,
        userKey: String,
        userName: String,
        fileName: String,
        isAuthor: Boolean,
        callback: OnCompleteCallback
    ) {


        //   val bitmapDescriptor = BitmapDescriptorFactory.fromResource(drawableId)
        val viewerMarker: MarkerOptions = MarkerOptions().position(ll).flat(true)

        //----------------

        lifecycleScope.launch(Dispatchers.IO) {

/*
            val storageReference = mainActivityViewModel.getResourceUrl(
                AppConstants.PROFILE_IMAGES_STORAGE_PATH, userKey, fileName
            )
*/


            val localStorageFolder = AppConstants.PROFILE_IMAGES_STORAGE_PATH + userKey + "/"
            val fileLocation =
                FirebaseStorage.getInstance().getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                    .child(userKey).child(fileName)
                    .downloadUrlWithCache(AppClass.instance, localStorageFolder)

            Log.d("GLIDEAPP", "10")



            GlideApp.with(this@MapSituationFragment).asBitmap().load(fileLocation)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        var markerIcon: Bitmap? = null
                        if (!isAuthor) {
                            markerIcon = getMarkerFromView(
                                requireContext(),
                                R.layout.custom_marker_pin_viewer_circle_point2,
                                resource,
                                0,
                                requireContext().resources.getDimension(R.dimen.marker_user_image_size),
                                0
                            )
                        } else {
                            markerIcon = getMarkerFromView(
                                requireContext(),
                                R.layout.custom_marker_pin_viewer_circle_point_author,
                                resource,
                                0,
                                requireContext().resources.getDimension(R.dimen.marker_user_image_size),
                                0
                            )
                        }

                        viewerMarker.icon(
                            BitmapDescriptorFactory.fromBitmap(
                                markerIcon!!
                            )
                        )
                        callback.onComplete(true, viewerMarker)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })

        }

        /*
                val storageReference =
                    FirebaseStorage.getInstance().getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                        .child(userKey).child(fileName)
                //  .getReference(fileName)
        */


    }


    private suspend fun generateNewMarkerForEvent(
        key: String,
        type: MapObjectsType,
        userType: UserTypesEnum?,
        resourceLocation: Any?,
        location: LatLng
    ): MarkerOptions = suspendCancellableCoroutine { continuation ->
        val markerPosition = location
        var zoomFactor: Float = 1f
        zoomFactor = if (mMap!!.cameraPosition.zoom <= 10) {
            .4f
        } else if (mMap!!.cameraPosition.zoom > 10 && mMap!!.cameraPosition.zoom <= 16) {
            //var auxFact = mMap!!.cameraPosition.zoom / 16
            mMap!!.cameraPosition.zoom / 16
        } else 1f

        var newMarker: MarkerOptions? = null

        when (type) {
            MapObjectsType.EVENT_MARKER -> {
                val markerOptions = generateEventMarker(
                    key, EventTypesEnum.valueOf(resourceLocation as String), location
                )
                continuation.resume(markerOptions)
            }

            MapObjectsType.AUTHOR -> {
                lifecycleScope.launch {
                    val markerOptions = mMap!!.getUserMarkerOptions(
                        markerPosition, key, resourceLocation as String, true
                    )
                    continuation.resume(markerOptions)
                }

            }

            MapObjectsType.COMMON_USER -> {
                when (userType) {
                    UserTypesEnum.COMMON_USER -> {
                        lifecycleScope.launch {
                            val markerOptions = mMap!!.getUserMarkerOptions(
                                markerPosition, key, resourceLocation as String, false
                            )
                            continuation.resume(markerOptions)
                        }


                    }

                    UserTypesEnum.POLICE_CAR -> {
                        var iconWidth = 18.px
                        iconWidth = (zoomFactor * iconWidth).toInt()
                        Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                        var iconAspectRatio = "v,43.70:100"
                        val dimensionsMap =
                            MathUtils.calculateNewSizeWithRatio(iconWidth, "H,43.70:100")
                        Log.d(
                            "ZOOM_VARIATION", "dimensionsMap = $dimensionsMap"
                        )
                        val markerOptions = getVehicleMarkerOptions(
                            markerPosition,
                            R.drawable.vehicle_police_car,
                            dimensionsMap["width"].toString().toInt(),
                            dimensionsMap["height"].toString().toInt()
                        )
//                        callback.onComplete(true, markerOptions)
                        continuation.resume(markerOptions)
                    }

                    UserTypesEnum.AMBULANCE -> {
                        var iconWidth = 18.px
                        iconWidth = (zoomFactor * iconWidth).toInt()

                        Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                        var iconAspectRatio = "v,43.70:100"
                        val dimensionsMap =
                            MathUtils.calculateNewSizeWithRatio(iconWidth, "H,48.97:100")
                        Log.d(
                            "ZOOM_VARIATION", "dimensionsMap = $dimensionsMap"
                        )
                        val markerOptions = getVehicleMarkerOptions(
                            markerPosition,
                            R.drawable.vehicle_ambulance,
                            dimensionsMap["width"].toString().toInt(),
                            dimensionsMap["height"].toString().toInt()
                        )
//                        callback.onComplete(true, markerOptions)
                        continuation.resume(markerOptions)

                    }

                    UserTypesEnum.FIRE_TRUCK -> {

                        var iconWidth = 18.px
                        iconWidth = (zoomFactor * iconWidth).toInt()
                        Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                        var iconAspectRatio = "v,43.70:100"
                        val dimensionsMap =
                            MathUtils.calculateNewSizeWithRatio(iconWidth, "H,33.11:100")
                        Log.d(
                            "ZOOM_VARIATION", "dimensionsMap = $dimensionsMap"
                        )
                        val markerOptions = getVehicleMarkerOptions(
                            markerPosition,
                            R.drawable.vehicle_firetruck,
                            dimensionsMap["width"].toString().toInt(),
                            dimensionsMap["height"].toString().toInt()
                        )
//                        callback.onComplete(true, markerOptions)
                        continuation.resume(markerOptions)
                    }

                    else -> {}
                }

            }

            else -> {}
        }


    }


    /***
     * Se ejecuta despues que retorna de llamar a la autoridad
     *
     * */

    fun onAfterCallIntent() {

        val onYesCallback: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (context is ViewersActionsCallback) {
                    val callback = object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {
// NO HAGO NADA
                        }

                    }
                    (context as ViewersActionsCallback).onCallAuthorityStateChanged(
                        currentEvent?.event_key.toString(),
                        FirebaseAuth.getInstance().uid.toString(),
                        callback
                    )


                }
            }
        }

        requireActivity().showErrorDialog(
            getString(R.string.how_did_you_go),
            getString(R.string.were_you_able_to_communicate_with_the_authority),
            getString(R.string.yes),
            onYesCallback
        )

    }

    //--------------- VIEWERS

    //--------------- CAMERAS
    private fun toggleCameraModeSelector(isOpen: Boolean) {
        if (!isOpen) openCameraModeSelector()
        else {
            setCameraMode(currentCameraMode.mode!!, currentCameraMode.additionalKey)
            closeCameraModeSelector()
        }
        isCameraSelectorOpen = !isOpen
    }

    private fun closeCameraModeSelector() {
        //      binding?.infoButton?.visibility = VISIBLE
        //      binding?.viewersButton?.visibility = VISIBLE
        binding.cameraModeSection.visibility = GONE
        //      binding?.toolbarCardview?.invalidate()
    }

    private fun openCameraModeSelector() {
        //    binding?.infoButton?.visibility = GONE
        //    binding?.viewersButton?.visibility = GONE

        val drawableTopBackArrowIcon = requireContext().resizeDrawable(
            AppCompatResources.getDrawable(
                requireContext(), R.drawable.ic_back_arrow_white
            )!!, toolbarIconWitdh, toolbarIconHeight
        )

        binding.cameraModeButton.setImageDrawable(drawableTopBackArrowIcon)/*
            binding?.cameraModeButton?.setCompoundDrawablesWithIntrinsicBounds(
                null,
                drawableTopBackArrowIcon,
                null,
                null
            )
    */

        binding.selectedCameraModeHint.setText(R.string.go_back)
        binding.cameraModeButton.imageTintList = ColorStateList.valueOf(
            getColor(
                requireContext(), R.color.white
            )
        )

        binding.cameraModeSection.visibility = VISIBLE
        // binding?.toolbarCardview?.invalidate()
    }


    fun setCameraMode(cameraMode: CameraModesEnum) {
        setCameraMode(cameraMode, "")
    }

    fun setCameraMode(mode: CameraModesEnum, additionalKey: String?) {
        userFollowed = null
        currentCameraMode = CameraMode(mode)

        if (mode == CameraModesEnum.FOLLOW_USER) {
            if (currentEvent?.viewers?.containsKey(additionalKey) == true) {
                currentCameraMode = CameraMode(mode, additionalKey)
            } else {
                setCameraMode(CameraModesEnum.SHOW_ALL_MARKERS)
            }

        }

        updateSelectedCameraModeButton(mode, additionalKey)
        closeCameraModeSelector()
        currentCameraMode.let { modeObject ->
            currentCameraMode = modeObject
            updateCameraAccordingMode(currentCameraMode)
        }
    }

    private fun updateSelectedCameraModeButton(
        mode: CameraModesEnum, additionalKey: String?
    ) {
        when (mode) {
            CameraModesEnum.FREE_MODE -> {
                val drawableTop = requireContext().resizeDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.ic_toolbar_camera_free_move
                    )!!, toolbarIconWitdh, toolbarIconHeight
                )

                binding.cameraModeButton.setImageDrawable(drawableTop)

                binding.selectedCameraModeHint.setText(R.string.free)
            }

            CameraModesEnum.MY_LOCATION -> {

                val drawableTop = requireContext().resizeDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.ic_toolbar_camera_my_position
                    )!!, toolbarIconWitdh, toolbarIconHeight
                )
                binding.cameraModeButton.setImageDrawable(drawableTop)
                binding.selectedCameraModeHint.setText(R.string.to_me)
            }

            CameraModesEnum.CENTER_IN_EVENT_LOCATION -> {
                val drawableTop = requireContext().resizeDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.ic_toolbar_camera_event_centered
                    )!!, toolbarIconWitdh, toolbarIconHeight
                )
                binding.cameraModeButton.setImageDrawable(drawableTop)
                binding.selectedCameraModeHint.setText(R.string.center_in_event_location)
            }

            CameraModesEnum.SHOW_ALL_MARKERS -> {
                val drawableTop = requireContext().resizeDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.ic_toolbar_camera_all_in_map
                    )!!, toolbarIconWitdh, toolbarIconHeight
                )
                binding.cameraModeButton.setImageDrawable(drawableTop)
                binding.selectedCameraModeHint.setText(R.string.all)
            }

            CameraModesEnum.FOLLOW_USER -> {
            }
        }

        binding.cameraModeButton.imageTintList = ColorStateList.valueOf(
            getColor(
                requireContext(), R.color.white
            )
        )

    }


    private fun updateCameraAccordingMode(modeSelected: CameraMode) {

        Log.d("CAMERA_MODE", modeSelected.mode?.name.toString())
        when (modeSelected.mode) {
            CameraModesEnum.FREE_MODE -> {
            }

            CameraModesEnum.MY_LOCATION -> {
                goToMyLocationInMap()
            }

            CameraModesEnum.CENTER_IN_EVENT_LOCATION -> {
                centerInEventLocation()
            }

            CameraModesEnum.SHOW_ALL_MARKERS -> {
                zoomToFitAllMarkers()
            }

            CameraModesEnum.FOLLOW_USER -> {
                centerInUserPosition(modeSelected.additionalKey.toString())
            }

            null -> TODO()
        }
    }

    var lastPolygon: Polygon? = null
    var circles = ArrayList<Circle>()
    private fun centerInEventLocation() {

        lifecycleScope.launch(Dispatchers.Main) {

            var zoomBounds = LatLngBounds.builder()
            circles?.forEach { circle ->
                circle.remove()
            }


            var eventPosition: LatLng? = null
            var myLocation = AppClass.instance.lastLocation.value
            var myPosition: LatLng = LatLng(myLocation!!.latitude, myLocation.longitude)


            currentEvent?.let { event: Event ->
                if (event.event_location_type == EventLocationType.FIXED.name) {
                    eventPosition = LatLng(event.location?.latitude!!, event.location?.longitude!!)
                } else {
                    viewModel.getFollower(event.author!!.author_key)?.let { follower ->
                        eventPosition = LatLng(follower.l[0], follower.l[1])
                    }
                }

                var existingBounds = LatLngBounds.builder()


                // Agrego la posicion del usuario actual porque no se muestra en el mapa
                existingBounds.include(myPosition)


                // Agrego el resto de los markers que no sean el ripple
                markersMap.values.forEach { marker ->
                    if (marker?.getKey()?.startsWith("ripple_") == false) {
                        existingBounds.include(marker?.position!!)
                    }
                }

                var elementsBounds = existingBounds.build()

                var centerLocation = Location("center").apply {
                    latitude = eventPosition!!.latitude
                    longitude = eventPosition!!.longitude
                }

                // 1 - calculo el punto horizonal derecho y el punto horizontal izquierdo

                var horDerLat = LatLng(eventPosition!!.latitude, elementsBounds.northeast.longitude)
                var horDerLocation = Location("horDer").apply {
                    latitude = eventPosition!!.latitude
                    longitude = elementsBounds.northeast.longitude
                }


                var horIzqLat = LatLng(eventPosition!!.latitude, elementsBounds.southwest.longitude)
                var horIzqLocation = Location("horIzq").apply {
                    latitude = eventPosition!!.latitude
                    longitude = elementsBounds.southwest.longitude
                }

                var farthestLongitude =
                    if (centerLocation.distanceTo(horDerLocation) > centerLocation.distanceTo(
                            horIzqLocation
                        )
                    ) {
                        horDerLocation.toLatLngGoogle()
                    } else {
                        horIzqLocation.toLatLngGoogle()
                    }
                var puntoHorizontalNegativo =
                    eventPosition?.calcularLongitudNegativa(farthestLongitude!!)

                // agrego a los bounds su punto original y su punto negativo
                existingBounds.include(farthestLongitude)
                existingBounds.include(puntoHorizontalNegativo!!)


                //--------------------------
                var vertSupLat =
                    LatLng(elementsBounds.northeast.latitude, eventPosition!!.longitude)
                var vertSupLocation = Location("vertSup").apply {
                    latitude = elementsBounds.northeast.latitude
                    longitude = eventPosition!!.longitude
                }
                var vertInfLat =
                    LatLng(elementsBounds.southwest.latitude, eventPosition!!.longitude)

                var vertInfLocation = Location("vertInf").apply {
                    latitude = elementsBounds.southwest.latitude
                    longitude = eventPosition!!.longitude


                }

                var farthestLatitude =
                    if (centerLocation.distanceTo(vertSupLocation) > centerLocation.distanceTo(
                            vertInfLocation
                        )
                    ) {
                        vertSupLocation.toLatLngGoogle()
                    } else {
                        vertInfLocation.toLatLngGoogle()
                    }

                var puntoVerticalNegativo =
                    eventPosition?.calcularLatitudNegativa(farthestLatitude!!)


                // agrego a los bounds su punto original y su punto negativo
                existingBounds.include(farthestLatitude)
                existingBounds.include(puntoVerticalNegativo!!)

/*
                circles.add(
                    mMap?.drawControlCircle(
                        myPosition, getColor(requireContext(), R.color.material_green500), 1000.0
                    )!!
                )

                circles.add(mMap?.drawControlCircle(farthestLongitude, R.color.red)!!)
                circles.add(mMap?.drawControlCircle(puntoHorizontalNegativo!!, R.color.red)!!)
                circles.add(mMap?.drawControlCircle(farthestLatitude, R.color.blue)!!)
                circles.add(mMap?.drawControlCircle(puntoVerticalNegativo!!, R.color.blue)!!)
*/
                elementsBounds = existingBounds.build()

                if (lastPolygon != null) {
                    lastPolygon?.remove()
                }

                lastPolygon = mMap?.drawBounds(elementsBounds)

                mMap?.zoomToBounds(elementsBounds)

            }
        }
    }

    private fun centerInUserPosition(userFollowed: String) {
        val viewer = currentEvent?.viewers?.get(userFollowed)
        if (viewer != null) {
            val userLatLng = LatLng(viewer.l[0], viewer.l[1])
            Log.d("CAMARA", userLatLng.toString())
            val camera = CameraUpdateFactory.newLatLng(userLatLng)
            mMap!!.animateCamera(camera)
        }
    }


    private fun goToMyLocationInMap() {

        mMyLocation?.let { latLng ->
            val camera = CameraUpdateFactory.newLatLng(latLng)
            //mMap!!.moveCamera(camera)
            mMap!!.animateCamera(camera)
        }
    }

    private fun zoomToFitAllMarkers() {
        val positions = ArrayList<LatLng>()
        mapObjectsMap.values.forEach { objectMap ->
            positions.add(objectMap.latLng)
        }
        // agrego la ubicacion actual.
        SmartLocation.with(context).location().oneFix().start { location ->
            val latLng: LatLng = LatLng(location.latitude, location.longitude)
            positions.add(latLng)

        }
        val mapCanvas = mapView.requireView()

        val mapWidth = mapCanvas.width
        var mapHeight = mapCanvas.height
        mMap?.zoomToFitMarkers(positions, mapWidth, mapWidth, null)
    }


    // TODO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {

            //  if (requestCode === com.github.dhaval2404.imagepicker.ImagePicker.REQUEST_CODE) {

        }
    }


    /**
     * Genera un marker para el tipo de evento que selecciono.
     */
    private fun generateEventMarker(
        eventKey: String, eventType: EventTypesEnum, latLng: LatLng
    ): MarkerOptions {
        val eventMarker = MarkerOptions().position(latLng)

        val eventIcon = generateEventIcon(eventType)
        if (eventIcon != null) {
            eventMarker.icon(BitmapDescriptorFactory.fromBitmap(eventIcon))
        }
        return eventMarker
    }

    // Optimizado
    private fun generateEventIcon(
        eventType: EventTypesEnum, imageHeight: Int = 48.px
    ): Bitmap? {
        var eventIcon: Bitmap? = null
        when (eventType) {
            EventTypesEnum.SEND_POLICE -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_police
                )
            }

            EventTypesEnum.SEND_AMBULANCE -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_ambulance
                )
            }

            EventTypesEnum.SEND_FIREMAN -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_fire
                )
            }

            EventTypesEnum.ROBBER_ALERT -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_robbery
                )
            }

            EventTypesEnum.SCORT_ME -> {
                eventIcon = BitmapFactory.decodeResource(
                    resources, R.drawable.ic_destination_flag
                )
            }

            EventTypesEnum.KID_LOST -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_kid_lost
                )
            }

            EventTypesEnum.PET_LOST -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_pet_lost
                )
            }

            EventTypesEnum.PERSECUTION -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_persecution
                )
            }

            EventTypesEnum.MECANICAL_AID -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_mechanical_aid
                )
            }

            EventTypesEnum.PANIC_BUTTON -> {
                eventIcon = BitmapFactory.decodeResource(
                    requireContext().resources, R.drawable.marker_sos
                )
            }

            else -> {
            }

        }

        eventIcon?.let { bitmap ->
            val newWidth = (imageHeight * bitmap.width) / bitmap.height
            eventIcon = Bitmap.createScaledBitmap(bitmap, newWidth, imageHeight, false)
        }

        return eventIcon
    }


    private fun remainingTimeStartRepeatingJob(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (NonCancellable.isActive) {
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


    private fun createAndAddMapElement(currentObject: EventMapObject) {
        if (markersMap.containsKey(currentObject.key) == false) {
            markersMap.put(currentObject.key, null)
            lifecycleScope.launch {
                val markerOptions = generateNewMarkerForEvent(
                    currentObject.key,
                    currentObject.type,
                    currentObject.userType,
                    currentObject.resourceLocation,
                    currentObject.latLng
                )

                val marker = mMap?.addMarker(markerOptions)
                val markerBundle = Bundle()
                markerBundle.putString("key", currentObject.key)
                marker?.tag = markerBundle
                markersMap.put(currentObject.key, marker!!)
                if (!markersMap.containsKey("ripple_" + currentObject.key)) {
                    Log.d("MARKERS", "Voy a generar el Ripple de ${currentObject.key}")
                    generateMarkerRipple(currentObject.key, currentObject.latLng)
                }
            }
        }
    }


    private fun getRouteAndMoveMarker(
        key: String, tempPoints: ArrayList<LatLng>, callback: MarkerAnimationCallback
    ) {

        if (key == "-N96nPjExKNyW0T79BL4") {
            var pp = 3
        }
        Thread {
            changesPending.add(key)
            requireContext().getTripBetweenCoordsAsync(key, tempPoints, object : DownloadCallback {
                override fun onDownload(data: String?) {
                    val response = Gson().fromJson<OSRMResponse>(
                        data!!, OSRMResponse::class.java
                    )
                    try {

                        Log.d("ROUTE", "inicio = " + tempPoints[0].toString())

                        val points = ArrayList<LatLng>()

                        points.add(tempPoints[0])

                        response.trips[0].legs[0].steps?.forEach { step ->
                            step.geometry.coordinates!!.forEach { geo ->
                                val point = LatLng(geo[1], geo[0])
                                points.add(point)
                                //  Log.d("ROUTE", "paso = " + point.toString())

                                // drawMarkerPath(marker.id)
                            }
                        }
                        points.add(tempPoints[1])

                        Log.d("ROUTE", "termino = " + tempPoints[1].toString())

                        Log.d(
                            "UPDATE_MARKERS ", "path generation end"
                        )

                        requireActivity().runOnUiThread {}
                    } catch (ex: Exception) {
                        Log.d("UPDATE_MARKERS", "Error buscando la ruta")
                    }
                }
            })
        }.start()
    }


    private fun getEventsFollowed(): ArrayList<EventFollowed> {
        return (requireActivity() as MainActivity).getEventsFollowed()
    }


    @JvmName("getEventKey1")
    fun getEventKey(): String? {
        return eventKeyShowing
    }

    fun setEventKey(key: String) {
        eventKeyShowing = key
    }


//-------------- CHAT WINDOW

    private fun animateChatFragmentIn() {

        chatFragmentStatus = ChatFragmentStatus.LOADING

        //   chatWindowResizeToNormalSize()

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        // Button Position
        val loc = IntArray(2)
        binding.fabChat.getLocationOnScreen(loc)
        binding.fabChat.getLocationInWindow(loc)
        val x = loc[0]
        val y = loc[1]


        val frameLayout = binding.chatLayout

// Crea una animación de transición
        val slide = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT,
            1.0f,
            Animation.RELATIVE_TO_PARENT,
            0.0f,
            Animation.RELATIVE_TO_PARENT,
            0.0f,
            Animation.RELATIVE_TO_PARENT,
            0.0f
        ).apply {
            duration = 500
            fillAfter = true
            interpolator = AccelerateDecelerateInterpolator()
        }


        slide.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                frameLayout.visibility = VISIBLE
                binding.eventsRecyclerView.isEnabled = false
                disableAdapterButtons()
            }

            override fun onAnimationEnd(animation: Animation?) {
                chatFragmentStatus = ChatFragmentStatus.OPEN
                viewModel.onChatOpened()
                chatWindowRestoreNormalSizeButton.visibility = GONE
                chatWindowMaximizeButton.setOnClickListener {
                    chatWindowResizeToFullSize()
                }
                binding.root.requestLayout()
                chatFragment?.enableControls()
                AppClass.instance.setCurrentFragment(chatFragment)
                // Provisoriamente elimino del contador los mensajes ya leidos
                mainActivity.removeUnreadMessagesByRoomKey(eventKeyShowing ?: "")
            }

            override fun onAnimationRepeat(animation: Animation?) {
                TODO("Not yet implemented")
            }
        })

        binding.unreadCounterText.text = ""
        binding.unreadCounterText.visibility = GONE

        frameLayout.startAnimation(slide)


        val contentContainerConstraintSet = ConstraintSet()
        contentContainerConstraintSet.clone(contentContainer)


        val buttonWidth = (chatButton.height - chatButton.x)
        val chatFragmentRightMargin = buttonWidth + 10.px
        contentContainerConstraintSet.setMargin(
            chatWindow.id, ConstraintSet.END, chatFragmentRightMargin.toInt()
        )

        contentContainerConstraintSet.applyTo(contentContainer)

// Asegúrate de que el FrameLayout tenga un margen derecho de 12dp

        /*
                val params = frameLayout.layoutParams as ViewGroup.MarginLayoutParams
                params.rightMargin = 60.px

                var titleBarRect = Rect()
                titleBar.getLocalVisibleRect(titleBarRect)

                params.topMargin = (titleBarRect.height()) + 10.px

                if (chatButtonAttrs == null) {
                    params.bottomMargin = 66.px
                } else {

                    var r = Rect()
                    binding.root.getLocalVisibleRect(r)
                    params.bottomMargin = requireActivity().screenHeight() - bottomToolbar.y.toInt()
                }

                frameLayout.layoutParams = params

                ((chatButtonAttrs?.y ?: 0) + (chatButtonAttrs?.height ?: 0)) / 2
        */
    }


    private fun enableAdapterButtons() {
        eventsHeaderRecyclerAdapter.enableButtons()
    }

    private fun disableAdapterButtons() {
        eventsHeaderRecyclerAdapter.disableButtons()
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
                chatFragment?.disableControls()
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


    private fun chatWindowResizeToNormalSize() {


        mainActivityViewModel.onBottomBarVisibilityOnRequired()
        mainActivityViewModel.onTitleBarVisibilityOnRequired()


        val titleBarRec = Rect()
        titleBar?.getLocalVisibleRect(titleBarRec)


        val titleBarRect = Rect()
        titleBar.getLocalVisibleRect(titleBarRect)

        val buttonWidth = (chatButton.height - chatButton.x)
        val chatFragmentRightMargin = buttonWidth

        val contentContainerConstraintSet = ConstraintSet()
        contentContainerConstraintSet.clone(contentContainer)
        contentContainerConstraintSet.clear(chatWindow.id, ConstraintSet.START)
        contentContainerConstraintSet.clear(chatWindow.id, ConstraintSet.END)
        contentContainerConstraintSet.connect(
            chatWindow.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP
        )
        contentContainerConstraintSet.connect(
            chatWindow.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START
        )
        contentContainerConstraintSet.connect(
            chatWindow.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
        )
        contentContainerConstraintSet.setMargin(
            chatWindow.id, ConstraintSet.END, chatFragmentRightMargin.toInt()
        )
        contentContainerConstraintSet.setMargin(
            chatWindow.id,
            ConstraintSet.BOTTOM,
            requireActivity().screenHeight() - bottomToolbar.y.toInt()
        )/*
            viewModel.messageFragmentMode.value == ChatWindowStatus.NORMAL
            if (titleBar.isVisible)
            {
                contentContainerConstraintSet.setMargin(
                    chatWindow?.id!!, ConstraintSet.TOP,
                     10.px
                )

            }
            else {
      */

        // if (!fromKeyboard) {
        contentContainerConstraintSet.setMargin(
            chatWindow.id, ConstraintSet.TOP, titleBar.height + 10.px
        )/*
    } else {
        contentContainerConstraintSet.setMargin(
            chatWindow?.id!!, ConstraintSet.TOP, 10.px
        )

    }*/
//        }

        contentContainerConstraintSet.constrainPercentWidth(chatWindow.id, .8f)
        chatWindow.layoutParams?.width = 0

        val chatWindowLayoutParams: ConstraintLayout.LayoutParams =
            chatWindow.layoutParams as ConstraintLayout.LayoutParams

        //   chatWindowLayoutParams.setMargins(0, 0, 50.px, 0)
        chatWindowLayoutParams.width = 0
        chatWindow.layoutParams = chatWindowLayoutParams
        chatWindow.requestLayout()


        cardView.radius = 20.px.toFloat()
        cardView.cardElevation = 2.px.toFloat()
        cardView.preventCornerOverlap = true
        cardView.useCompatPadding = true
        contentContainerConstraintSet.applyTo(contentContainer)
        contentContainer.requestLayout()
        cardView.requestLayout()

        val chatButtonRect = Rect()
        chatButton.getLocalVisibleRect(chatButtonRect)

        chatWindowMaximizeButton.setOnClickListener {
            chatWindowResizeToFullSize()
        }


        chatWindowMaximizeButton.visibility = VISIBLE
        chatWindowRestoreNormalSizeButton.visibility = GONE
        chatButton.visibility = VISIBLE
        viewModel.setChatWindowMode(ChatWindowStatus.NORMAL)
    }

    private fun chatWindowResizeToFullSize() {
        val chatLayout: FrameLayout = binding.chatLayout

        mainActivityViewModel.onBottomBarVisibilityOffRequired()
        mainActivityViewModel.onTitleBarVisibilityOffRequired()

        cardView.cardElevation = 0f
        cardView.radius = 0f
        cardView.preventCornerOverlap = false
        cardView.useCompatPadding = false


        val contentContainerConstraintSet = ConstraintSet()
        contentContainerConstraintSet.clone(contentContainer)
        contentContainerConstraintSet.clear(chatWindow.id, ConstraintSet.START)
        contentContainerConstraintSet.clear(chatWindow.id, ConstraintSet.END)
        contentContainerConstraintSet.connect(
            chatWindow.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP
        )
        contentContainerConstraintSet.setMargin(
            chatWindow.id,
            ConstraintSet.BOTTOM,
            requireActivity().screenHeight() - bottomToolbar.y.toInt()
        )

        contentContainerConstraintSet.connect(
            chatWindow.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START
        )
        contentContainerConstraintSet.connect(
            chatWindow.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
        )

        contentContainerConstraintSet.constrainPercentWidth(chatWindow.id, 1f)
        contentContainerConstraintSet.applyTo(contentContainer)


        val chatWindowLayoutParams = (chatWindow.layoutParams as ViewGroup.MarginLayoutParams)


        chatWindowLayoutParams.setMargins(0, 0, 0, 0)
        chatWindow.layoutParams = chatWindowLayoutParams
        chatWindow.requestLayout()

        chatWindowRestoreNormalSizeButton.setOnClickListener {
            chatWindowResizeToNormalSize()
        }

        chatWindowMaximizeButton.visibility = GONE

        chatWindowMaximizeButton.visibility = GONE
        chatWindowRestoreNormalSizeButton.visibility = VISIBLE

        viewModel.setChatWindowMode(ChatWindowStatus.FULLSCREEN)
    }

    private fun whenKeyboardOpen() {

        val titleBarRec = Rect()
        titleBar?.getLocalVisibleRect(titleBarRec)

        // cuando el teclado esta abierto
        //-- reduzco limites de la cardview que sirve de marco
        cardView.radius = 0f
        cardView.cardElevation = 0f
        cardView.elevation = 0f

        val chatFragmentLayoutParams: ConstraintLayout.LayoutParams =
            chatWindow.layoutParams as ConstraintLayout.LayoutParams
        chatFragmentLayoutParams.setMargins(0, 0, 0, 0)
        chatWindow.layoutParams = chatFragmentLayoutParams

        val constraintSet = ConstraintSet()
        constraintSet.clone(contentContainer)

        constraintSet.connect(
            chatWindow.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM
        )
        constraintSet.setMargin(chatWindow.id, ConstraintSet.BOTTOM, 0)

        constraintSet.connect(
            chatWindow.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP
        )
        constraintSet.setMargin(
            chatWindow.id, ConstraintSet.TOP, titleBarRec.bottom + 10.px
        )

        constraintSet.connect(
            chatWindow.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START
        )
        constraintSet.setMargin(chatWindow.id, ConstraintSet.START, 0)

        constraintSet.connect(
            chatWindow.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
        )
        constraintSet.setMargin(chatWindow.id, ConstraintSet.END, 0)

        constraintSet.constrainPercentWidth(chatWindow.id, 1f)


        /*
                var chatFragmentContainerLayoutParams: CoordinatorLayout.LayoutParams =
                    contentContainer.layoutParams as CoordinatorLayout.LayoutParams

                chatFragmentContainerLayoutParams.setMargins(0, titleBarRec.bottom + 2.px, 0, 0)
                contentContainer.layoutParams = chatFragmentContainerLayoutParams
        */
        constraintSet.applyTo(contentContainer)

        chatWindowMaximizeButton.visibility = GONE
        chatWindow.requestLayout()

        chatButton.visibility = GONE
    }

    private fun whenKeyboardClose() {

        val titleBarRec = Rect()
        titleBar?.getLocalVisibleRect(titleBarRec)
        when (viewModel.messageFragmentMode.value) {
            ChatWindowStatus.NORMAL -> {
                chatWindowResizeToNormalSize()
            }

            ChatWindowStatus.FULLSCREEN -> {
                chatWindowResizeToFullSize()
            }

            else -> {}
        }


        // cuando el teclado esta abierto
        //-- reduzco limites de la cardview que sirve de marco
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

    fun doAction(arguments: Bundle) {
        var pp = 33
        val eventKey = viewModel.currentEventKey.value.toString()
        connectToChat(eventKey)
        animateChatFragmentIn()
    }

    fun connectToChat(eventKey: String) {
        chatFragment?.connectToEvent(eventKey)
    }
}




