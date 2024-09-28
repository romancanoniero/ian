package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.repositories.EventRepository
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.dao.ServerResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await

class EventRepositoryImpl : EventRepository() {


    private val firebaseAuth = FirebaseAuth.getInstance()
    private var viewersReference: DatabaseReference? = null
    private var eventViewersListener: ChildEventListener? = null
    private var eventsReference: DatabaseReference? = null
    private var eventListener: ValueEventListener? = null


    override suspend fun getEvent(eventKey: String): Resource<Event?> {
        return try {
            val eventsReference =
                FirebaseDatabase.getInstance().getReference(tableName).child(eventKey)
            var call = eventsReference.get().await()

            var event = call.getValue(Event::class.java)
            if (event is Event) Resource.Success<Event?>(event)
            else Resource.Error<Event?>("event_does_not_exist")

        } catch (exception: Exception) {
            Resource.Error<Event?>(exception.message.toString())
        }
    }


    override suspend fun checkIfEventExists(eventKey: String): Resource<Boolean> {
        return try {
            val eventsReference =
                FirebaseDatabase.getInstance().getReference(tableName).child(eventKey)
            var call = eventsReference.get().await()

            var event = call.getValue(Event::class.java)
            if (event is Event) Resource.Success<Boolean>(true)
            else Resource.Error<Boolean>("event_doest_not_exists")

        } catch (exception: Exception) {
            Resource.Error<Boolean>(exception.message.toString())
        }
    }


    /*
    * Forma correcta de emitir datos desde Firebase Realtime Database
     */
    override suspend fun listenEventFlow(eventKey: String): Flow<Resource<Event>> = callbackFlow {

        trySend(Resource.Loading<Event>())

        eventsReference = FirebaseDatabase.getInstance().getReference(tableName).child(eventKey)

        eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val event = snapshot.getValue(
                        Event::class.java
                    )
                    val eventKey = snapshot.key
                    event!!.event_key = eventKey!!

                    trySend(Resource.Success<Event>(event))
                } else {
                    // cuando el evento dejo de existir
                    trySend(Resource.Success<Event>(null))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        eventsReference?.addValueEventListener(eventListener!!)

        awaitClose {
            eventsReference?.removeEventListener(eventListener!!)
        }
    }.conflate()

/*
    override suspend fun getEventFlow(eventKey: String): Flow<Resource<Event>> = callbackFlow {

        trySend(Resource.Loading<Event>())


        eventsReference = FirebaseDatabase.getInstance().getReference(tableName).child(eventKey)

        eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val event = snapshot.getValue(
                        Event::class.java
                    )
                    val eventKey = snapshot.key
                    event!!.event_key = eventKey!!

                    trySend(Resource.Success<Event>(event))
                } else {
                    // cuando el evento dejo de existir
                    trySend(Resource.Success<Event>(null))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        eventsReference?.addValueEventListener(eventListener!!)

        awaitClose {
            eventsReference?.removeEventListener(eventListener!!)
        }
    }.conflate()
  */


    /*
        override fun getEventViewersFlow(eventKey: String): Flow<Resource<EventViewersDataEvent?>> =
            callbackFlow {
                viewersReference = FirebaseDatabase.getInstance()
                    .getReference(tableName)
                    .child(eventKey)
                    .child("viewers")



                eventViewersListener = object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val eventFollower = snapshot.getValue(EventFollower::class.java)!!
                        eventFollower.user_key = snapshot.key!!

                        trySend(
                            Resource.Success<EventViewersDataEvent?>(
                                EventViewersDataEvent.OnChildAdded(
                                    eventFollower,
                                    null
                                )
                            )
                        )
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        val eventFollower = snapshot.getValue(EventFollower::class.java)!!
                        eventFollower.user_key = snapshot.key!!
                        trySend(
                            Resource.Success<EventViewersDataEvent?>(
                                EventViewersDataEvent.OnChildChanged(
                                    eventFollower,
                                    null
                                )
                            )
                        )
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val eventFollower = snapshot.getValue(EventFollower::class.java)!!
                        eventFollower.user_key = snapshot.key!!
                        trySend(
                            Resource.Success<EventViewersDataEvent?>(
                                EventViewersDataEvent.OnChildRemoved(
                                    eventFollower
                                )
                            )
                        )
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        val eventFollower = snapshot.getValue(EventFollower::class.java)!!
                        eventFollower.user_key = snapshot.key!!
                        trySend(
                            Resource.Success<EventViewersDataEvent?>(
                                EventViewersDataEvent.OnChildMoved(
                                    eventFollower,
                                    null
                                )
                            )
                        )
                    }

                    override fun onCancelled(error: DatabaseError) {
                        trySend(Resource.Error<EventViewersDataEvent?>(error.message))

                    }
                }
                viewersReference?.addChildEventListener(eventViewersListener!!)

                awaitClose {
                    viewersReference?.removeEventListener(eventViewersListener!!)
                }
            }
    */


