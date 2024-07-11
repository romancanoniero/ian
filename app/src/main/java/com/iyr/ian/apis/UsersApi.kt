package com.iyr.ian.apis


class UsersApi {


    private var updatingExpiredLocations: Boolean = false
    val followingMap = HashMap<String, String>()

/*
    fun blockUserServer(userKey: String, callback: OnCompleteCallback) {
        //---------------------------------
        val data: MutableMap<String, Any> = HashMap()
        data["user_who_blocks_key"] = FirebaseAuth.getInstance().uid.toString()
        data["user_to_block_key"] = userKey
        data["auth_token"] =
            SessionForProfile.getInstance(getApplicationContext()).getProfileProperty("auth_token")
        FirebaseFunctions.getInstance()
            .getHttpsCallable("blockUser")
            .call(data)
            .continueWith(Continuation<HttpsCallableResult, String?> { task -> // This continuation runs on either success or failure, but if the task
                val result = task.result!!.data as HashMap<String, Any>
                var resultCode: Int = result["status"].toString().toInt()
                when (resultCode) {
                    0 -> {
                        callback.onComplete("").toString()
                    }
                    else -> {

                    }
                } as String?

            }).addOnFailureListener(OnFailureListener { e ->

                callback.onError(e)
            })


    }

    fun unblockUser(userKey: String, callback: OnCompleteCallback) {


        var tableBlockedRef = FirebaseDatabase.getInstance()
            .getReference(TABLE_USERS_BLOCKED)
            .child(FirebaseAuth.getInstance().uid.toString())

        var userLocationBlockedRef =
            FirebaseDatabase.getInstance().getReference(TABLE_USERS_LOCATIONS)
                .child(FirebaseAuth.getInstance().uid.toString())
                .child("blocked")

        var userBlockedRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS)
            .child(FirebaseAuth.getInstance().uid.toString())
            .child("blocked")

        var flaresReceivedRef = FirebaseDatabase.getInstance().getReference(
            TABLE_USERS_FLARES_RECEIVED
        )
            .child(FirebaseAuth.getInstance().uid.toString())
            .child(userKey)
            .child("status")



        tableBlockedRef
            .child(userKey)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    userBlockedRef
                        .child(userKey)
                        .removeValue()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {

                                userLocationBlockedRef
                                    .child(userKey)
                                    .removeValue()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {

                                            flaresReceivedRef
                                                .setValue(FLARE_STATUS_FIRED)
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful) {

                                                        var me = SessionForProfile.getInstance(
                                                            getApplicationContext()
                                                        ).userProfile

                                                        me.blocked?.remove(userKey)
                                                        SessionForProfile.getInstance(
                                                            getApplicationContext()
                                                        ).storeUserProfile(me)


                                                        callback.onComplete()
                                                    } else {
                                                        it.exception?.let { it1 ->
                                                            callback.onError(
                                                                it1
                                                            )
                                                        }
                                                    }
                                                }

                                        } else {
                                            it.exception?.let { it1 -> callback.onError(it1) }
                                        }
                                    }


                            } else {
                                it.exception?.let { it1 -> callback.onError(it1) }
                            }
                        }

                } else {
                    it.exception?.let { it1 -> callback.onError(it1) }
                }


            }
    }

    fun unblockUserServer(userKey: String, callback: OnCompleteCallback) {
        //---------------------------------
        val data: MutableMap<String, Any> = HashMap()
        data["user_who_unblocks_key"] = FirebaseAuth.getInstance().uid.toString()
        data["user_to_unblock_key"] = userKey
        data["auth_token"] =
            SessionForProfile.getInstance(getApplicationContext())
                .getProfileProperty("auth_token")
        FirebaseFunctions.getInstance()
            .getHttpsCallable("unblockUser")
            .call(data)
            .continueWith(Continuation<HttpsCallableResult, String?> { task -> // This continuation runs on either success or failure, but if the task
                val result = task.result!!.data as HashMap<String, Any>
                var resultCode: Int = result["status"].toString().toInt()
                when (resultCode) {
                    0 -> {
                        callback.onComplete().toString()

                    }
                    else -> {

                    }
                } as String?

            }).addOnFailureListener(OnFailureListener { e ->
                callback.onError(e)
            })


    }

    fun updateUserLocationServer(location: LatLng, callback: OnCompleteCallback?) {
        //---------------------------------
        val geoHash = GeoHash(location.latitude, location.longitude)

        val data: MutableMap<String, Any> = HashMap()
        data["auth_token"] =
            SessionForProfile.getInstance(getApplicationContext())
                .getProfileProperty("auth_token")

        data["user_key"] = FirebaseAuth.getInstance().uid.toString()
        data["l"] = Arrays.asList(location.latitude, location.longitude)
        data["g"] = geoHash.geoHashString
        //       data["show_me_on_map"] =  SessionForProfile.getInstance(getApplicationContext()).userProfile.show_me_on_map
        //       data[AppConstants.FIELDS_EVENT_TIME] = System.currentTimeMillis()

        FirebaseFunctions.getInstance()
            .getHttpsCallable("updateUserLocation")
            .call(data)
            .continueWith(Continuation<HttpsCallableResult, String?> { task -> // This continuation runs on either success or failure, but if the task
                val result = task.result!!.data as HashMap<String, Any>
                var resultCode: Int = result["status"].toString().toInt()
                when (resultCode) {
                    0 -> {
                        callback?.onComplete().toString()
                    }
                    else -> {

                    }
                } as String?

            }).addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(getApplicationContext(), e.cause.toString(), Toast.LENGTH_LONG)
                    .show()
                callback?.onError(e)
            })
    }
*/
/*
    fun updateUserLocation(location: LatLng, callback: OnCompleteCallback?) {
        //---------------------------------


        // TODO: PASARLO AL SERVER
        var me = SessionForProfile.getInstance(AppClass.instance)!!.getUserProfile()
        var geoObject = UserLocation()
        geoObject.key = FirebaseAuth.getInstance().uid.toString()
        geoObject.display_name = me.display_name
      //  geoObject.image = me.image
        geoObject.user_image_url = me.image.url
        //Setting GeoFire Data
        val geoHash = GeoHash(location.latitude, location.longitude)
        geoObject.l = Arrays.asList(location.latitude, location.longitude)
        geoObject.g = geoHash.geoHashString
        geoObject.event_time = Date().time


        val eventsRef = FirebaseDatabase.getInstance().getReference(TABLE_EVENTS_LOCATIONS)

        val eventsFollowingRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS_EVENTS_FOLLOWED)
            .child(FirebaseAuth.getInstance().uid.toString())

        val locationRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS_LOCATIONS)
            .child(FirebaseAuth.getInstance().uid.toString())

        val userRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS)
            .child(FirebaseAuth.getInstance().uid.toString())

        locationRef.setValue(geoObject)
            .addOnCompleteListener {
                // actualizo mi ubicacion en el evento.

                if (AppClass.instance.isTrackingMe()) {
                    var map = HashMap<String, Any>()
                    map.put("last_location", geoObject.l)
                    map.put("last_update", ServerValue.TIMESTAMP)
                    userRef.updateChildren(map)
                }

                eventsFollowingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { record ->
                            var event = record.getValue(EventFollowed::class.java)!!

                            var updateMap = HashMap<String, Any>()
                            updateMap.put("l", geoObject.l)
                            updateMap.put("g", geoObject.g)
                            eventsRef.child(event.event_key!!)
                                .child("viewers")
                                .child(FirebaseAuth.getInstance().uid.toString())
                                .updateChildren(updateMap)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })


                if (it.isSuccessful) {
                    callback?.onComplete(true, null)
                } else {
                    callback?.onError(it.exception!!)
                }
            }


    }
*/
    /*
    suspend fun sendRegistrationToServer(token: String, callback: OnCompleteCallback?) {

        FirebaseDatabase.getInstance().getReference(TABLE_USERS_NOTIFICATION_TOKENS)
            .child(FirebaseAuth.getInstance().uid.toString())
            .setValue(token)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback?.onComplete(true, null)
                } else {
                    it.exception?.let { it1 -> callback?.onError(it1) }
                }
            }

    }
*/
    /*
    fun sendPulseVerificationSuccessfull(context: Context) {
        FirebaseDatabase.getInstance().getReference(TABLE_USERS)
            .child(FirebaseAuth.getInstance().uid.toString())
            .child("pulse_status")
            .setValue(PulseValidationStatus.USER_OK.name)
            .addOnCompleteListener {
                var me = SessionForProfile.getInstance(context)!!.getUserProfile()
                me.pulse_status = PulseValidationStatus.USER_OK.name
                SessionForProfile.getInstance(context)!!.storeUserProfile(me)
            }
    }
*/
/*
    fun sendPulseVerificationUnsuccessfull(context: Context, pulseStatus: String) {


        var me : User = SessionForProfile.getInstance(context)!!.getUserProfile()
        me.pulse_status = pulseStatus
        SessionForProfile.getInstance(context)!!.storeUserProfile(me)


        var usersRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS)
        var eventsRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS_EVENTS_FOLLOWED)

        usersRef.child(FirebaseAuth.getInstance().uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
//-------------------------------
                        var user = snapshot.getValue(User::class.java)!!
                        user.user_key = snapshot.key


                        usersRef.child(FirebaseAuth.getInstance().uid.toString())
                            .child("pulse_status")
                            .setValue(pulseStatus)
                            .addOnCompleteListener {
                                // Selecciono a los seguidores en los eventos abiertos de scorting y notifico a todos

                                var usersToNotify = ArrayList<String>()
                                eventsRef
                                    .child(FirebaseAuth.getInstance().uid.toString())
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            snapshot.children.forEach { eachEvent ->
                                                var event = eachEvent.getValue(Event::class.java)
                                                var eventKey = event!!.event_key
                                                //--------
                                                if (event?.event_type == EventTypes.SCORT_ME.name) {
                                                    event?.viewers?.forEach { record ->
                                                        var viewer = record.value as Viewer
                                                        if (viewer.key != FirebaseAuth.getInstance().uid.toString()) {
                                                            if (!usersToNotify.contains(viewer.key)) {
                                                                usersToNotify.add(viewer.key)
                                                            }
                                                        }
                                                    }
                                                }

                                                var map = HashMap<String, Any>()
                                                map.put("user_key", user!!.user_key)
                                                map.put(
                                                    "display_name",
                                                    user!!.display_name
                                                )
                                                map.put("image", user!!.image)

                                                var notification =
                                                    EventsWSClient.instance.createEventNotification(
                                                        EventNotificationType.PULSE_VERIFICATION_FAILED,
                                                        Date().time,
                                                        map
                                                    )
                                                usersToNotify.forEach { userKey ->

                                                    FirebaseDatabase.getInstance()
                                                        .getReference(TABLE_EVENTS_NOTIFICATIONS)
                                                        .child(userKey)
                                                        .child(eventKey!!)
                                                        .setValue(notification)
                                                        .addOnCompleteListener {
                                                            if (!it.isSuccessful) {
                                                                //    mCallback.onError(it.exception!!)
                                                            } else {


                                                            }
                                                        }

                                                }
                                            }

                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // TODO("devolver error")
                                        }
                                    })
                            }


                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
*/




