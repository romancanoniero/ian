package com.iyr.ian.app


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.liveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.exoplayer.ExoPlayer
import com.clj.fastble.BleManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_CANCEL_PANIC
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_REQUEST_PIN
import com.iyr.ian.AppConstants.Companion.CHANNEL_ALARM_DESCRIPTION
import com.iyr.ian.AppConstants.Companion.CHANNEL_ALARM_ID
import com.iyr.ian.AppConstants.Companion.CHANNEL_ALARM_NAME
import com.iyr.ian.AppConstants.Companion.CHANNEL_DEFAULT_DESCRIPTION
import com.iyr.ian.AppConstants.Companion.CHANNEL_DEFAULT_ID
import com.iyr.ian.AppConstants.Companion.CHANNEL_DEFAULT_NAME
import com.iyr.ian.BuildConfig
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.core.Core
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.models.GeoLocation
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.dao.models.SystemConfig
import com.iyr.ian.dao.models.UnreadMessages
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.ConfigRepository
import com.iyr.ian.dao.repositories.EventRepository
import com.iyr.ian.dao.repositories.EventsFollowedRepository
import com.iyr.ian.dao.repositories.EventsRepository
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.enums.EventStatusEnum
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITag.ble
import com.iyr.ian.itag.ITagModesEnum
import com.iyr.ian.physical_button.BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ChatRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ConfigRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.CoreRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsFollowedRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SubscriptionTypeRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.services.falling_detection.FallDetectionServiceMethod
import com.iyr.ian.services.location.ServiceLocation
import com.iyr.ian.services.location.isServiceRunning
import com.iyr.ian.services.receivers.GpsStatusReceiver
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.ui.base.ValidationPulsePayload
import com.iyr.ian.utils.SMSUtils
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionInterface
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_ACTION_BLE_DEVICE_ADD_TO_AUTO_CONNECTION
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_ACTION_BLE_DEVICE_REMOVE_FROM_AUTO_CONNECTION
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_BLE_DEVICE_DISCONNECTED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_BLE_DEVICE_DISCOVERED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_BLE_REFRESH_DEVICES_DISCONNECTED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_BLE_SERVICE_CONNECTED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_BLE_SERVICE_DISCONNECTED
import com.iyr.ian.utils.bluetooth.constants.BTConstants.Companion.BROADCAST_MESSAGE_SCAN_RESULT_UPDATED
import com.iyr.ian.utils.bluetooth.startBLEService
import com.iyr.ian.utils.bluetooth.stopBLEService
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getFileExtension
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.permissionsVibration
import com.iyr.ian.utils.support_models.MediaTypesEnum
import com.iyr.ian.viewmodels.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.collections.set


// Jeffrey Christopher
// jemoraz
// Shinan
// Özgür Ablak telefono lanzadera avion
// ANIGRAV studio lanzadera


class AppClass : Application(), LifecycleEventObserver, LifecycleObserver {

    var logged: Boolean = false
    private var currentFragment: androidx.fragment.app.Fragment? = null
    private var mainActivityRef: MainActivity? = null

    var alreadyStarted = false

    @SuppressLint("StaticFieldLeak")
    var context: Context? = null
    var core: Core? = null


    val player by lazy { ExoPlayer.Builder(this).build() }

    private val _lastLocation = MutableLiveData<Location?>(null)
    val lastLocation: LiveData<Location?> = _lastLocation


    // utilizado para observar el estado cuando se pulsa el boton de panico.
    private val _postingPanicButtonStatus = MutableLiveData<Resource<Event?>>()
    val postingPanicButtonStatus: LiveData<Resource<Event?>> = _postingPanicButtonStatus

    private val _subscriptionType = MutableLiveData<Resource<SubscriptionTypes?>>()
    val subscriptionType: LiveData<Resource<SubscriptionTypes?>> = _subscriptionType

    private val _isFreeUser = MutableLiveData<Boolean>(true)
    val isFreeUser: LiveData<Boolean> = _isFreeUser
    fun isFreeUser() = isFreeUser.value ?: true

    private val _eventsFlow = MutableLiveData<Resource<EventsRepository.DataEvent?>>()
    val eventsFlow: LiveData<Resource<EventsRepository.DataEvent?>> = _eventsFlow


    //var lastLocation: LatLng = LatLng(0.0, 0.0)
    var appConfigs: SystemConfig? = null

    var serviceLocationPointer: ServiceLocation? = null
    private lateinit var eventsRepository: EventsRepositoryImpl


    /*
        private lateinit var appConfigsRef: DatabaseReference
        private var appConfigsListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                this@AppClass.appConfigs = snapshot.getValue(SystemConfig::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                instance.currentActivity?.showErrorDialog(error.toException().localizedMessage)
            }
        }
    */

    private val lastUnreadMessagesMap: HashMap<String, UnreadMessages> by lazy { HashMap<String, UnreadMessages>() }
    private lateinit var userCurrentPlan: SubscriptionTypes

    //  private lateinit var userSubscription: Subscription
    var isLocationAvailable: Boolean = false

    //   private var mLocationService: LocationService? = null
    private var mLocationServiceConnection: ServiceConnection? = null

    /*
    TODO: Implementar desde el repositorio

    private val unreadMessagesRef by lazy {
        FirebaseDatabase
            .getInstance()
            .getReference(TABLE_CHATS_UNREADS_BY_USER)
            .child(FirebaseAuth.getInstance().uid.toString())
    }
    private var unreadMessagesListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val hashMapUnreads = HashMap<String, UnreadMessages>()
            if (snapshot.exists()) {
                snapshot.children.forEach { room ->
                    val _room = room.getValue(UnreadMessages::class.java)!!
                    _room.chat_room_key = room.key!!
                    hashMapUnreads[room.key.toString()] = _room
                }
            }
            this@AppClass.lastUnreadMessagesMap.clear()
            this@AppClass.lastUnreadMessagesMap.putAll(hashMapUnreads)
            broadcastUnreadMessagesUpdate(hashMapUnreads)
        }

        override fun onCancelled(error: DatabaseError) {
            instance.currentActivity?.showErrorDialog(error.toException().localizedMessage)
        }
    }
    */


    var itagPressMode = ITagModesEnum.active


    private val subscriptionTypesRepository by lazy { SubscriptionTypeRepositoryImpl() }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            super.onReceive(context, intent)
            Log.d("ITags", "broadcastReceiver")


