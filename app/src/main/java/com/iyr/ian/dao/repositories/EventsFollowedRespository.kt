package com.iyr.ian.dao.repositories


import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.flow.Flow

interface EventsFollowedInterface {
    suspend fun getEventsFollowedFlow(eventKey: String): Flow<Resource<EventsFollowedRepository.EventsFollowedDataEvent>>?
    suspend fun getEventsFollowedAll(userKey: String): Resource<List<EventFollowed>>
}


abstract class EventsFollowedRepository : EventsFollowedInterface {
    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = "users_following"

    sealed class EventsFollowedDataEvent {
        data class OnChildAdded(val data: EventFollowed, val previousChildName: String?) :
            EventsFollowedDataEvent()

        data class OnChildChanged(val data: EventFollowed, val previousChildName: String?) :
            EventsFollowedDataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class OnChildRemoved(val data: EventFollowed) : EventsFollowedDataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class OnChildMoved(val data: EventFollowed, val previousChildName: String?) :
            EventsFollowedDataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved

        data class OnError(val exception: Exception) : EventsFollowedDataEvent()
    }

}