    /*
        fun setFakeUserLocation(userKey: String, location: LatLng, callback: OnCompleteCallback?) {
            //---------------------------------
            var me = SessionForProfile.getInstance(getApplicationContext()).userProfile
            var geoObject = GeoLocationEnhanced()
            geoObject.key = userKey
            me.day_of_birth?.let {
                val dobInMillis = DateFunctions.StringDateToMillis(me.day_of_birth)
                val userAge = DateFunctions.timeDifferenceInStringFromMillis(
                    System.currentTimeMillis(),
                    dobInMillis
                ).years
                geoObject.age = userAge
            }
            geoObject.gender_key = me.gender_key
            geoObject.sexual_preference_key = me.sexual_preference_key
            geoObject.searching_age_ranges = me.searching_age_ranges
            geoObject.searching_genders =
                me.searching_genders // Todo: Convertirlo a Searching genders y agregar las orientaciones buscadas
            geoObject.searching_age_ranges = me.searching_age_ranges
            geoObject.searching_sexual_preference = me.searching_sexual_preference
            geoObject.show_me_on_map = me.show_me_on_map
            geoObject.blocked = me.blocked
            //Setting GeoFire Data
            val geoHash = GeoHash(location.latitude, location.longitude)
            geoObject.l = Arrays.asList(location.latitude, location.longitude)
            geoObject.g = geoHash.geoHashString
            geoObject.event_time = AppClass.instance.uTCTime
            val locationRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS_LOCATIONS)
                .child(userKey)
            locationRef.setValue(geoObject)
                .addOnCompleteListener {
                    //     removeExpiredLocations()
                    if (it.isSuccessful) {
                        callback?.onComplete()
                    } else {
                        callback?.onError(it.exception!!)
                    }
                }
        }

        private fun removeExpiredLocations() {
            if (updatingExpiredLocations == false) {
                updatingExpiredLocations = true
                FirebaseDatabase.getInstance().getReference(TABLE_USERS_LOCATIONS)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (snap in snapshot.children) {
                                var userLocation = snap.getValue(GeoLocationEnhanced::class.java)!!
                                userLocation.key = snap.key
                                try {
                                    if (System.currentTimeMillis() >= userLocation.event_time + AppConstants.USER_LOCATION_REFRESH_TIME) {
                                        FirebaseDatabase.getInstance()
                                            .getReference(TABLE_USERS_LOCATIONS)
                                            .child(userLocation.key)
                                            .removeValue()
                                    }
                                } catch (ex: java.lang.Exception) {
                                    var pp = 3
                                }
                            }
                            updatingExpiredLocations = false

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
        }

        fun updateNotificationTokenAndLoginTime(
            notificationToken: String,
            callback: OnCompleteCallback
        ) {
            val userRef = FirebaseDatabase.getInstance().getReference(Constants.TABLE_USERS)
                .child(FirebaseAuth.getInstance().uid!!)
            var params = HashMap<String, Any>()
            params.put("notification_token", notificationToken)
            params.put("login_time_in_millis", ServerValue.TIMESTAMP)
            userRef.updateChildren(params)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        userRef.child("login_time_in_millis")
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    AppClass.instance.uTCTime = (snapshot.value as Long?)!!
                                    AppClass.instance.startingTime =
                                        (snapshot.value as Long?)!!
                                    callback.onComplete()

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    callback.onError(error.toException())
                                }
                            })
                    }

                    //---------------
                }
        }

        fun setVisibilityStatus(visibilityStatus: LocationService.UserVisibilityStatus) {

            var me = SessionForProfile.getInstance(AppClass.instance).userProfile
            me.settings.visibility_status = visibilityStatus
            SessionForProfile.getInstance(AppClass.instance).storeUserProfile(me)

            val userRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS)
                .child(FirebaseAuth.getInstance().uid.toString())
                .child("settings")
                .child("visibility_status")
                .setValue(visibilityStatus)


    /*
            val ref = FirebaseDatabase.getInstance().getReference(TABLE_USERS_LOCATIONS)
            val geoFire = GeoFire(ref)

            var userKey = FirebaseAuth.getInstance().uid
            userKey = FirebaseAuth.getInstance().uid
            geoFire.removeLocation(
                userKey
            ) { key, error ->
                val userRef = FirebaseDatabase.getInstance().getReference(TABLE_USERS)
                    .child(FirebaseAuth.getInstance().uid.toString())

                userRef.child(USERFIELDS_LOCATION_ENABLED).setValue(false)
            }
    */
        }

        fun getAuthToken(callback: OnCompleteCallback) {

            FirebaseAuth.getInstance().currentUser!!.getIdToken(false)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result!!.token
                        SessionForProfile.getInstance(AppClass.instance)
                            .setProfileProperty("auth_token", idToken)
                        callback.onComplete()
                    } else {
                        callback.onError(task.exception!!)
                    }
                }


        }

        fun userInPlace(placeKey: String) {
            var me = SessionForProfile.getInstance(getApplicationContext()).userProfile
            var visitor = UserMinimumExtended()
            visitor.user_key = me.user_key
            visitor.user_name = me.display_name
            visitor.user_image_url = me.images.sortedWith(compareByDescending({ it.isFavorite }))
                .get(0).url
            visitor.day_of_birth = me.day_of_birth
            visitor.gender_key

            var placeVisitsRef = FirebaseDatabase.getInstance().getReference(TABLE_PLACES_VISITORS)
                .child(placeKey)
                .child(FirebaseAuth.getInstance().uid.toString())
                .setValue(visitor)
        }

        fun userOutPlace(placeKey: String) {

            var placeVisitsRef = FirebaseDatabase.getInstance().getReference(TABLE_PLACES_VISITORS)
                .child(placeKey)
                .child(FirebaseAuth.getInstance().uid.toString())
                .removeValue()
        }
    */
    companion object {
        private lateinit var mInstance: UsersApi
        val instance: UsersApi
            get() {
                if (!Companion::mInstance.isInitialized) {
                    mInstance = UsersApi()
                }
                return mInstance
            }
    }

    init {
        mInstance = this
    }
}