            when (intent.action) {
                BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED -> {

                    Log.d("ITags", "BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED")

                    Log.d("ITags", "ITagMode = " + instance.itagPressMode.toString())
                    Log.d(
                        "ITags", "isInPanic = " + isInPanic().toString()
                    )


                    if (instance.itagPressMode == ITagModesEnum.active && !isInPanic()) {
                        Log.d("ITags", "Disparo el panico")
                        onEmergencyButtonPressed()
                    } else {
                        Log.d("ITags", "no lo dispare ")
                    }
                }
                //-------------
                BROADCAST_ACTION_CANCEL_PANIC -> {

                    Toast.makeText(context, "Enviar mensaje al Core 2", Toast.LENGTH_SHORT).show()

                    var eventKey: String? = null
                    if (intent.hasExtra("data")) {
                        var extrasAsJson = intent.getStringExtra("data")

                        var bundle = Gson().fromJson<HashMap<String, String>>(
                            extrasAsJson, HashMap::class.java
                        )
                        eventKey = bundle.get("panicEventKey")
                    }

                    var payload = ValidationPulsePayload()
                    payload.validationType = PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT
                    payload.key = eventKey ?: "error"


                    broadcastMessage(payload, BROADCAST_ACTION_REQUEST_PIN)/*
                                        var mainActivity = (currentActivity as MainActivity)

                                        mainActivity.requestStatusConfirmation(
                                            PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT,
                                            object : PulseValidationCallback {

                                                override fun onWrongCode(
                                                    dialog: PulseValidatorDialog,
                                                    securityPIN: String
                                                ) {
                                                    super.onWrongCode(dialog, securityPIN)
                                                    if (!isFreeUser()) {
                                                        mainActivity.showErrorDialog(
                                                            getString(R.string.error_wrong_security_code),
                                                            getString(R.string.error_wrong_security_code_message),
                                                            getString(R.string.close),
                                                            null
                                                        )
                                                    } else {
                                                        Toast.makeText(
                                                            this@AppClass,
                                                            "Codigo Incorrecto. Hay que Notificar a todos",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }


                                                override fun onValidationOK(
                                                    dialog: PulseValidatorDialog,
                                                    code: String
                                                ) {

                                                    if (!isFreeUser()) {
                                                        mainActivity.showLoader(getString(R.string.closing_event_wait))

                                                        EventsWSClient.instance.closeEvent(
                                                            getPanicEventKey()!!,
                                                            FirebaseAuth.getInstance().uid.toString(),
                                                            code,
                                                            object : OnCompleteCallback {
                                                                override fun onComplete(
                                                                    success: Boolean,
                                                                    result: Any?
                                                                ) {
                                                                    mainActivity.hideLoader()
                                                                    getPanicEvent()
                                                                        ?.let { mainActivity.onEventCloseDone(it.event_key) }

                                                                    mainActivity.showAnimatedDialog(
                                                                        getString(R.string.closing_event_title),
                                                                        getString(R.string.event_sucessfully_close)
                                                                    )

                                                                }
                                                            }
                                                        )
                                                    } else {
                                                        SessionApp.getInstance(this@AppClass).isInPanic(false)
                                                        broadcastMessage(null, BROADCAST_ACTION_REFRESH_PANIC_BUTTON)
                                                    }
                                                }
                                            })
                      */
                }


                Constants.BROADCAST_LOCATION_UPDATED -> {
                    try {
                        val dataAsJson = intent.getStringExtra("data")
                        val data = Gson().fromJson(dataAsJson, java.util.HashMap::class.java)
                        val locationAsJson = data["location"].toString()
                        val locationCust: java.util.HashMap<String, Double> = Gson().fromJson(
                            locationAsJson, java.util.HashMap::class.java
                        ) as java.util.HashMap<String, Double>
                        val latLng = LatLng(
                            (locationCust["latitude"])!!, (locationCust["longitude"])!!
                        )
//                        _lastLocation.postValue(latLng)

                    } catch (ex: Exception) {
                        var pp = 33

                    }
                }

                BROADCAST_MESSAGE_BLE_SERVICE_CONNECTED -> {
                    Toast.makeText(context, "BLE Connected", Toast.LENGTH_SHORT).show()
                    if (SessionApp.getInstance(context).isBTPanicButtonEnabled) {
                        //          bleService?.scan()
                    }
                }

                BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED -> {
                    val dataAsJson = intent.getStringExtra("data")
                    val device = Gson().fromJson(dataAsJson, BluetoothDevice::class.java)
                    //  bleService?.pressMode = ITagService.ButtonMode.ON
                    Toast.makeText(context, "Device Connected $device ", Toast.LENGTH_SHORT).show()

                    //    bleDevicesConnected.put(device.address, device)
                    broadcastMessage(
                        null,
                        AppConstants.ServiceCode.BROADCAST_MESSAGE_UPDATE_BLE_DEVICES_INDICATOR_REQUESTED
                    )

                }

                BROADCAST_MESSAGE_BLE_DEVICE_DISCONNECTED -> {
                    val dataAsJson = intent.getStringExtra("data")
                    val device = Gson().fromJson(dataAsJson, BluetoothDevice::class.java)



                    broadcastMessage(
                        null,
                        AppConstants.ServiceCode.BROADCAST_MESSAGE_UPDATE_BLE_DEVICES_INDICATOR_REQUESTED
                    )

                    /*      broadcastMessage(
                              bleService?.getDevicesList(),
                              ServiceCode.BROADCAST_MESSAGE_REFRESH_BLE_DEVICES_LIST
                          )
      */
                    Toast.makeText(context, "Device Disconnected $device ", Toast.LENGTH_SHORT)

                }

                BROADCAST_ACTION_BLE_DEVICE_ADD_TO_AUTO_CONNECTION -> {
                    val dataAsJson = intent.getStringExtra("data")
                    val device = Gson().fromJson(dataAsJson, BluetoothDevice::class.java)
                    SessionApp.getInstance(context).addBleDeviceToConnectList(device)/*
                            broadcastMessage(
                                bleService?.getDevicesList(),
                                ServiceCode.BROADCAST_MESSAGE_REFRESH_BLE_DEVICES_LIST
                            )*/
                }

                BROADCAST_ACTION_BLE_DEVICE_REMOVE_FROM_AUTO_CONNECTION -> {
                    val dataAsJson = intent.getStringExtra("data")
                    val device = Gson().fromJson(dataAsJson, BluetoothDevice::class.java)

                    SessionApp.getInstance(context).removeFromBleDevicesToConnectList(device)/*
                            broadcastMessage(
                                bleService?.getDevicesList(),
                                ServiceCode.BROADCAST_MESSAGE_REFRESH_BLE_DEVICES_LIST
                            )

                     */
                    Toast.makeText(
                        context,
                        "Device Disconnected $device and remove from autoconnect ",
                        Toast.LENGTH_SHORT
                    )

                }

                BROADCAST_MESSAGE_BLE_DEVICE_DISCOVERED -> {
                    val dataAsJson = intent.getStringExtra("data")
                    val device = Gson().fromJson(dataAsJson, BluetoothDevice::class.java)
                    if (SessionApp.getInstance(context).bleDevicesToConnect.contains(device.address)) {
                        //             bleService?.connect(device.address)
                    }
                }


                BROADCAST_MESSAGE_BLE_REFRESH_DEVICES_DISCONNECTED -> {
                    var pp = 3
                }
            }
        }
    }

    fun getUnreadMessagesMap(): HashMap<String, UnreadMessages> {
        return lastUnreadMessagesMap
    }


    var isKilling = false

    //  var isTrackingMe: Boolean = false
    private var currentActivity: Activity? = null
    private var mediaPlayer: MediaPlayer? = null
    val isUserAddressStorageEnabled: Boolean = false
    var isInForeground: Boolean = false
    var startTime: Long = 0

    companion object {

        var subscriptionType: Int? = 0
        private lateinit var mInstance: AppClass

        @JvmStatic
        val instance: AppClass
            get() {
                if (!::mInstance.isInitialized) {
                    mInstance = AppClass()
                    //Initialize Loader
                }
                return mInstance
            }

    }


    init {
        mInstance = this
        this.mediaPlayer = MediaPlayer()
    }


    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck: FirebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance())


        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, p1: Bundle?) {
                // No necesito usarla
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                isInForeground = true
                val intent = Intent(AppConstants.BROADCAST_ACTION_ENTER_FOREGROUND)
                intent.setPackage(this@AppClass.packageName)
                LocalBroadcastManager.getInstance(this@AppClass).sendBroadcast(intent)
            }

            override fun onActivityPaused(activity: Activity) {
                isInForeground = false

                val intent = Intent(AppConstants.BROADCAST_ACTION_ENTER_BACKGROUND)
                intent.setPackage(this@AppClass.packageName)
                LocalBroadcastManager.getInstance(this@AppClass).sendBroadcast(intent)

            }

            override fun onActivityStopped(activity: Activity) {
                // No lo necesito
            }

            override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
                // No lo necesito
            }

            override fun onActivityDestroyed(activity: Activity) {
                //     currentActivity = null
            }
        })

        eventsRepository = EventsRepositoryImpl()


        if (FirebaseAuth.getInstance().uid != null && !SessionForProfile.getInstance(this).getUserId().isNullOrBlank())
         {

            val gpsStatusReceiver = GpsStatusReceiver()
            registerReceiver(
                gpsStatusReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            )
            setupPrimaryObservers()
            setupPrimaryObservables(FirebaseAuth.getInstance().uid.toString())
        }


        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        context = this

        Log.d("ITags", "AppClass creado ")

        GlobalScope.launch(Dispatchers.IO) {
            ITag.initITag(context)
        }
        // core = Core(applicationContext)
        startTime = System.currentTimeMillis()

        createLocationServiceNotificationChannel()
        createNotificationChannel()
        //  connectToLocationService()
        //  appConfigsSubscribe()
        // registerReceivers()
//        core?.registerReceivers()

    }


    private val coreRepository by lazy { CoreRepositoryImpl() }
    private val usersRepository by lazy { UsersRepositoryImpl() }
    private val subscriptionsRepository by lazy { SubscriptionTypeRepositoryImpl() }


    private val _initializationReady = MutableLiveData<Boolean>()
    val initializationReady: LiveData<Boolean> = _initializationReady

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user


    private val _panic = MutableLiveData<Boolean>()
    val panic: LiveData<Boolean> = _panic

    private val _panicButtonStatus = MutableLiveData<Resource<Event?>>() // Maneja el estado cuando se esta publicando un evento de panico
    val panicButtonStatus: LiveData<Resource<Event?>> = _panicButtonStatus

    private val _refreshEventsShortcut = MutableLiveData<Boolean>()
    val refreshEventsShortcut: LiveData<Boolean> = _refreshEventsShortcut

    private val _showEventsCounter = MutableLiveData<Boolean>()
    val showEventsCounter: LiveData<Boolean> = _showEventsCounter

    private val _enableMapIcon = MutableLiveData<Boolean>()
    val enableMapIcon: LiveData<Boolean> = _enableMapIcon


    private var eventsFollowingList = ArrayList<EventFollowed>()

    val eventsFollowed: HashMap<String, EventFollowed> = HashMap<String, EventFollowed>()


    private val _userSubscription = MutableLiveData<SubscriptionTypes?>()
    val userSubscription: LiveData<SubscriptionTypes?> = _userSubscription


    private lateinit var userFlowJob: Job

    fun setupPrimaryObservers() {

        /*
        eventsFlow.observe(ProcessLifecycleOwner.get()) {
            //        processEventsRelatedActions()

            if (getEventsCount() > 0) {
                _enableMapIcon.postValue(true)
                _showEventsCounter.postValue(true)

                var newPanicStatus = isInPanic()
                if (_panic.value != newPanicStatus) {
                    _panic.postValue(newPanicStatus)
                }
            } else {
                _enableMapIcon.postValue(false)
                _showEventsCounter.postValue(false)
                _panic.postValue(false)
            }

        }
   */



    }