    override suspend fun postLastTimeSeen(
        userKey: String, eventKey: String
    ): Resource<Long?> {

        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["event_key"] = eventKey
                data["user_key"] = userKey
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call = FirebaseFunctions.getInstance().getHttpsCallable("eventSetViewTime")
                        .call(data).await()

                    val response: HashMap<String, Any> = call.data as HashMap<String, Any>
                    val result: Long = response["data"].toString().replace("\"", "").toLong()
                    Resource.Success<Long?>(result)

                } catch (exception: Exception) {
                    Resource.Error<Long?>(exception.message.toString())
                }
            } else {
                Resource.Error<Long?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<Long?>(exception.message.toString())
        }
    }

    override suspend fun extendEvent(userKey: String, eventKey: String): Resource<Boolean?> {
        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["event_key"] = eventKey
                data["user_key"] = userKey
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("eventExtend").call(data)
                            .await()

//                    val response: HashMap<String, Any> = call.data as HashMap<String, Any>
//                    val result: Long = response["data"].toString().replace("\"", "").toLong()
                    Resource.Success<Boolean?>(true)

                } catch (exception: Exception) {
                    Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }


    override suspend fun postEvent(event: Event): Resource<Event?> {
        return try {

            if (event.location_at_creation == null) {
                Resource.Error<Event?>("location_at_creation_is_null")
            } else
            {
                var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
                if (tokenResult != null) {
                    val data: MutableMap<String, Any> = HashMap()
                    data["event"] = Gson().toJson(event)
                    data["auth_token"] = tokenResult.token.toString()
                    try {
                        var call =
                            FirebaseFunctions.getInstance().getHttpsCallable("publishEvent")
                                .call(data)
                                .await()

                        Resource.Success<Event?>(call.data as Event)

                    } catch (exception: Exception) {
                        Resource.Error<Event?>(exception.message.toString())
                    }
                } else {
                    Resource.Error<Event?>("error_getting_token")
                }
            }
        } catch (exception: Exception) {
            Resource.Error<Event?>(exception.message.toString())
        }
    }

    override suspend fun closeEvent(
        eventKey: String, userKey: String, securityCode: String
    ): Resource<Boolean?> {
        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["event_key"] = eventKey
                data["user_key"] = userKey
                data["security_code"] = securityCode
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("eventClose").call(data)
                            .await()

                    val response: ServerResponse = Gson().fromJson<ServerResponse>(
                        data.toString(), ServerResponse::class.java
                    )

                    return Resource.Success<Boolean?>(true)

                } catch (exception: Exception) {
                    return Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                return Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error<Boolean?>(exception.message.toString())
        }
    }

    override suspend fun subscribeToEvent(
        userKey: String, notificationKey: String
    ): Resource<Boolean?> {

        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["user_key"] = userKey
                data["notification_key"] = notificationKey
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("eventSubscribeTo")
                            .call(data)
                            .await()

                    val response: HashMap<String, Any> = call.data as HashMap<String, Any>

                    return Resource.Success<Boolean?>(true)

                } catch (exception: Exception) {
                    return Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                return Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error<Boolean?>(exception.message.toString())
        }
    }


    //---
    override suspend fun togleGoingStatus(
        userKey: String, eventKey: String, setTime: Boolean
    ): Resource<Boolean?> {

        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["event_key"] = eventKey
                data["viewer_key"] = userKey
                data["set_time"] = setTime
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("viewerUpdateGoingStatus")
                            .call(data).await()

                    val response: HashMap<String, Any> = call.data as HashMap<String, Any>

                    return Resource.Success<Boolean?>(true)

                } catch (exception: Exception) {
                    return Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                return Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error<Boolean?>(exception.message.toString())
        }
        //--------------------------------------
    }


    override suspend fun toggleCallAuthority(
        userKey: String, eventKey: String, setTime: Boolean
    ): Resource<Boolean?> {

        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["event_key"] = eventKey
                data["viewer_key"] = userKey
                data["set_time"] = setTime
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("viewerUpdateCallAuthorityStatus").call(data).await()

                    val response: HashMap<String, Any> = call.data as HashMap<String, Any>

                    return Resource.Success<Boolean?>(true)

                } catch (exception: Exception) {
                    return Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                return Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error<Boolean?>(exception.message.toString())
        }
        //--------------------------------------
    }


//----------

}