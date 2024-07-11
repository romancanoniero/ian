package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventFollowersRepositoryImpl : EventFollowersRepository() {

    private var followersReference: DatabaseReference? = null
    private var eventFollowersListener: ChildEventListener? = null

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference =
        FirebaseDatabase.getInstance().getReference(tableName)

    override fun getEventFlow(eventKey: String): Flow<Resource<EventFollower>>? {
        TODO("Not yet implemented")
    }

    /**
     * Forma correcta de emitir datos desde Firebase Realtime Database
     */
    @ExperimentalCoroutinesApi
    override suspend fun getEventFollowersFlow(eventKey: String): Flow<EventFollowerDataEvent?> =
        callbackFlow {

            followersReference = databaseReference
                .child(eventKey)


            eventFollowersListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val eventFollower = snapshot.getValue(EventFollower::class.java)!!
                    eventFollower.user_key = snapshot.key!!

                    trySend(
//                        Resource.Success<EventFollowerDataEvent?>(
                            EventFollowerDataEvent.OnChildAdded(
                                eventFollower,
                                null
                            )
  //                      )
                    )
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val eventFollower = snapshot.getValue(EventFollower::class.java)!!
                    eventFollower.user_key = snapshot.key!!
                    trySend(
//                        Resource.Success<EventFollowerDataEvent?>(
                            EventFollowerDataEvent.OnChildChanged(
                                eventFollower,
                                null
                            )
  //                      )
                    )
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val eventFollower = snapshot.getValue(EventFollower::class.java)!!
                    eventFollower.user_key = snapshot.key!!
                    trySend(
    //                    Resource.Success<EventFollowerDataEvent?>(
                            EventFollowerDataEvent.OnChildRemoved(
                                eventFollower
                            )
      //                  )
                    )
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    val eventFollower = snapshot.getValue(EventFollower::class.java)!!
                    eventFollower.user_key = snapshot.key!!
                    trySend(
//                        Resource.Success<EventFollowerDataEvent?>(
                            EventFollowerDataEvent.OnChildMoved(
                                eventFollower,
                                null
                            )
  //                      )
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(EventFollowerDataEvent.OnError(error.toException()))
                }
            }
            followersReference?.addChildEventListener(eventFollowersListener!!)

            awaitClose {
                followersReference?.removeEventListener(eventFollowersListener!!)
            }
        }


    override suspend fun getEventFollowers(eventKey: String): Resource<ArrayList<EventFollower>?> {

        val eventFollowers = ArrayList<EventFollower>()

        followersReference = databaseReference
            .child(eventKey)

        var call = followersReference?.get()?.await()
        var results = call
        results?.let { results ->
            for (eventFollowerSnapshot in results.children) {
                val eventFollower = eventFollowerSnapshot.getValue(EventFollower::class.java)!!
                eventFollower.user_key = eventFollowerSnapshot.key!!
                eventFollowers.add(eventFollower)
            }
            return Resource.Success(eventFollowers)
        }

        return Resource.Error<ArrayList<EventFollower>?>("Error")

    }
}