//check bugs


    private fun setupPrimaryObservables(userKey: String) {/*
                GlobalScope.launch(Dispatchers.IO) {

                    eventsRepository.getEvents(userKey).collect { event ->
                        when (event) {
                            is EventsRepository.DataEvent.ChildAdded -> {
                                eventsFollowingList.add(event.data)
                            }

                            is EventsRepository.DataEvent.ChildChanged -> {
                                val index = eventsFollowingList.indexOf(event.data) ?: -1
                                if (index > -1) {
                                    eventsFollowingList.set(index, event.data)
                                }
                            }

                            is EventsRepository.DataEvent.ChildRemoved -> {
                                val index = eventsFollowingList.indexOf(event.data) ?: -1
                                if (index > -1) {
                                    eventsFollowingList.removeAt(index)
                                }
                            }

                            is EventsRepository.DataEvent.OnChildMoved -> TODO()
                            is EventsRepository.DataEvent.OnError -> TODO()
                            else -> {}
                        }
                        _eventsFollowed.postValue(eventsFollowingList)
                        _eventsFlow.postValue(event)
                        if (_refreshEventsShortcut.value == true == false) {
                            //   processEventsRelatedActions()
                            _refreshEventsShortcut.postValue(true)
                        }

                    }
                }
        */
        GlobalScope.launch(Dispatchers.IO) {

            var userReturned: User? = null
            userFlowJob = GlobalScope.launch(Dispatchers.IO) {
                usersRepository.getUserDataAsFlow(
                    SessionForProfile.getInstance(this@AppClass).getUserId()!!
                )?.collect { response ->
                    if (response is Resource.Success) {
                        userReturned = response.data!!
                        _user.postValue(userReturned!!)
                        var intent: Intent? = null
                        SessionForProfile.getInstance(this@AppClass).storeUserProfile(userReturned)

                        _initializationReady.postValue(true)
                    } else {
                        // TODO: Manejar este error
                    }
                }

            }
        }
    }


    fun remorePrimaryObservers() {
        //eventsFollowed.removeObservers(ProcessLifecycleOwner.get())
    }


    /**
     * Retorna por unica vez la lista de eventos seguidos
     */


    var eventsJobsMap = HashMap<String, Job>()
    private val _eventsMap =
        MutableLiveData<HashMap<String, EventFollowed>>(HashMap<String, EventFollowed>())
    val eventsMap: LiveData<HashMap<String, EventFollowed>> = _eventsMap

    private val _eventsListFlow = MutableLiveData<EventsRepository.DataEvent>()
    val eventsListFlow: LiveData<EventsRepository.DataEvent> get() = _eventsListFlow


    //   var eventsFollowed = HashMap<String, EventFollowed>()
    /**
     * Ok
     */
    var getEventsFollowedFlow =
        liveData<EventsFollowedRepository.EventsFollowedDataEvent?>(Dispatchers.IO) {

            var eventsFollowedRepository = EventsFollowedRepositoryImpl()

            eventsFollowedRepository.getEventsFollowedFlow(FirebaseAuth.getInstance().uid.toString())
                ?.collect { update ->
                    //emit(update)
                    when (update) {
                        is Resource.Error -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            emit(update.data!!)

                            var resourceEvent: EventsFollowedRepository.EventsFollowedDataEvent =
                                update.data!!



                            when (resourceEvent) {
                                is EventsFollowedRepository.EventsFollowedDataEvent.OnChildAdded -> {
                                    var eventFollowed =
                                        (resourceEvent as EventsFollowedRepository.EventsFollowedDataEvent.OnChildAdded).data


                                    if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {

                                        eventsFollowed.set(
                                            eventFollowed.event_key,
                                            eventFollowed
                                        )
                                    } else {
                                        eventsFollowed.put(
                                            eventFollowed.event_key,
                                            eventFollowed
                                        )
                                    }
                                }

                                is EventsFollowedRepository.EventsFollowedDataEvent.OnChildChanged -> {
                                    var eventFollowed =
                                        (resourceEvent as EventsFollowedRepository.EventsFollowedDataEvent.OnChildChanged).data


                                    if (eventsFollowed.containsKey(eventFollowed.event_key!!) == true) {
                                        eventsFollowed.set(eventFollowed.event_key, eventFollowed)
                                    } else {
                                        eventsFollowed.put(
                                            eventFollowed.event_key,
                                            eventFollowed
                                        )
                                    }
                                }

                                is EventsFollowedRepository.EventsFollowedDataEvent.OnChildMoved -> {
                                }

                                is EventsFollowedRepository.EventsFollowedDataEvent.OnChildRemoved -> {
                                    var eventFollowed =
                                        (resourceEvent as EventsFollowedRepository.EventsFollowedDataEvent.OnChildRemoved).data

                                    if (eventsFollowed.containsKey(eventFollowed.event_key!!) == true) {
                                        eventsFollowed.remove(eventFollowed.event_key)
                                    }
                                    // si el autor del evento soy yo, reestablece el estado de panico
                                    if (eventFollowed.event_type == EventTypesEnum.PANIC_BUTTON.toString() && eventFollowed.author.author_key== FirebaseAuth.getInstance().uid.toString()) {
                                       _panic.postValue(false)
                                        SessionApp.getInstance(this@AppClass).isInPanic(false)
                                    }
                                }

                                is EventsFollowedRepository.EventsFollowedDataEvent.OnError -> {
                                    var pp = 3
                                }
                            }


                            _eventsMap.postValue(eventsFollowed)
                        }
                    }

                    /*
                                    when (update) {
                                        is EventsFollowedRepository.EventsFollowedDataEvent.OnError -> {
                                            emit(Resource.Error(update.exception.localizedMessage.toString()))
                                        }

                                        null -> {
                                            emit(Resource.Error("null"))
                                        }

                                        else -> {
                                            emit(Resource.Success(update))
                                        }
                                    }
                    */
                }
        }

    var unreadMessagesList: ArrayList<UnreadMessages> = ArrayList<UnreadMessages>()


    var unreadMessagesFlow = liveData<ArrayList<UnreadMessages>?>(Dispatchers.IO) {
        var chatRepository: ChatRepositoryImpl = ChatRepositoryImpl()
        chatRepository.unreadMessagesFlow(FirebaseAuth.getInstance().uid.toString())
            .collect { unreadMessages ->
                unreadMessagesList = ArrayList<UnreadMessages>()
                unreadMessages.forEach { unreadMessage ->
                    unreadMessagesList.add(unreadMessage)
                }
                emit(unreadMessagesList)
            }
    }

    fun getUnreadMessagesExcludingSomeRoomKey(roomKey: String): LiveData<ArrayList<UnreadMessages>?> {
        unreadMessagesList.removeIf { it.chat_room_key == roomKey }
        return unreadMessagesFlow

    }

    suspend fun getEventsFollowingAll(userKey: String): Resource<Boolean?> {

        var eventFollowedRepository = EventsFollowedRepositoryImpl()
        var call = eventFollowedRepository.getEventsFollowedAll(userKey)
        when (call) {
            is Resource.Error -> {}
            is Resource.Loading -> {}
            is Resource.Success -> {
                var list = call.data!!
                list.forEach { eventFollowed ->
                    if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                        _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                    } else {
                        _eventsMap.value!!.put(eventFollowed.event_key, eventFollowed)
                    }
                }

            }
        }

        /*
                    ?.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                if (resource.data != null) {

                                    var eventData = resource.data!!

                                    when (eventData) {
                                        is EventsFollowedRepository.EventsFollowedDataEvent.OnChildAdded -> {
                                            var eventFollowed = eventData.data
                                            if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                                _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                            } else {
                                                _eventsMap.value!!.put(
                                                    eventFollowed.event_key,
                                                    eventFollowed
                                                )
                                            }
                                        }

                                        is EventsFollowedRepository.EventsFollowedDataEvent.OnChildChanged -> {
                                            var eventFollowed = eventData.data
                                            if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                                _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                            } else {
                                                _eventsMap.value!!.put(
                                                    eventFollowed.event_key,
                                                    eventFollowed
                                                )
                                            }
                                        }

                                        is EventsFollowedRepository.EventsFollowedDataEvent.OnChildMoved -> {
                                        }

                                        is EventsFollowedRepository.EventsFollowedDataEvent.OnChildRemoved -> {
                                            var eventFollowed = eventData.data
                                            if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                                _eventsMap.value!!.remove(eventFollowed.event_key)
                                            }
                                        }

                                        is EventsFollowedRepository.EventsFollowedDataEvent.OnError -> {
                                            var pp = 3
                                        }
                                    }

                                }
                            }

                            is Resource.Error -> {
                                Resource.Error<Boolean?>(resource.message!!)
                            }

                            is Resource.Loading -> {
                                Resource.Loading<Boolean?>(null)
                            }

                            else -> {}
                        }
                    }
        */

        var eventsRepository = EventsRepositoryImpl()
        withContext(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                val eventsList = eventsRepository.getEventsList(userKey)

                when (eventsList) {
                    is Resource.Success -> {
                        if (eventsList.data != null) {
//                            _eventsFollowed.postValue(eventsList.data!!)
                            eventsList.data!!.forEach { eventFollowed ->

                                if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                    _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                    /*
                                    _eventsEventsFlow.postValue(
                                        EventRepository.EventDataEvent.OnChildChanged(event, null)
                                    )*/
                                } else {
                                    _eventsMap.value!!.put(eventFollowed.event_key, eventFollowed)
                                    /*
                                                         _eventsEventsFlow.postValue(
                                                             EventRepository.EventDataEvent.OnChildAdded(event, null)
                                                         )

                                     */
                                }

                                /*
                                                                if (!eventsJobsMap.containsKey(eventFollowed.event_key)) {

                                                                    eventsJobsMap[eventFollowed.event_key] =
                                                                        createEventUpdatesJob(eventFollowed.event_key)
                                                                }
                                                                eventsJobsMap[eventFollowed.event_key]?.start()
                                */


                            }


                            processEventsRelatedActions()
                            startFollowingEvents(userKey)
                            Resource.Success(true)
                        } else {
                            Resource.Error<Boolean?>(eventsList.message!!)
                        }
                    }

                    is Resource.Error -> {
                        Resource.Error<Boolean?>(eventsList.message!!)
                    }

                    is Resource.Loading -> {
                        Resource.Loading<Boolean?>(null)
                    }

                    else -> {}
                }

            }

        }
        return Resource.Loading<Boolean?>(null)
    }


    fun eventsFollowedJob(userKey: String) = GlobalScope.launch(Dispatchers.IO) {
        var eventFollowedRepository = EventsFollowedRepositoryImpl()
        eventFollowedRepository.getEventsFollowedFlow(userKey)?.collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    if (resource.data != null) {

                        var eventData = resource.data!!



                        when (eventData) {
                            is EventsFollowedRepository.EventsFollowedDataEvent.OnChildAdded -> {
                                var eventFollowed = eventData.data
                                if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                    _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                } else {
                                    _eventsMap.value!!.put(eventFollowed.event_key, eventFollowed)
                                }

                            }

                            is EventsFollowedRepository.EventsFollowedDataEvent.OnChildChanged -> {
                                var eventFollowed = eventData.data
                                if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                    _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                } else {
                                    _eventsMap.value!!.put(eventFollowed.event_key, eventFollowed)
                                }
                            }

                            is EventsFollowedRepository.EventsFollowedDataEvent.OnChildMoved -> {
                            }

                            is EventsFollowedRepository.EventsFollowedDataEvent.OnChildRemoved -> {
                                var eventFollowed = eventData.data
                                if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                    _eventsMap.value!!.remove(eventFollowed.event_key)
                                }
                            }

                            is EventsFollowedRepository.EventsFollowedDataEvent.OnError -> {
                                var pp = 3
                            }

                            else -> {}
                        }

                    }
                }

                is Resource.Error -> {
                    Resource.Error<Boolean?>(resource.message!!)
                }

                is Resource.Loading -> {
                    Resource.Loading<Boolean?>(null)
                }

                else -> {}
            }
        }

        /*
                var eventsRepository = EventsRepositoryImpl()
                withContext(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        val eventsList = eventsRepository.getEventsList(userKey)

                        when (eventsList) {
                            is Resource.Success -> {
                                if (eventsList.data != null) {
        //                            _eventsFollowed.postValue(eventsList.data!!)
                                    eventsList.data!!.forEach { eventFollowed ->

                                        if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                            _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                            /*
                                            _eventsEventsFlow.postValue(
                                                EventRepository.EventDataEvent.OnChildChanged(event, null)
                                            )*/
                                        } else {
                                            _eventsMap.value!!.put(eventFollowed.event_key, eventFollowed)
                                            /*
                                                                 _eventsEventsFlow.postValue(
                                                                     EventRepository.EventDataEvent.OnChildAdded(event, null)
                                                                 )

                                             */
                                        }

                                        /*
                                                                        if (!eventsJobsMap.containsKey(eventFollowed.event_key)) {

                                                                            eventsJobsMap[eventFollowed.event_key] =
                                                                                createEventUpdatesJob(eventFollowed.event_key)
                                                                        }
                                                                        eventsJobsMap[eventFollowed.event_key]?.start()
                                        */


                                    }


                                    processEventsRelatedActions()
                                    startFollowingEvents(userKey)
                                    Resource.Success(true)
                                } else {
                                    Resource.Error<Boolean?>(eventsList.message!!)
                                }
                            }

                            is Resource.Error -> {
                                Resource.Error<Boolean?>(eventsList.message!!)
                            }

                            is Resource.Loading -> {
                                Resource.Loading<Boolean?>(null)
                            }

                            else -> {}
                        }

                    }

                }
                */
    }

    suspend fun getEventsFollowingFlow(userKey: String): Resource<Boolean?> {

        var eventFollowedRepository = EventsFollowedRepositoryImpl()
        eventFollowedRepository.getEventsFollowedFlow(userKey)?.collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    if (resource.data != null) {

                        var eventData = resource.data!!

                        when (eventData) {
                            is EventsFollowedRepository.EventsFollowedDataEvent.OnChildAdded -> {
                                var eventFollowed = eventData.data
                                if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                    _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                } else {
                                    _eventsMap.value!!.put(eventFollowed.event_key, eventFollowed)
                                }

                            }

                            is EventsFollowedRepository.EventsFollowedDataEvent.OnChildChanged -> {
                                var eventFollowed = eventData.data
                                if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                    _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                } else {
                                    _eventsMap.value!!.put(eventFollowed.event_key, eventFollowed)
                                }
                            }

                            is EventsFollowedRepository.EventsFollowedDataEvent.OnChildMoved -> {
                            }

                            is EventsFollowedRepository.EventsFollowedDataEvent.OnChildRemoved -> {
                                var eventFollowed = eventData.data
                                if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                    _eventsMap.value!!.remove(eventFollowed.event_key)
                                }
                            }

                            is EventsFollowedRepository.EventsFollowedDataEvent.OnError -> {
                                var pp = 3
                            }

                            else -> {}
                        }

                    }
                }

                is Resource.Error -> {
                    Resource.Error<Boolean?>(resource.message!!)
                }

                is Resource.Loading -> {
                    Resource.Loading<Boolean?>(null)
                }

                else -> {}
            }
        }


        var eventsRepository = EventsRepositoryImpl()
        withContext(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                val eventsList = eventsRepository.getEventsList(userKey)

                when (eventsList) {
                    is Resource.Success -> {
                        if (eventsList.data != null) {
//                            _eventsFollowed.postValue(eventsList.data!!)
                            eventsList.data!!.forEach { eventFollowed ->

                                if (_eventsMap.value!!.containsKey(eventFollowed.event_key)) {
                                    _eventsMap.value!![eventFollowed.event_key] = eventFollowed
                                    /*
                                    _eventsEventsFlow.postValue(
                                        EventRepository.EventDataEvent.OnChildChanged(event, null)
                                    )*/
                                } else {
                                    _eventsMap.value!!.put(eventFollowed.event_key, eventFollowed)
                                    /*
                                                         _eventsEventsFlow.postValue(
                                                             EventRepository.EventDataEvent.OnChildAdded(event, null)
                                                         )

                                     */
                                }

                                /*
                                                                if (!eventsJobsMap.containsKey(eventFollowed.event_key)) {

                                                                    eventsJobsMap[eventFollowed.event_key] =
                                                                        createEventUpdatesJob(eventFollowed.event_key)
                                                                }
                                                                eventsJobsMap[eventFollowed.event_key]?.start()
                                */


                            }


                            processEventsRelatedActions()
                            startFollowingEvents(userKey)
                            Resource.Success(true)
                        } else {
                            Resource.Error<Boolean?>(eventsList.message!!)
                        }
                    }

                    is Resource.Error -> {
                        Resource.Error<Boolean?>(eventsList.message!!)
                    }

                    is Resource.Loading -> {
                        Resource.Loading<Boolean?>(null)
                    }

                    else -> {}
                }

            }

        }
        return Resource.Loading<Boolean?>(null)
    }


    /*
        private suspend fun createEventUpdatesJob(eventKey: String) =  GlobalScope.launch(Dispatchers.IO) {
                eventRepository.getEventFlow(eventKey)?.collect { response ->
                    when (response) {
                        is Resource.Success -> {
                            if (response.data != null) {
                                var event = response.data!!
                                if (_eventsMap.value!!.containsKey(event.event_key)) {
                                    _eventsMap.value!![event.event_key] = event
                                    _eventsEventsFlow.postValue(
                                        EventRepository.EventDataEvent.OnChildChanged(event, null)
                                    )
                                } else {
                                    _eventsMap.value!!.put(event.event_key, event)
                                    _eventsEventsFlow.postValue(
                                        EventRepository.EventDataEvent.OnChildAdded(event, null)
                                    )
                                }

                                var currentEventsMap: HashMap<String, Event> = eventsMap.value!!

                                currentEventsMap.set(
                                    eventKey, response.data!!
                                )
                                _eventsMap.postValue(currentEventsMap)
                            } else {
                                var eventKey = eventKey

                                var mapUpdated: HashMap<String, Event> =
                                    _eventsMap.value!!.clone() as HashMap<String, Event>
                                mapUpdated.remove(eventKey)
                                _eventsMap.postValue(mapUpdated)


    //                        _eventsMap.value!!.remove(eventKey)
                                _eventsEventsFlow.postValue(
                                    EventRepository.EventDataEvent.OnChildRemoved(eventKey)
                                )
                            }
                        }

                        is Resource.Error -> {
                            Resource.Error<Boolean?>(response.message!!)
                        }

                        is Resource.Loading -> {
                            Resource.Loading<Boolean?>(null)
                        }

                        else -> {}
                    }

                    //   }

                }



            }
          */

    /**
     * escucha los cambios en la tabla de eventos seguidos
     */

    private var currentEventsList: java.util.ArrayList<EventFollowed> =
        java.util.ArrayList<EventFollowed>()

    val eventRepository by lazy { EventRepositoryImpl() }

    private val _eventsEventsFlow = MutableLiveData<EventRepository.EventDataEvent>()
    val eventsEventsFlow: LiveData<EventRepository.EventDataEvent> = _eventsEventsFlow

    fun startFollowingEvents(userKey: String) {


        var eventsRepository = EventsRepositoryImpl()

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val eventsList =
                    eventsRepository.getEventsListFlow(userKey)?.collect { response ->

                        _eventsFlow.postValue(response!!)
                        when (response) {
                            is Resource.Success -> {
                                if (response.data != null) {
                                    var dataEvent = response.data!!

                                    // Actualizo la lista de eventos
                                    when (dataEvent) {
                                        is EventsRepository.DataEvent.OnChildAdded -> {
                                            var eventFollowed = dataEvent.data
                                            if (!eventsJobsMap.containsKey(eventFollowed.event_key)) {
                                                /*
                                                                                            eventsJobsMap[eventFollowed.event_key] =
                                                                                                createEventUpdatesJob(eventFollowed.event_key)
                                                */
                                                eventsJobsMap[eventFollowed.event_key]?.start()
                                            }
                                        }

                                        is EventsRepository.DataEvent.OnChildRemoved -> {
                                            var eventFollowed = dataEvent.data

                                            eventsJobsMap[eventFollowed.event_key]?.let { job ->
                                                job.cancel()
                                                eventsJobsMap.values.remove(job)
                                                eventsMap?.value?.remove(eventFollowed.event_key)
                                            }


                                        }

                                        is EventsRepository.DataEvent.OnChildMoved -> TODO()
                                        is EventsRepository.DataEvent.OnError -> {

                                        }

                                        else -> {}
                                    }

                                    //                        _eventsFollowed.postValue(currentEventsList)
                                    _eventsListFlow.postValue(dataEvent)
                                    //instance.setEventsFollowed(response.data!!)
                                    // Elimino los eventos que ya no estan en la lista

                                    /*
                                                                    for (eventFollowed in currentEventsList) {
                                                                        if (!newEventsList.contains(eventFollowed)) {
                                                                            var index = newEventsList.indexOf(eventFollowed)
                                                                            if (index > -1) {
                                                                                newEventsList.removeAt(index)
                                                                                _eventsListFlow.postValue(
                                                                                    EventsRepository.DataEvent.ChildRemoved(
                                                                                        eventFollowed
                                                                                    )
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                    // Agrego los eventos que no estan en la lista
                                                                    for (eventFollowed in newEventsList) {
                                                                        if (!currentEventsList.contains(eventFollowed)) {
                                                                            currentEventsList.add(eventFollowed)
                                                                            _eventsListFlow.postValue(
                                                                                EventsRepository.DataEvent.ChildAdded(eventFollowed, null)
                                                                            )
                                                                        }
                                                                        else
                                                                        {
                                                                            var index = currentEventsList.indexOf(eventFollowed)
                                                                            currentEventsList.set(index, eventFollowed)
                                                                            _eventsListFlow.postValue(
                                                                                EventsRepository.DataEvent.ChildChanged(eventFollowed, null)
                                                                            )
                                                                        }
                                                                    }

                                                                    _eventsFollowed.postValue(newEventsList)
                                                                    processEventsRelatedActions()

                                                                    currentEventsList = newEventsList ?: java.util.ArrayList()
                                    */
                                    return@collect
                                } else {
                                    //_eventsFollowed.postValue(Resource.Error(response.message!!))

                                }
                            }

                            is Resource.Error -> {
                                // _eventsFollowed.postValue(Resource.Error(response.message!!))
                            }

                            is Resource.Loading -> {
                                // _eventsFollowed.postValue(Resource.Loading(null))
                            }
                        }
                    }
            }

        }

        return
    }


    /**
     * Retorna la cantidad de eventos activos del usuario
     */
    fun getEventsCount() = _eventsMap.value?.size ?: 0

    /**
     * Analiza de acuerdo a los cambios en el listado de eventos, como deben comportarse los distintos controles
     */
    private fun processEventsRelatedActions() {

        if (getEventsCount() > 0) {
            _enableMapIcon.postValue(true)
            _showEventsCounter.postValue(true)

            var newPanicStatus = isInPanic()
            if (_panic.value != newPanicStatus) {
                _panic.postValue(newPanicStatus)
            }
        } else {
            _enableMapIcon.postValue(false)
            _showEventsCounter.postValue(false)
            _panic.postValue(false)
        }
    }


    private fun appConfigsSubscribe() {
// Todo: Conectar esto ala APP2

        CoroutineScope(Dispatchers.IO).launch {
            ConfigRepositoryImpl().getConfigFlow()?.collect {
                var dataEvent: ConfigRepository.ConfigDataEvent = it.data!!
                //  dataEvent.data
            }
        }
    }


    private fun registerReceivers() {
        Log.d(this.javaClass.name, "Register Receivers")
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.BROADCAST_LOCATION_UPDATED)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_SERVICE_CONNECTED)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_DEVICE_CONNECTED)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_DEVICE_DISCONNECTED)
        intentFilter.addAction(BROADCAST_ACTION_BLE_DEVICE_REMOVE_FROM_AUTO_CONNECTION)
        intentFilter.addAction(BROADCAST_ACTION_BLE_DEVICE_ADD_TO_AUTO_CONNECTION)
        intentFilter.addAction(BROADCAST_ACTION_CANCEL_PANIC)
        intentFilter.addAction(BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED)
        intentFilter.addAction(BROADCAST_MESSAGE_SCAN_RESULT_UPDATED)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_DEVICE_DISCOVERED)
        intentFilter.addAction(BROADCAST_MESSAGE_BLE_REFRESH_DEVICES_DISCONNECTED)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            broadcastReceiver, intentFilter
        )
    }

    /* Supuestamente no se usa esto
    private fun connectToLocationService() {
        mLocationServiceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                Toast.makeText(applicationContext, "Service is disconnected", Toast.LENGTH_SHORT)
                    .show()
                mLocationService = null
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val mLocalBinder = service as LocationService.LocalBinder
                mLocationService = mLocalBinder.getServerInstance()
                mLocationService?.refresh()
            }
        }
        val intent = Intent(applicationContext, LocationService::class.java)
        bindService(intent, mLocationServiceConnection!!, Context.BIND_AUTO_CREATE)
    }
    */

    private fun createLocationServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "location_channel_id"
            val channelName = "Location Channel"
            val channelDescription = "Channel for location updates"

            val channel =
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            channel.description = channelDescription

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )

            notificationManager.deleteNotificationChannel(CHANNEL_DEFAULT_ID)
            notificationManager.deleteNotificationChannel(CHANNEL_ALARM_ID)
            //---------------- default
            val channelDefault = NotificationChannel(
                CHANNEL_DEFAULT_ID,
                CHANNEL_DEFAULT_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channelDefault.description = CHANNEL_DEFAULT_DESCRIPTION
            channelDefault.enableVibration(true)
            channelDefault.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channelDefault.setShowBadge(true)
            channelDefault.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channelDefault)

            //   channel.setSound(sound,audioAttributes)
            // Register the channel with the system
            val channelAlarms = NotificationChannel(
                CHANNEL_ALARM_ID, CHANNEL_ALARM_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            channelAlarms.description = CHANNEL_ALARM_DESCRIPTION
            channelAlarms.enableLights(true)
            channelAlarms.lightColor = Color.RED
            channelAlarms.enableVibration(true)
            channelAlarms.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channelAlarms.setShowBadge(true)
            channelAlarms.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            val soundSirenUri: Uri =
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.policesiren)
            if (soundSirenUri != null) {
                val audioAttributes =
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                channelAlarms.setSound(soundSirenUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channelAlarms)
        }
    }

    fun setCurrentActivity(activity: Activity) {
        currentActivity = activity
    }

    fun getCurrentActivity(): Activity? {
        return currentActivity
    }

    fun setRTLocationUpdate(isEnabled: Boolean) {
        val activity: Activity = instance.getCurrentActivity()!!

        SessionForProfile.getInstance(this)
            .setProfileProperty("RTLocationEnabled", isEnabled)/*
                if (!isEnabled && activity.isServiceRunning(LocationService::class.java)
                ) {
                    activity.stopService(Intent(activity.applicationContext, LocationService::class.java))
                } else {

                    activity.startService(Intent(activity.applicationContext, LocationService::class.java))
                }
        */
    }


    fun getEventStatus(
        eventStatus: String
    ): String {
        when (eventStatus) {
            EventStatusEnum.IDLE.name -> {
                return getString(R.string.status_idle)
            }

            EventStatusEnum.CONFIRMATION_REQUESTED.name -> {
                return getString(R.string.status_confirmation_required)
            }

            EventStatusEnum.CONFIRMATION_MISSING.name -> {
                return getString(R.string.status_confirmation_missing)
            }

            EventStatusEnum.USER_IN_TROUBLE.name -> {
                return getString(R.string.status_user_in_trouble)
            }

            EventStatusEnum.DANGER.name -> {
                return getString(R.string.status_user_in_danger)
            }

        }
        return return getString(R.string.status_unknown)
    }

    /*
        fun isPanicButtonActive(): Boolean {
            return isPanicButtonActive(false)
        }

        fun isPanicButtonActive(_ignoreVisibilityStatus: Boolean?): Boolean {


            var me = SessionForProfile.getInstance(this)
                .getUserProfile()

            var ignoreVisibilityStatus = false
            if (_ignoreVisibilityStatus != null) {
                ignoreVisibilityStatus = _ignoreVisibilityStatus
            }

            var events = (getCurrentActivity() as MainActivity).getEvents()
            var panicEventExists = false


            if (events != null) {
                events.forEach { event ->
                    if (event.author.author_key == FirebaseAuth.getInstance().uid.toString() &&
                        event.event_type == EventTypesEnum.PANIC_BUTTON.name &&
                        (ignoreVisibilityStatus || event.visibility != EventVisibilityTypes.HIDDEN_FOR_AUTHOR.name)
                    ) {
                        panicEventExists = true
                        return@forEach
                    }
                }
                return panicEventExists
            }
            return false
        }
    */
    fun getPanicEventKey(): String? {
        var panicEventKey: String? = null
        _eventsMap.value?.values?.forEach { event ->
            if (event.author?.author_key == FirebaseAuth.getInstance().uid.toString() && event.event_type == EventTypesEnum.PANIC_BUTTON.toString()) return event.event_key

        }
        return null
    }

    fun getPanicEvent(): EventFollowed? {
        val me = SessionForProfile.getInstance(this).getUserProfile()
        var panicEvent: EventFollowed? = null
        if (me.events != null) {
            me.events.forEach { event ->
                if (event.author.author_key == SessionForProfile.getInstance(
                        applicationContext
                    )
                        .getUserId() && event.event_type == EventTypesEnum.PANIC_BUTTON.name
                ) {
                    panicEvent = event
                    return@forEach
                }
            }
            return panicEvent
        }
        return null
    }


    fun isTrackingMe(): Boolean {

        var shouldTrackMe = false
        val me = SessionForProfile.getInstance(this).getUserProfile()

        if (me.events != null) {
            me.events.forEach { event ->
                if (event.author.author_key == SessionForProfile.getInstance(
                        applicationContext
                    )
                        .getUserId() && event.event_type == EventTypesEnum.PANIC_BUTTON.name
                ) {
                    shouldTrackMe = true
                    return@forEach
                } else {
                    var pepe = 2
                }

            }
            return shouldTrackMe
        }

        return false
    }

    fun enableFallingSensor() {
        if (applicationContext.isServiceRunning(FallDetectionServiceMethod::class.java) == false) {
            SessionForProfile.getInstance(this.applicationContext)
                .setProfileProperty("falling_sensor", true)
            startService(Intent(this, FallDetectionServiceMethod::class.java))
        }
    }

    fun disableFallingSensor() {
        if (instance.currentActivity?.isServiceRunning(FallDetectionServiceMethod::class.java) == true) {
            SessionForProfile.getInstance(this.applicationContext)
                .setProfileProperty("falling_sensor", false)
            stopService(Intent(this, FallDetectionServiceMethod::class.java))
        }
    }


    fun checkIfUserEventExists(eventType: EventTypesEnum): Boolean {

        val me = SessionForProfile.getInstance(this).getUserProfile()
        var eventTypeExists = false
        if (me.events != null) {
            me.events.forEach { event ->
                if (event.author.author_key == SessionForProfile.getInstance(
                        applicationContext
                    )
                        .getUserId() && event.event_type == eventType.name
                ) {
                    eventTypeExists = true
                    return@forEach
                }
            }
            return eventTypeExists
        }
        return false
    }

    // Verifica si se podria disparar el evento de caida ante la inexistencia de un evento mas grave.
    fun shouldDispatchFallingEvent(): Boolean {
        var response = true
        val me = SessionForProfile.getInstance(this).getUserProfile()
        if (me.events != null && me.events.size > 0) {
            me.events.forEach { event ->
                // Solo controlo los eventos creados por mi.
                if (event.author.author_key == SessionForProfile.getInstance(
                        applicationContext
                    )
                        .getUserId()
                ) {
                    if (event.event_type == EventTypesEnum.PANIC_BUTTON.name) {
                        response = false
                        return@forEach
                    } else if (event.event_type == EventTypesEnum.FALLING_ALARM.name) {
                        response = false
                        return@forEach
                    }
                }
            }
        }
        return response
    }


    // Verifica si se podria disparar el evento de caida ante la inexistencia de un evento mas grave.
    fun existingFallingEventKey(): String? {
        var eventKey: String? = null
        val me = SessionForProfile.getInstance(this).getUserProfile()
        if (me.events != null) {
            if (me.events.size > 0) {
                me.events.forEach { event ->
                    // Solo controlo los eventos creados por mi.
                    if ((event.author.author_key == FirebaseAuth.getInstance().uid.toString()) && (event.event_type == EventTypesEnum.FALLING_ALARM.name)) {
                        eventKey = event.event_key
                        return@forEach
                    }
                }
            }
        }
        return eventKey
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        isInForeground = event == Lifecycle.Event.ON_RESUME
    }

    fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") || Build.FINGERPRINT.startsWith(
            "generic"
        ) || Build.FINGERPRINT.startsWith("unknown") || Build.HARDWARE.contains("goldfish") || Build.HARDWARE.contains(
            "ranchu"
        ) || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains(
            "Android SDK built for x86"
        ) || Build.MANUFACTURER.contains("Genymotion") || Build.PRODUCT.contains("sdk_google") || Build.PRODUCT.contains(
            "google_sdk"
        ) || Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains("sdk_x86") || Build.PRODUCT.contains(
            "sdk_gphone64_arm64"
        ) || Build.PRODUCT.contains("vbox86p") || Build.PRODUCT.contains("emulator") || Build.PRODUCT.contains(
            "simulator"
        ))
    }


    fun setCurrentMediaPlayer(player: MediaPlayer?) {
        this.mediaPlayer = player
    }

    fun getCurrentMediaPlayer(): MediaPlayer? {
        return this.mediaPlayer
    }

    /*
        fun setCurrentSubscription(subscription: Subscription) {
            _userSubscription.postValue(subscription)
        }

        fun getCurrentSubscriptionKey(): String {
            return this.userSubscription.subscription_type_key
        }
    */

    fun fetchCurrentSubscriptionType(callback: OnCompleteCallback) {
        GlobalScope.launch(Dispatchers.IO) {
            var resource =
                subscriptionTypesRepository.getSubscriptionType(this@AppClass.userSubscription.value?.subscription_type_key!!)
            if (resource.message == null) {
                this@AppClass.userCurrentPlan = resource.data!!
                callback.onComplete(true, resource.data)
            } else {
                callback.onError(java.lang.Exception(resource.message))
            }
        }
    }

    /*
    fun fetchCurrentSubscriptionType() {
        GlobalScope.launch(Dispatchers.IO) {
            try {

                //     this@AppClass.subscriptionType.value?.data?.subscription_type_key

                var subscriptionType =
                    this@AppClass.userSubscription.value?.subscription_type_key!!
                var resource =
                    subscriptionTypesRepository.getSubscriptionType(subscriptionType)
                if (resource.message == null) {
                    _subscriptionType.postValue(
                        Resource.Success<SubscriptionTypes?>(
                            resource.data!!
                        )
                    )
                } else {
                    _subscriptionType.postValue(
                        Resource.Error<SubscriptionTypes?>(
                            resource.message ?: "".toString()
                        )
                    )

                }

            } catch (ex: Exception) {
                var pp = 3
            }
        }
    }


    fun getCurrentSubscriptionType(): SubscriptionTypes {
        return this@AppClass.userCurrentPlan
    }
*/
    /*
        fun isFreeUser(): Boolean {

            return this@AppClass.userCurrentPlan.access_level == AccessLevelsEnum.FREE.ordinal // 0 = Free; 1 = Solidario ; 2 = VIP
        }
    */
    fun startLocationServices() {/*
            // can be schedule in this way also
            //  Utils.scheduleJob(this, LocationUpdatesService.class);
            //doing this way to communicate via messenger
            // Start service and provide it a way to communicate with this class.
            // can be schedule in this way also
              JobServicesUtils.scheduleJob(this, LocationUpdatesService::class.java);
            //doing this way to communicate via messenger
            // Start service and provide it a way to communicate with this class.
      */


        /* estoooo
        var context: Context = AppClass.instance
        var activity = AppClass.instance.getCurrentActivity()
        var serviceIntent = Intent(context, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppClass.instance.getLocationService()?.showNotificationAndStartForegroundService()
            AppClass.instance.getLocationService()?.startListeningLocation()
        }
    */
    }

    fun subscribeToUnreadMessages() {
// es un subscribe para las tablas que atienden a toda la app.
        // si funciona bien, puedo agregarle el tema de las notificaciones
        //unreadMessagesRef.addValueEventListener(unreadMessagesListener)
    }

    private fun unSubscribeToUnreadMessages() {

        /*
        TODO: Implementar esto bien
        unreadMessagesRef.removeEventListener(unreadMessagesListener)

         */

    }


    private fun broadcastUnreadMessagesUpdate(hashMapUnreads: HashMap<String, UnreadMessages>) {
        // dispersa el mensaje de la nueva actualizacion de mensajes no leidos.
        val intent = Intent(Constants.BROADCAST_UNREAD_MESSAGES_UPDATES) //action: "msg"
        intent.setPackage(packageName)
        intent.putExtra("data", hashMapUnreads)

        sendBroadcast(intent)

    }


    /*

        fun checkIfLogged(intent: Intent, callback: OnLoginCallback) {

            Log.d("INGRESO", "checkIfLoged")
            val emailLink: String? = intent.data?.toString()

            if (FirebaseAuth.getInstance().currentUser != null) {

                Log.d("INGRESO", "recargo el usuario desde FBA")

                FirebaseAuth.getInstance().currentUser!!.reload()
                    .addOnCompleteListener(object :
                        OnCompleteListener<Void?> {
                        override fun onComplete(task: Task<Void?>) {
                            Log.d("INGRESO", "volvio de recargar el usuario desde FBA")
                            //User has been disabled, deleted or login credentials are no longer valid,
                            //so send them to Login screen
                            if (task.isSuccessful())
                                when ((FirebaseAuth.getInstance().currentUser!!.providerData[1] as UserInfo).providerId) {
                                    "email" -> {
                                        if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                                            callback.onLoggedIn()
                                        } else {
                                            callback.onEmailNotVerified()
                                        }
                                    }
                                    "password" -> {
                                        if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                                            callback.onLoggedIn()
                                        } else {
                                            callback.onEmailNotVerified()
                                        }
                                    }
                                    "google.com" -> {
                                        if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified) {
                                            callback.onLoggedIn()
                                        } else {
                                            callback.onEmailNotVerified()
                                        }
                                    }

                                    "phone" -> {
                                        var userInfo =
                                            (FirebaseAuth.getInstance().currentUser!!.providerData[1] as UserInfo)
                                        UsersWSClient.instance.verifyIfPhoneNumberExists(userInfo.phoneNumber.toString(),
                                            object : OnCompleteCallback {
                                                override fun onComplete(
                                                    success: Boolean,
                                                    result: Any?
                                                ) {
                                                    if (success) {
                                                        callback.onLoggedIn()
                                                    } else {
                                                        callback.onNotLogged()
                                                    }

                                                }

                                                override fun onError(exception: Exception) {
                                                    super.onError(exception)
                                                    SessionForProfile.getInstance(AppClass.instance)!!
                                                        .logout()
                                                    callback.onNotLogged()
                                                }
                                            }
                                        )


                                    }
                                } else callback.onNotLogged()
                        }


                    }).addOnFailureListener {
                        var pp = 3
                    }.addOnCanceledListener {
                        var qq = 3
                    }


            } else
                if (emailLink != null && emailLink.isEmpty() == false) {
                    Log.d("EMAIL_REGISTRATION", emailLink)
                    Toast.makeText(this.currentActivity, emailLink, Toast.LENGTH_LONG).show()
                    var email: String =
                        SessionForProfile.getInstance(this.currentActivity!!)
                            ?.getProfileProperty("PENDING_EMAIL")
                            .toString()
    //                signInWithEmailLink(email, emailLink)
                } else {
                    callback.onNotLogged()
                }

        }
    */


