package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.repositories.EventsRepository
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.dao.ServerResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventsRepositoryImpl : EventsRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)

    override fun getFollowedEventsComplete(userKey: String): Flow<DataEvent> = callbackFlow {

        val eventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.getValue(EventFollowed::class.java)
                data?.let {
                    data.event_key = snapshot.key.toString()
                    trySend(DataEvent.OnChildAdded(data, previousChildName))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.getValue(EventFollowed::class.java)
                data?.let {
                    data.event_key = snapshot.key.toString()
                    trySend(DataEvent.OnChildAdded(data, previousChildName))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val data = snapshot.getValue(EventFollowed::class.java)
                data?.let {
                    data.event_key = snapshot.key.toString()
                    trySend(DataEvent.OnChildRemoved(data))
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(DataEvent.OnError(error.toException()))
            }
        }

        databaseReference.child(userKey).addChildEventListener(eventListener)

        awaitClose {
            databaseReference.removeEventListener(eventListener)
        }
    }


    override fun getEvents(userKey: String): Flow<DataEvent> = callbackFlow {

        val eventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.getValue(EventFollowed::class.java)
                data?.let {
                    data.event_key = snapshot.key.toString()
                    trySend(DataEvent.OnChildAdded(data, previousChildName))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.getValue(EventFollowed::class.java)
                data?.let {
                    data.event_key = snapshot.key.toString()
                    trySend(DataEvent.OnChildChanged(data, previousChildName))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val data = snapshot.getValue(EventFollowed::class.java)
                data?.let {
                    data.event_key = snapshot.key.toString()
                    trySend(DataEvent.OnChildRemoved(data))
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(DataEvent.OnError(error.toException()))
            }
        }

        databaseReference.child(userKey).addChildEventListener(eventListener)

        awaitClose {
            databaseReference.removeEventListener(eventListener)
        }
    }


    override suspend fun getEventsList(userKey: String): Resource<ArrayList<EventFollowed>?> {

        /*

                val eventsListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var eventsList = ArrayList<EventFollowed>()
                        if (snapshot.exists()) {
                            snapshot.children.forEach { event ->
                                val eventFollowed = event.getValue(EventFollowed::class.java)
                                eventFollowed!!.event_key = event.key.toString()
                                eventsList.add(eventFollowed)
                            }
                        }
                        Resource.Success<ArrayList<EventFollowed>>(eventsList)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Resource.Error<ArrayList<EventFollowed?>?>(error.message)
                    }
                }

                databaseReference.child(userKey).addListenerForSingleValueEvent(eventsListener)

        */
        var eventsList = ArrayList<EventFollowed>()
        try {
            var result = databaseReference.child(userKey).get().await()



            if (result.exists()) {
                result.children.forEach { event ->
                    val eventFollowed = event.getValue(EventFollowed::class.java)
                    eventFollowed!!.event_key = event.key.toString()
                    eventsList.add(eventFollowed)
                }
            }

        } catch (exception: Exception) {
            return Resource.Error<ArrayList<EventFollowed>?>(exception.localizedMessage)

        }
        return Resource.Success<ArrayList<EventFollowed>?>(eventsList)

        /*
            override suspend fun getEventsListFlow(userKey: String): Flow<Resource<ArrayList<EventFollowed>?>> =
                callbackFlow {

                    val eventsListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var eventsList = ArrayList<EventFollowed>()
                            if (snapshot.exists()) {
                                snapshot.children.forEach { event ->
                                    val eventFollowed = event.getValue(EventFollowed::class.java)
                                    eventFollowed!!.event_key = event.key.toString()
                                    eventsList.add(eventFollowed)
                                }
                            }
        //                    Resource.Success<ArrayList<EventFollowed?>?>(eventsList)
                            trySend(Resource.Success<ArrayList<EventFollowed>?>(eventsList))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Resource.Error<ArrayList<EventFollowed?>?>(error.message)
                            trySend(Resource.Error<ArrayList<EventFollowed>?>(error.message))
                        }
                    }

                    databaseReference.child(userKey).addValueEventListener(eventsListener)

                    awaitClose {
                        databaseReference.removeEventListener(eventsListener)
                    }
                    trySend(Resource.Loading<ArrayList<EventFollowed>?>())
                }
        */
    }

    override suspend fun getEventsListFlow(userKey: String): Flow<Resource<EventsRepository.DataEvent?>> =
        callbackFlow {

            val eventsListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val eventFollowed: EventFollowed =
                        snapshot.getValue(EventFollowed::class.java)!!
                    trySend(
                        Resource.Success<EventsRepository.DataEvent?>(
                            EventsRepository.DataEvent.OnChildAdded(
                                eventFollowed,
                                null
                            )
                        )
                    )
                }

                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    val eventFollowed: EventFollowed =
                        snapshot.getValue(EventFollowed::class.java)!!
                    trySend(
                        Resource.Success<EventsRepository.DataEvent?>(
                            EventsRepository.DataEvent.OnChildChanged(
                                eventFollowed,
                                null
                            )
                        )
                    )
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val eventFollowed: EventFollowed =
                        snapshot.getValue(EventFollowed::class.java)!!
                    trySend(
                        Resource.Success<EventsRepository.DataEvent?>(
                            EventsRepository.DataEvent.OnChildRemoved(
                                eventFollowed
                            )
                        )
                    )

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.Error<EventsRepository.DataEvent?>(error.message))

                }
                /*
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var eventsList = ArrayList<EventFollowed>()
                                if (snapshot.exists()) {
                                    snapshot.children.forEach { event ->
                                        val eventFollowed = event.getValue(EventFollowed::class.java)
                                        eventFollowed!!.event_key = event.key.toString()
                                        eventsList.add(eventFollowed)
                                    }
                                }
                //                    Resource.Success<ArrayList<EventFollowed?>?>(eventsList)
                                trySend(Resource.Success<ArrayList<EventFollowed>?>(eventsList))
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Resource.Error<ArrayList<EventFollowed?>?>(error.message)
                                trySend(Resource.Error<ArrayList<EventFollowed>?>(error.message))
                            }
                            */
            }

            databaseReference.child(userKey).addChildEventListener(eventsListener)

            awaitClose {
                databaseReference.removeEventListener(eventsListener)
            }
            trySend(Resource.Loading<EventsRepository.DataEvent?>())

            /*
            val eventsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var eventsList = ArrayList<EventFollowed>()
                    if (snapshot.exists()) {
                        snapshot.children.forEach { event ->
                            val eventFollowed = event.getValue(EventFollowed::class.java)
                            eventFollowed!!.event_key = event.key.toString()
                            eventsList.add(eventFollowed)
                        }
                    }
    //                    Resource.Success<ArrayList<EventFollowed?>?>(eventsList)
                    trySend(Resource.Success<ArrayList<EventFollowed>?>(eventsList))
                }

                override fun onCancelled(error: DatabaseError) {
                    Resource.Error<ArrayList<EventFollowed?>?>(error.message)
                    trySend(Resource.Error<ArrayList<EventFollowed>?>(error.message))
                }
            }

            databaseReference.child(userKey).addValueEventListener(eventsListener)

            awaitClose {
                databaseReference.removeEventListener(eventsListener)
            }
            trySend(Resource.Loading<ArrayList<EventFollowed>?>())

            */
        }


    override suspend fun postEvent(event: Event): Resource<Event?> {

        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["event"] = Gson().toJson(event)
                data["auth_token"] = tokenResult.token.toString()
                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("eventPost").call(data)
                            .await()/*
                                    val response: ServerResponse = Gson().fromJson<ServerResponse>(
                                        call.data.toString(),
                                        ServerResponse::class.java
                                    )
                */
                    var dataReturned =
                        (call.data as HashMap<String, Any>).get("data").toString()

                    var eventReturned = Gson().fromJson<Event>(dataReturned, Event::class.java)
                    if (eventReturned != null) {
                        return Resource.Success<Event?>(eventReturned)
                    } else {
                        return Resource.Error<Event?>(
                            (call.data as HashMap<String, Any>).get("message").toString()
                        )
                    }
                    //    return Resource.Success<Event?>(eventReturned)

                } catch (exception: Exception) {
                    return Resource.Error<Event?>(exception.message.toString())
                }
            } else {
                return Resource.Error<Event?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error<Event?>(exception.message.toString())
        }
    }


    override suspend fun subscribeToEvent(
        notificationKey: String,
        eventKey: String
    ): Resource<Boolean?> {
        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()

            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["user_key"] = FirebaseAuth.getInstance().uid.toString()
                data["notification_key"] = notificationKey
                data["auth_token"] = tokenResult.token.toString()

                try {
                    val call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("eventSubscribeTo")
                        .call(data)
                        .await()

                    val result: ServerResponse = Gson().fromJson<ServerResponse>(
                        call.data.toString(),
                        ServerResponse::class.java
                    )

                    Resource.Success<Boolean?>(result.status == 200)

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


}