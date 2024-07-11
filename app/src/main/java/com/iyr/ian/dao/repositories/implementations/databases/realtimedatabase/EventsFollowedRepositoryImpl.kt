package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.repositories.EventsFollowedRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventsFollowedRepositoryImpl : EventsFollowedRepository() {

    private var eventsReference: DatabaseReference? = null
    private var eventsListener: ChildEventListener? = null

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference =
        FirebaseDatabase.getInstance().getReference(tableName)


    /**
     * Forma correcta de emitir datos desde Firebase Realtime Database
     */
    @ExperimentalCoroutinesApi
    override suspend fun getEventsFollowedFlow(userKey: String): Flow<Resource<EventsFollowedDataEvent>> {
        return callbackFlow {

            eventsReference = databaseReference
                .child(userKey)


            eventsListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val eventFollowed = snapshot.getValue(EventFollowed::class.java)!!
                    eventFollowed.event_key = snapshot.key!!

                    trySend(
                        Resource.Success(
                            EventsFollowedDataEvent.OnChildAdded(
                                eventFollowed,
                                null
                            )
                        )
                    )


                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val eventFollowed = snapshot.getValue(EventFollowed::class.java)!!
                    eventFollowed.event_key = snapshot.key!!
                    trySend(
                        Resource.Success(
                            EventsFollowedDataEvent.OnChildChanged(
                                eventFollowed,
                                null
                            )
                        )
                    )
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val eventFollowed = snapshot.getValue(EventFollowed::class.java)!!
                    eventFollowed.event_key = snapshot.key!!
                    trySend(
                        Resource.Success(
                            EventsFollowedDataEvent.OnChildRemoved(
                                eventFollowed
                            )
                        )
                    )
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    val eventFollowed = snapshot.getValue(EventFollowed::class.java)!!
                    eventFollowed.event_key = snapshot.key!!
                    trySend(
                        Resource.Success(
                            EventsFollowedDataEvent.OnChildMoved(
                                eventFollowed,
                                null
                            )
                        )
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.Error(error.toException().localizedMessage.toString(), null))
                }
            }
            eventsReference?.addChildEventListener(eventsListener!!)

            awaitClose {
                eventsReference?.removeEventListener(eventsListener!!)
            }
        }
    }

    /**
     * Devuelve un Resource con la lista total de los eventos seguidos por un usuario
     */
    override suspend fun getEventsFollowedAll(userKey: String): Resource<List<EventFollowed>> {
        val call = databaseReference.child(userKey).get().await()
        return if (call.exists()) {
            val eventsFollowed = mutableListOf<EventFollowed>()
            for (event in call.children) {
                val eventFollowed = event.getValue(EventFollowed::class.java)!!
                eventFollowed.event_key = event.key!!
                eventsFollowed.add(eventFollowed)
            }
            Resource.Success(eventsFollowed)
        } else {
            Resource.Error("No se encontraron eventos seguidos", null)
        }
    }
}