//private var bleService: ITagService? = null  // Servicio de bluetooth
    /*
        private var devicesList: RecyclerView? = null
        private var devicesSwipeRefresh: SwipeRefreshLayout? = null
        val bluetoothAdapter: BluetoothAdapter by lazy {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }
    */
    /**
     * Conexion al Servicio de Bluetooth
     */
    var bleServiceConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder) {

            /*
            Toast.makeText(
                applicationContext,
                "BLE Service Running and Connected",
                Toast.LENGTH_LONG
            )
                .show()
            val binder: ITagService.ServiceBinder = service as ITagService.ServiceBinder
            bleService = binder.service
    */
            broadcastMessage(null, BROADCAST_MESSAGE_BLE_SERVICE_CONNECTED)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Toast.makeText(
                applicationContext,
                "BLE Service Disconnected",
                Toast.LENGTH_LONG
            ).show()
            broadcastMessage(null, BROADCAST_MESSAGE_BLE_SERVICE_DISCONNECTED)
        }
    }


    /*
    private val scanResults =
        mutableListOf<ScanResult>() // Lista donde se almacenaran los resultados del scaneo
    *//*
      private val scanResultAdapter: ScanResultAdapter by lazy {
          ScanResultAdapter(this, scanResults) { result ->
              // User tapped on a scan result
              with(result) {
                  Log.w("ScanResultAdapter", "Connecting to $address")
                  bleService?.connect(address)
              }


          }
      }
    */
    fun setupITagAsPanicButton() {


    }

    /*
        fun getBLEService(): ITagService? = bleService
    */
    fun startBLEService() {
        //      if (SessionApp.getInstance(applicationContext).isBTPanicButtonEnabled) {
        applicationContext.startBLEService(bleServiceConn)
        //     }
    }


    fun stopBLEService() {
        applicationContext.stopBLEService(bleServiceConn)

        Toast.makeText(applicationContext, "Service stopped", Toast.LENGTH_SHORT).show()
    }

    /*
        @SuppressLint("MissingPermission")
        fun connect(bleDevice: BluetoothDevice) {
            bleService?.connect(bleDevice)
        }
    *//*
    override fun onScanResultUpdate(scanResult: ScanResult) {
        /*
         var registeredDevice = mutableListOf<String>( "FF:FF:DD:D8:84:A1" )
         if (scanResult.device != null && registeredDevice.indexOf(scanResult.device.address.toString()) > -1) {
             bleService?.connect(scanResult.device.address.toString())
         }
         */
        var pp = 33


        if (SessionApp.getInstance(applicationContext).bleDevicesToConnect.contains(scanResult.device.address)) {


        }


    }
