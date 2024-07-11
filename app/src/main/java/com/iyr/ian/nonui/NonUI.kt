package com.iyr.ian.nonui

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.AppConstants
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.models._LatLng
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.repository.implementations.databases.realtimedatabase.CoreRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SubscriptionTypeRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getBatteryPercentage
import com.iyr.ian.utils.showErrorDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NonUI private constructor() {

    private lateinit var userFlowJob: Job

    private var context: Context? = null

    val coreRepository by lazy { CoreRepositoryImpl() }
    val usersRepository by lazy { UsersRepositoryImpl() }
    val subscriptionTypesRepository by lazy { SubscriptionTypeRepositoryImpl() }


    private var userId = "???"
    private var _user: User? = null

    var mediaPlayer: MediaPlayer = MediaPlayer()
    private var bathUpdates: Boolean = false
    private var locationUpdatesJob: Job? = null
    lateinit var lastLocation: LatLng
    private var pendingChanges: HashMap<String, _LatLng> = HashMap<String, _LatLng>()

    companion object {

        @Volatile
        private var instance: NonUI? = null

        fun getInstance(context: Context): NonUI {
            val checkInstance = instance
            if (checkInstance != null) {
                return checkInstance
            }
            return synchronized(this) {
                val checkInstanceAgain = instance
                if (checkInstanceAgain != null) {
                    checkInstanceAgain
                } else {
                    val created = NonUI()

                    instance = created
                    created.context = context
                    created
                }
            }
        }

    }

    /*
        private val locationReceiver: BroadcastReceiver = object : LocationReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                super.onReceive(context, intent)
                when (intent.action) {
                    Constants.BROADCAST_LOCATION_UPDATED -> {


                        CoroutineScope(Dispatchers.IO).launch {

                            val dataAsJson = intent.getStringExtra("data")
                            val data = Gson().fromJson(dataAsJson, HashMap::class.java)

                            var locationAsJson = ""
                            try {
                                locationAsJson = data["location"].toString()
                            } catch (ex: Exception) {
                                var pp = 0
                            }

                            try {
                                val locationCust: HashMap<String, Double> =
                                    Gson().fromJson(
                                        locationAsJson,
                                        HashMap::class.java
                                    ) as HashMap<String, Double>

                                val latLng = LatLng(
                                    locationCust["latitude"]!!,
                                    locationCust["longitude"]!!
                                )

                                lastLocation = latLng

                                if (bathUpdates) {
                                    pendingChanges[System.currentTimeMillis().toString()] =
                                        _LatLng(locationCust["latitude"]!!, locationCust["longitude"]!!)
                                    Log.d(
                                        "LOCATION_SERVICES",
                                        "UPDATING USER LOCATION by BATCH$latLng"
                                    )
                                } else {
                                    Log.d(
                                        "LOCATION_SERVICES",
                                        "UPDATING USER LOCATION one by one = $latLng"
                                    )
                                    // Lo saco porque quiero hacer la actualizacion por lotes.


                                    var batteryLevel = context.getBatteryPercentage()
                                    var call =
                                        CoreRepositoryImpl().postCurrentLocation(
                                            userId,
                                            latLng,
                                            batteryLevel
                                        )

                                    if (call is Resource.Error<Boolean?>) {

                                        /*
                                        TODO: Mostrar error en pantalla cuando la aplicacion esta en primer plano
                                                                          AppClass.instance.getCurrentActivity()
                                                                              ?.let { activity ->
                                                                                  activity.hideLoader()
                                                                                  activity.showErrorDialog(call.message.toString())
                                                                              }

                                      */
                                    }
                                }
                            } catch (ex: Exception) {
                                var pp = 0
                            }

                        }

                    }

                }
            }
        }
    */

    init {
        /*
        registerReceiver()
        if (bathUpdates) {
            locationUpdatesJob = startRepeatingJob(AppConstants.SERVER_LOCATION_UPDATES_INTERVAL)
        }
        // subscribe()
        startFlows()
        */
    }

    fun initialize() {
        userId = SessionForProfile.getInstance(this.context!!).getUserId()!!
        registerReceiver()
        if (bathUpdates) {
            locationUpdatesJob = startRepeatingJob(AppConstants.SERVER_LOCATION_UPDATES_INTERVAL)
        }
        // subscribe()
        startFlows()
    }

    private fun startFlows() {
        userFlowJob = GlobalScope.launch(Dispatchers.IO) {
            usersRepository.getUserDataAsFlow(SessionForProfile.getInstance(context!!).getUserId()!!)
                ?.collect { response ->


                    if (response is Resource.Success) {
                        var _user: User = response.data!!

                        withContext(Dispatchers.Main) {
                            var _user: User = response.data!!
                            Toast.makeText(
                                context,
                                "UserFlowJob - Nombre = ${_user.subscriptionTypeKey}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                        var currentUser: User =
                            SessionForProfile.getInstance(context!!).getUserProfile()

                        if (_user.subscriptionTypeKey.isNullOrEmpty()) {

                            var call = subscriptionTypesRepository.getSubscriptionTypeByAccessLevel(
                                AccessLevelsEnum.FREE
                            )
                            when (call) {
                                is Resource.Error -> {
                                    AppClass.instance.getCurrentActivity()
                                        ?.showErrorDialog(call.message.toString())
                                }

                                is Resource.Loading -> {}
                                is Resource.Success -> {
                                    var subscriptionType = call.data!!
                                    _user.subscriptionTypeKey =
                                        subscriptionType.subscription_type_key // ponerle la basica.
                                    AppClass.instance.setUserSubscription(subscriptionType)
                                    usersRepository.updateSubscriptionType(
                                        FirebaseAuth.getInstance().uid.toString(),
                                        subscriptionType.subscription_type_key
                                    )
                                }
                            }

                        } else {

                            var subscriptionTypeCall =
                                subscriptionTypesRepository.getSubscriptionType(_user!!.subscription_type_key.toString())
                            when (subscriptionTypeCall) {
                                is Resource.Error -> {
                                    AppClass.instance.getCurrentActivity()
                                        ?.showErrorDialog(subscriptionTypeCall.message.toString())
                                }

                                is Resource.Loading -> {}
                                is Resource.Success -> {
                                    var subscriptionType = subscriptionTypeCall.data
                                    if (subscriptionType != null) {
                                        AppClass.instance.setUserSubscription(subscriptionType)
                                        SessionForProfile.getInstance(context!!).storeUserProfile(_user)
                                    }
                                    else
                                    {
                                        var call = subscriptionTypesRepository.getSubscriptionTypeByAccessLevel(
                                            AccessLevelsEnum.FREE
                                        )
                                        when (call) {
                                            is Resource.Error -> {
                                                AppClass.instance.getCurrentActivity()
                                                    ?.showErrorDialog(call.message.toString())
                                            }

                                            is Resource.Loading -> {}
                                            is Resource.Success -> {
                                                var subscriptionType = call.data!!
                                                _user.subscriptionTypeKey =
                                                    subscriptionType.subscription_type_key // ponerle la basica.
                                                AppClass.instance.setUserSubscription(subscriptionType)
                                                usersRepository.updateSubscriptionType(
                                                    FirebaseAuth.getInstance().uid.toString(),
                                                    subscriptionType.subscription_type_key
                                                )
                                            }
                                        }

                                    }
                                    AppClass.instance.setUserSubscription(subscriptionType!!)
                                }
                            }
                        }
                        // comparo la propiedad subscription_type_key de user con la de _user

                        SessionForProfile.getInstance(context!!).storeUserProfile(_user)
                        /*
                                                var intent: Intent? = null

                                                if (!_user?.status.isNullOrEmpty() && _user?.status is String && PulseValidationStatusEnum.valueOf(
                                                        _user?.status ?: ""
                                                    ) != PulseValidationStatusEnum.USER_OK
                                                ) {
                                                    intent?.action = AppConstants.BROADCAST_BLOCKED_LAYOUT_REQUIRED
                                                } else {
                                                    intent?.action = AppConstants.BROADCAST_BLOCKED_LAYOUT_DISMISS
                                                }
                                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent!!)
                        */
                    } else {
                        // TODO: Manejar este error
                    }
                }

        }
    }


    fun stopFlows() {
        if (userFlowJob != null) {
            userFlowJob.cancel()
        }
    }


    fun getUser(): User? {
        return _user
    }

    /*
        fun isFreeUser(): Boolean {
            return _user?.subscriptionTypeKey ?: 0 == AccessLevelsEnum.FREE.ordinal // 0 = Free; 1 = Solidario ; 2 = VIP
        }
    */
    /*
    private fun subscribe() {
        if (!::usersChangesListener.isInitialized) {
            usersChangesListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userUpdated = snapshot.getValue(User::class.java)!!
                    userUpdated.user_key = FirebaseAuth.getInstance().uid.toString()
                    SessionForProfile.getInstance(context).storeUserProfile(userUpdated)

                    var intent: Intent? = null

                    if (!userUpdated.status.isNullOrEmpty() && userUpdated.status is String && PulseValidationStatusEnum.valueOf(
                            userUpdated.status
                        ) != PulseValidationStatusEnum.USER_OK
                    ) {
                        intent?.action = AppConstants.BROADCAST_BLOCKED_LAYOUT_REQUIRED
                    } else {
                        intent?.action = AppConstants.BROADCAST_BLOCKED_LAYOUT_DISMISS
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent!!)

                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        }
        userRef.addValueEventListener(usersChangesListener)

    }
    */
    @SuppressLint("MissingPermission")
    private fun registerReceiver() {

        /*
           val intentFilter = IntentFilter()
           intentFilter.addAction(Constants.BROADCAST_LOCATION_UPDATED)

           LocalBroadcastManager.getInstance(context!!).registerReceiver(
               locationReceiver,
               intentFilter
           )
           //---- Services


         */
    }


    private fun startRepeatingJob(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (NonCancellable.isActive) {
                // Envio las actualizacion por lotes
                updateLocationsBatch()
                delay(timeInterval)
            }
        }
    }

    private suspend fun updateLocationsBatch() {
        if (pendingChanges.size > 0) {
            Log.d(
                "MARKER_ANIMATION",
                "mando un lote con " + pendingChanges.size.toString() + " registros"
            )
            var call = coreRepository.postUserLocationsBatch(
                SessionForProfile.getInstance(context!!).getUserId()!!,
                pendingChanges,
                context!!.getBatteryPercentage()
            )
            if (call is Resource.Error) {
                // TODO: manejar esto e informar el error haciendo un broadcast
            }

            /*
            UsersWSClient.instance.updateUserLocationBatch(
                pendingChanges,
                object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {

                    }

                    override fun onError(exception: Exception) {
                        super.onError(exception)
    /*
    TODO: Implementar esto
                        AppClass.instance.getCurrentActivity()?.let { activity ->
                            activity.hideLoader()
                            activity.showErrorDialog(exception.localizedMessage)
                        }

    */
                    }
                })
    */
            pendingChanges.clear()
        }
    }
}