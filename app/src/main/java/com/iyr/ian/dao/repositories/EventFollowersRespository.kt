package com.iyr.ian.dao.repositories


import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface EventFollowersInterface {
    fun getEventFlow(eventKey: String): Flow<Resource<EventFollower>>?
    suspend fun getEventFollowersFlow(eventKey: String): Flow<EventFollowersRepository.EventFollowerDataEvent?>
    suspend fun getEventFollowers(eventKey: String): Resource<ArrayList<EventFollower>?>
    @ExperimentalCoroutinesApi
    suspend fun followFollowerFlow(
        eventKey: String,
        userKey: String
    ): Flow<EventFollowersRepository.EventFollowerDataEvent?>

    suspend fun setOnLine(eventKey: String, userKey: String)
    suspend fun setOffLine(eventKey: String, userKey: String)

}


abstract class EventFollowersRepository : EventFollowersInterface {
    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = "events_followers"

    sealed class EventFollowerDataEvent {
        data class OnChildAdded(val data: EventFollower, val previousChildName: String?) :
            EventFollowerDataEvent()

        data class OnChildChanged(val data: EventFollower, val previousChildName: String?) :
            EventFollowerDataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class OnChildRemoved(val data: EventFollower) : EventFollowerDataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class OnChildMoved(val data: EventFollower, val previousChildName: String?) :
            EventFollowerDataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved

        data class OnError(val exception: Exception) : EventFollowerDataEvent()
    }

}