*//*
        override fun onScanFailed(errorCode: Int) {
            TODO("Not yet implemented")
        }

        override fun onDeviceConnected(deviceAddress: String) {
            // TODO("Not yet implemented")
            Toast.makeText(this, "Device Connected", Toast.LENGTH_SHORT).show()

        }
    *//*
        override fun onDeviceDesconnected(deviceAddress: String) {
            TODO("Not yet implemented")
        }

        override fun onPanicButtonPressed() {
            broadcastPanicButtonPressedMessage()
        }

        override fun onBatteryLevelUpdate(address: String, value: Int) {
            TODO("Not yet implemented")
        }

    */
    fun broadcastPanicButtonPressedMessage() {
        val intent: Intent = Intent(BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED)
        intent.setPackage(applicationContext.packageName)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    fun handleError(e: IOException, b: Boolean) {

    }


    fun handleError(@NonNull th: Throwable, toast: Boolean) {
        Handler(Looper.getMainLooper()).post {

            if (applicationContext == null) {
                Log.e("LT", "Attempt to handle error before application created", th)
                // FirebaseCrashlytics.getInstance().recordException(th);
            } else {
                Log.e("LT", "Toasted", th)
                if (toast) {
                    Toast.makeText(applicationContext, th.message, Toast.LENGTH_LONG).show()
                }
                // FirebaseCrashlytics.getInstance().recordException(th);
            }
        }
    }

    fun handleError(th: Throwable?, toast: Int) {
        Handler(Looper.getMainLooper()).post {
            if (applicationContext == null) {
                Log.e("LT", "Attempt to handle error before application created", th)
                // FirebaseCrashlytics.getInstance().recordException(th);
            } else {
                Log.e("LT", "Toasted", th)
                Toast.makeText(applicationContext, toast, Toast.LENGTH_LONG).show()
                // FirebaseCrashlytics.getInstance().recordException(th);
            }
        }
    }

    fun handleError(th: Throwable?) {
        if (th != null) {
            handleError(th, BuildConfig.DEBUG)
        }
    }

    fun fa(@NonNull event: String, bundle: Bundle?) {
        //if (mContext == null) return
        /*
        if (sFirebaseAnalytics == null) {
            sFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
        sFirebaseAnalytics.logEvent(event, bundle);
         */
        if (BuildConfig.DEBUG) {
            Log.d("LT", "FA log $event")
        }
    }

    fun fa(@NonNull event: String) {
        fa(event, null)
    }

    fun faAppCreated() {
        fa("itag_app_created")
    }

    fun faNoBluetooth() {
        fa("itag_no_bluetooth")
    }

    fun faBluetoothDisable() {
        fa("itag_bluetooth_disable")
    }

    fun faNotITag() {
        fa("itag_not_itag")
    }

    fun faScanView(empty: Boolean) {
        val bundle = Bundle()/*
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itag_scan_view_is_empty");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "First Scan");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "boolean");
        bundle.putBoolean(FirebaseAnalytics.Param.VALUE, empty);
         */
        fa("itag_scan_view", bundle)
    }

    fun faITagsView(devices: Int) {
        val bundle = Bundle()/*
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itag_itags_view_device");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Remembered Devices");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "int");
        bundle.putInt(FirebaseAnalytics.Param.VALUE, devices);
         */
        fa("itag_itags_view")
    }

    fun faSuspiciousDisconnect5() {
        fa("itag_suspicious_disconnect_5")
    }

    fun faSuspiciousDisconnect10() {
        fa("itag_suspicious_disconnect_10")
    }

    fun faSuspiciousDisconnect30() {
        fa("itag_suspicious_disconnect_30")
    }

    fun faSuspiciousDisconnectLong() {
        fa("itag_suspicious_disconnect_long")
    }

    fun faRememberITag() {
        fa("itag_remember_itag")
    }

    fun faForgetITag() {
        fa("itag_forget_itag")
    }

    fun faNameITag() {
        fa("itag_set_name")
    }

    fun faColorITag() {
        fa("itag_set_color")
    }

    fun faMuteTag() {
        fa("itag_mute")
    }

    fun faUnmuteTag() {
        fa("itag_unmute")
    }

    fun faFindITag() {
        fa("itag_find_itag")
    }

    fun faITagFound() {
        fa("itag_itag_found")
    }

    fun faITagFindStopped() {
        fa("itag_itag_find_stopped")
    }

    fun faITagLost(error: Boolean) {
        val bundle = Bundle()/*
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itag_itag_lost_error");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Lost with error");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "boolean");
        bundle.putBoolean(FirebaseAnalytics.Param.VALUE, error);
         */
        fa("itag_itag_lost")
    }

    fun faFindPhone() {
        fa("itag_find_phone")
    }

    fun faITagDisconnected() {
        fa("itag_user_disconnect")
    }

    fun faITagConnected() {
        fa("itag_user_connect")
    }

    fun faShowLastLocation() {
        fa("itag_show_last_location")
    }

    fun faIssuedGpsRequest() {
        fa("itag_issued_gps_request")
    }

    fun faRemovedGpsRequestBySuccess() {
        fa("itag_removed_gps_request_by_success")
    }

    fun faRemovedGpsRequestByConnect() {
        fa("itag_removed_gps_request_by_connect")
    }

    fun faRemovedGpsRequestByTimeout() {
        fa("itag_removed_gps_request_by_timeout")
    }

    fun faGotGpsLocation() {
        fa("itag_got_gps_location")
    }

    fun faGpsPermissionError() {
        fa("itag_gps_permission_error")
    }

    fun faDisconnectDuringCall() {
        fa("itag_disconnect_during_call")
    }

    fun faWtOff() {
        fa("itag_wt_off")
    }

    fun faWtOn1() {
        fa("itag_wt_on1")
    }


    //------------------- PANIC BUTTONS ------------------//
    @SuppressLint("MissingPermission")
    fun onEmergencyButtonPressed() {

        Log.d("ITags", "onEmergencyButtonPressed()")

        // lo que sigue deberia adaptarse

        if (itagPressMode == ITagModesEnum.active && !isInPanic()) {
            Log.d("ITags", " Ingrese porque no esta en panico")
            if (permissionsVibration()) {
                val buzzer =
                    getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
                buzzer.let {
                    val pattern = longArrayOf(0, 200, 100, 300, 100, 2000)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        //deprecated in API 26
                        buzzer.vibrate(pattern, -1)
                    }
                }
            }
            Log.d("ITags", " ya vibro!!!")
            _postingPanicButtonStatus.value = Resource.Loading<Event?>()

            SessionApp.getInstance(this).isInPanic(true)

            if (MainActivityViewModel.getInstance(this).isFreeUser())
            {

      //          broadcastMessage(null, BROADCAST_ACTION_REFRESH_PANIC_BUTTON)

                // como es usuario gratuito lo que va a hacer es adquirir la ubicacion y envia un SMS con la ubicacion

                val locationManager: LocationManager =
                    getSystemService(Context.LOCATION_SERVICE) as LocationManager

                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        CoroutineScope(Dispatchers.IO).launch {
                            SMSUtils.sendLocationSMS(
                                "Nombre del Contacto", "+5491161274148", location
                            )

                        }
                    }

                    override fun onLocationChanged(locations: MutableList<Location>) {
                        super.onLocationChanged(locations)
                    }

                    override fun onStatusChanged(
                        provider: String?,
                        status: Int,
                        extras: Bundle?
                    ) {
                        // super.onStatusChanged(provider, status, extras)
                    }

                }
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 30, 0F, locationListener
                )


            }
            else
            {

                Log.d("ITags", "Voy a llamar a preparePanicEvent")
                GlobalScope.launch(Dispatchers.IO) {
                    val call = preparePanicEventSuspend()
                    if (call.message == null) {
                        Log.d("ITags", " Deberia haber grabado bien")
                        _postingPanicButtonStatus.postValue(call)
                    } else {
                        _postingPanicButtonStatus.postValue(
                            Resource.Error<Event?>(
                                call.message
                            )
                        )
                    }
                }
            }
        } else {
            Log.d("ITags", " Ya estabas en panico")

            Toast.makeText(this, "You already are in PANIC!!!", Toast.LENGTH_SHORT).show()
        }


    }

    suspend fun preparePanicEventSuspend(): Resource<Event?> {
        return try {
            val newEvent = Event()
            newEvent.author_key =
                SessionForProfile.getInstance(applicationContext).getUserId()
            newEvent.event_type = EventTypesEnum.PANIC_BUTTON.name
            newEvent.status = EventStatusEnum.DANGER.name
            newEvent.event_location_type = EventLocationType.REALTIME.name

            val location = lastLocation.value
            if (location != null) {
                val latLng = LatLng(
                    instance.lastLocation.value?.latitude!!,
                    instance.lastLocation.value?.longitude!!
                )

                newEvent.location = EventLocation()
                newEvent.location?.latitude = latLng.latitude
                newEvent.location?.longitude = latLng.longitude
                val geoLocationAtCreation = GeoLocation()
                geoLocationAtCreation.l = ArrayList<Double>()
                (geoLocationAtCreation.l as ArrayList<Double>).add(latLng.latitude)
                (geoLocationAtCreation.l as ArrayList<Double>).add(latLng.longitude)
                geoLocationAtCreation.event_time = newEvent.time
                newEvent.location_at_creation = geoLocationAtCreation

                // Publish the Panic Event

                val callbackPublish: OnCompleteCallback = object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        val eventComplete = result as Event
                    }
                }

                Log.d("ITags", " llamo a publshPanicEventSuspend")

                val call = publishPanicEventSuspend(newEvent, callbackPublish)
                Log.d("ITags", " volcio de publshPanicEventSuspend")

                if (call.message == null) {
                    _panic.postValue(true)
                    return Resource.Success<Event?>(newEvent)
                } else Resource.Error<Event?>(call.message)
            } else {
                return Resource.Error<Event?>("error_location_not_available")
            }
        } catch (e: Exception) {
            return Resource.Error<Event?>("error_location_not_available")
        }

    }

    /*
        private fun preparePanicEvent() {

            val newEvent = Event()
            newEvent.author_key = SessionForProfile.getInstance(applicationContext).getUserId()
            newEvent.event_type = EventTypesEnum.PANIC_BUTTON.name
            newEvent.status = EventStatusEnum.DANGER.name
            newEvent.event_location_type = EventLocationType.REALTIME.name
            //   GeoFunctions.getInstance(AppClass.instance).getLastKnownLocation { location ->

            val latLng = LatLng(
                AppClass.instance.lastLocation.value?.latitude!!,
                AppClass.instance.lastLocation.value?.longitude!!
            )
            newEvent.location = EventLocation()
            newEvent.location?.latitude = latLng.latitude
            newEvent.location?.longitude = latLng.longitude
            val geoLocationAtCreation = GeoLocation()
            geoLocationAtCreation.l = ArrayList<Double>()
            (geoLocationAtCreation.l as ArrayList<Double>).add(latLng.latitude)
            (geoLocationAtCreation.l as ArrayList<Double>).add(latLng.longitude)
            geoLocationAtCreation.event_time = newEvent.time
            newEvent.location_at_creation = geoLocationAtCreation

            // Publish the Panic Event

            val callbackPublish: OnCompleteCallback = object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {
                    val eventComplete = result as Event
                }
            }

            publishPanicEvent(newEvent, callbackPublish)
            //    }

        }
    */

    fun publishPanicEvent(event: Event, iCallback: OnCompleteCallback?) {
        //        mCallback.showLoader(mActivity.getString(R.string.registering_event))
        // TODO : Pasarlo al servidor.
        // # 1) Grabar el evento.
        // # 2) Buscar a los amigos.
        // # 3) Enviar las notificaciones.
        //   var eventsRef = FirebaseDatabase.getInstance()
        //       .getReference(TABLE_EVENTS_LOCATIONS)

        MainActivityViewModel.getInstance().postPanicEvent(event)


        /*
                GlobalScope.launch(Dispatchers.IO) {

                    Log.d("ITags", "Termine de publicar el evento")
                    event.media?.forEach { event ->
                        if (event.media_type == MediaType.VIDEO ||
                            event.media_type == MediaType.AUDIO ||
                            event.media_type == MediaType.IMAGE
                        ) {
                            val fileExtension = FileUtils.getFileExtension(event.file_name)
                            var fileUri = event.file_name
                            if (fileExtension?.lowercase(Locale.getDefault()) == "jpg") {
                                fileUri = "file:" + event.file_name
                            }
                            var mediaFileEncoded: String? = null
                            if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" ||
                                fileExtension?.lowercase(Locale.getDefault()) == "mp4" ||
                                fileExtension?.lowercase(Locale.getDefault()) == "3gp"
                            ) {

                                mediaFileEncoded =
                                    MultimediaUtils.convertFileToBase64(Uri.parse(fileUri)).toString()
                            }
                            event.bytesB64 = mediaFileEncoded
                        }


                    }


                    var call = eventsRepository.postEvent(event);

                    if (call.message != null) {
                        iCallback?.onComplete(true, call.data)
                    } else {
                        iCallback?.onError(java.lang.Exception(call.message))
                    }
                }
        */

        /*
                EventsWSClient.instance.postEvent(event, object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        if (success) {
                            if (result is Event) {

                                iCallback?.onComplete(true, result)
                            }
                        }
                    }

                    override fun onError(exception: java.lang.Exception) {
                        iCallback?.onError(exception)
                    }
                })
        */
    }

    suspend fun publishPanicEventSuspend(
        event: Event, iCallback: OnCompleteCallback?
    ): Resource<Event?> {
        //        mCallback.showLoader(mActivity.getString(R.string.registering_event))
        // TODO : Pasarlo al servidor.
        // # 1) Grabar el evento.
        // # 2) Buscar a los amigos.
        // # 3) Enviar las notificaciones.
        //   var eventsRef = FirebaseDatabase.getInstance()
        //       .getReference(TABLE_EVENTS_LOCATIONS)

        return postPanicEventSuspend(event)


        /*
                GlobalScope.launch(Dispatchers.IO) {

                    Log.d("ITags", "Termine de publicar el evento")
                    event.media?.forEach { event ->
                        if (event.media_type == MediaType.VIDEO ||
                            event.media_type == MediaType.AUDIO ||
                            event.media_type == MediaType.IMAGE
                        ) {
                            val fileExtension = FileUtils.getFileExtension(event.file_name)
                            var fileUri = event.file_name
                            if (fileExtension?.lowercase(Locale.getDefault()) == "jpg") {
                                fileUri = "file:" + event.file_name
                            }
                            var mediaFileEncoded: String? = null
                            if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" ||
                                fileExtension?.lowercase(Locale.getDefault()) == "mp4" ||
                                fileExtension?.lowercase(Locale.getDefault()) == "3gp"
                            ) {

                                mediaFileEncoded =
                                    MultimediaUtils.convertFileToBase64(Uri.parse(fileUri)).toString()
                            }
                            event.bytesB64 = mediaFileEncoded
                        }


                    }


                    var call = eventsRepository.postEvent(event);

                    if (call.message != null) {
                        iCallback?.onComplete(true, call.data)
                    } else {
                        iCallback?.onError(java.lang.Exception(call.message))
                    }
                }
        */

        /*
                EventsWSClient.instance.postEvent(event, object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        if (success) {
                            if (result is Event) {

                                iCallback?.onComplete(true, result)
                            }
                        }
                    }

                    override fun onError(exception: java.lang.Exception) {
                        iCallback?.onError(exception)
                    }
                })
        */
    }

    /*
        fun isInPanic(): Boolean {
            return _panic.value ?: false
        }
    */


    suspend fun postPanicEventSuspend(event: Event): Resource<Event?> {
        return try {
            Log.d("ITags", "Termine de publicar el evento")
            event.media?.forEach { event ->
                if (event.media_type == MediaTypesEnum.VIDEO || event.media_type == MediaTypesEnum.AUDIO || event.media_type == MediaTypesEnum.IMAGE) {
                    val fileExtension = event.file_name.getFileExtension(applicationContext)
                    var fileUri = event.file_name
                    if (fileExtension?.lowercase(Locale.getDefault()) == "jpg") {
                        fileUri = "file:" + event.file_name
                    }
                    var mediaFileEncoded: String? = null
                    if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                            Locale.getDefault()
                        ) == "mp4" || fileExtension?.lowercase(Locale.getDefault()) == "3gp"
                    ) {

                        mediaFileEncoded =
                            MultimediaUtils(applicationContext).convertFileToBase64(
                                Uri.parse(
                                    fileUri
                                )
                            ).toString()
                    }
                    event.bytesB64 = mediaFileEncoded
                }
            }

            val call: Resource<Event?> = eventsRepository.postEvent(event)

            return if (call.message == null) {
                Resource.Success<Event?>(call.data)
            } else {
                Resource.Error<Event?>(call.message.toString())
            }
        } catch (ex: Exception) {
            Resource.Error<Event?>(ex.message ?: "".toString())
        }

    }

    /**
     * Recorre la lista de eventos a los que esta subscripto el usuario y verifica si hay alguno de tipo PANIC_BUTTON
     * y retorna true si ha disparado un evento
     */

    private fun isInPanic(): Boolean {
        var toReturn = false
        eventsMap.value?.values?.forEach { event ->
            if (event.author?.author_key == FirebaseAuth.getInstance().uid.toString() && event.event_type == EventTypesEnum.PANIC_BUTTON.toString()) toReturn =
                true
            return@forEach
        }
        return toReturn

    }


    fun setInPanic() {
        _panic.postValue(true)
    }

    fun enableBluetoothPeripherals() {
        BleManager.getInstance().init(this)
        SessionApp.getInstance(this).isBTPanicButtonEnabled(true)
    }

    fun disableBluetoothPeripherals() {
//        BleManager.getInstance().disableBluetooth()
        SessionApp.getInstance(this).isBTPanicButtonEnabled(false)

    }

    fun onDeviceLocationChanged(location: Location?) {
        _lastLocation.postValue(location)
    }/*
        fun onPanicButtonPressed() {
            onEmergencyButtonPressed()
            // _dispatchPanicButtonPressedEvent.value = true
        }

    */

    /**
     * Inicializa e implementa la funcionalidad principal de los ITags
     */

    private val disposableBag: DisposableBag = DisposableBag()

    fun initializeITags() {
        disposableBag.add(ITag.store.observable().subscribe { event ->
            var pp = 3
        })
        for (i in 0 until ITag.store.count()) {
            val itag = ITag.store.byPos(i) ?: continue
            val id = itag.id()
            val connection: BLEConnectionInterface = ble.connectionById(id)
            disposableBag.add(connection.observableRSSI().subscribe { rssi ->
                var ppp = 3
                //    updateRSSI(id, rssi)
            })
            disposableBag.add(connection.observableImmediateAlert().subscribe { state ->
                var pppp = 3/*
                updateITagImageAnimation(
                    itag,
                    connection
                )
                */
            })
            disposableBag.add(connection.observableState().subscribe { state ->
                var ppp = 3/*
                    getActivity().runOnUiThread(
                        Runnable {
                            if (BuildConfig.DEBUG) {
                                Log.d(
                                    ITagsFragment.LT,
                                    "connection " + id + " state changed " + connection.state()
                                        .toString()
                                )
                            }

                            updateAlertButton(id)
                            updateState(id, state)
                            updateITagImageAnimation(itag, connection)
                            if (connection.state() === BLEConnectionState.connected) { //isConnected()) {
                                connection.enableRSSI()
                            } else {
                                connection.disableRSSI()
                                updateRSSI(id, -999)
                            }
                        })
                    */
            })
            disposableBag.add(connection.observableClick().subscribe { event ->

                //    onEmergencyButtonPressed()
                /*
                  updateITagImageAnimation(
                      itag,
                      connection
                  )

                 */
            })
        }
    }

    fun updateSubscriptionPlan(subscriptionType: SubscriptionTypes) {
        _subscriptionType.postValue(Resource.Success<SubscriptionTypes?>(subscriptionType))
        _isFreeUser.postValue(subscriptionType.access_level == AccessLevelsEnum.FREE.ordinal)
    }


//  val viewsStack = ArrayDeque<TopViewModel>()

    /**
     * Almacena los datos de modulo, fragmento y clave del fragmento visible
     *//*
      fun addViewToStack(module: IANModulesEnum, view: Any, eventKey: String? = null) {
          viewsStack.addLast(TopViewModel(module, view, eventKey))
      }
  */
//* modifica una entrada de la pila que coincida en modulo y fragmento
    /*
      fun updateViewFromStack(module: IANModulesEnum, view: Any, eventKey: String? = null) {
          // Encuentra el índice del primer objeto que coincide con una determinada propiedad
          val index = viewsStack.indexOfFirst { it.view == view }
          // Elimina ese objeto de la lista
          if (index != -1) {
              viewsStack.removeAt(index)
              // Agrega el nuevo objeto en la misma posición
              viewsStack.add(index, TopViewModel(module, view, eventKey))
          }
          else
          {
              // Agrega el nuevo objeto en la misma posición
              viewsStack.addLast(TopViewModel(module, view, eventKey))
          }

      }
    */


    /**
     * Remueve el ultimo fragmento de la pila
     *//*
        fun removeViewFromStack(view: Any) {
            // Encuentra el índice del primer objeto que coincide con una determinada propiedad
            val index = viewsStack.indexOfFirst { it.view == view }

            // Elimina ese objeto de la lista
            if (index != -1) {
                viewsStack.removeAt(index)
            }
        }
    */
    /**
     * Devuelve el ultimo fragmento de la pila
     *//*
      fun getLastViewFromStack(): TopViewModel? {
          return viewsStack.lastOrNull()
      }
  *//*
    fun clearStack() {
        viewsStack.clear()
    }
*/
    fun setMainActivityRef(activity: MainActivity) {
        this.mainActivityRef = activity
    }

    fun getMainActivityRef(): MainActivity? {
        return this.mainActivityRef
    }

    fun setUserSubscription(subscriptionType: SubscriptionTypes) {
        updateSubscriptionPlan(subscriptionType)
    }/*
        fun setEventsFollowed(data: ArrayList<EventFollowed>) {
            this._eventsFollowed.postValue(data)
        }
    */

    fun setCurrentFragment(fragment: androidx.fragment.app.Fragment?) {
        this.currentFragment = fragment
    }

    fun getActiveFragment(): androidx.fragment.app.Fragment? {
        return this.currentFragment
    }


    /**
     * Elimina un evento del mapa de eventos.
     */
    fun onEventRemoved(eventKey: String) {
        this.eventsFollowed.remove(eventKey)
    }




    private var orignalSoftMode: Int = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
    private var softMode: Int = orignalSoftMode


    /**
     * Cambia el comportamiento de la actividad ante la apertura del teclado virtual
     */

    fun setSoftMode(mode: Int) {
        this.softMode = mode
        (applicationContext as AppClass).currentActivity?.window?.setSoftInputMode(mode)
    }

    /**
     * Reestablece el comportamiento de la actividad ante la apertura del teclado virtual
     */
    fun restoreSoftMode()
    {
        (applicationContext as AppClass).currentActivity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        this.softMode = orignalSoftMode
    }


    /*
        class eventUpdatesJob(val eventKey: String) {

            fun createJob() {

                return GlobalScope.launch(Dispatchers.IO) {
                    eventRepository.getEventFlow(eventKey)?.collect { response ->
                        when (response) {
                            is Resource.Success -> {
                                if (response.data != null) {
                                    var event = response.data!!
                                    if (_eventsMap.value!!.containsKey(event.event_key)) {
                                        _eventsMap.value!![event.event_key] = event
                                        _eventsEventsFlow.postValue(
                                            EventRepository.EventDataEvent.OnChildChanged(event, null)
                                        )
                                    } else {
                                        _eventsMap.value!!.put(event.event_key, event)
                                        _eventsEventsFlow.postValue(
                                            EventRepository.EventDataEvent.OnChildAdded(event, null)
                                        )
                                    }

                                    var currentEventsMap: HashMap<String, Event> = eventsMap.value!!

                                    currentEventsMap.set(
                                        eventKey, response.data!!
                                    )
                                    _eventsMap.postValue(currentEventsMap)
                                } else {
                                    var eventKey = eventKey

                                    var mapUpdated: HashMap<String, Event> =
                                        _eventsMap.value!!.clone() as HashMap<String, Event>
                                    mapUpdated.remove(eventKey)
                                    _eventsMap.postValue(mapUpdated)


    //                        _eventsMap.value!!.remove(eventKey)
                                    _eventsEventsFlow.postValue(
                                        EventRepository.EventDataEvent.OnChildRemoved(eventKey)
                                    )
                                }
                            }

                            is Resource.Error -> {
                                Resource.Error<Boolean?>(response.message!!)
                            }

                            is Resource.Loading -> {
                                Resource.Loading<Boolean?>(null)
                            }

                            else -> {}
                        }

                    }


                }
            }

        }
    */
}

