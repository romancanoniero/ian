package com.iyr.ian.dao.repositories


import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.flow.Flow

interface EventsRepositoryInterface {
    fun getEvents(userKey: String): Flow<EventsRepository.DataEvent>?
    fun getFollowedEventsComplete(userKey: String): Flow<EventsRepository.DataEvent>?
    suspend fun postEvent(event: Event): Resource<Event?>
    suspend fun getEventsListFlow(userKey: String): Flow<Resource<EventsRepository.DataEvent?>>
    //   fun getEventFlow(eventKey: String): Flow<Resource<Event>>?

    suspend fun getEventsList(userKey: String): Resource<ArrayList<EventFollowed>?>
    suspend fun subscribeToEvent(notificationKey: String, eventKey: String): Resource<Boolean?>
}


abstract class EventsRepository : EventsRepositoryInterface {


    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = "users_following"

    sealed class DataEvent {
        data class OnChildAdded(val data: EventFollowed, val previousChildName: String?) : DataEvent()
        data class OnChildChanged(val data: EventFollowed, val previousChildName: String?) :
            DataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class OnChildRemoved(val data: EventFollowed) : DataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class OnChildMoved(val data: EventFollowed, val previousChildName: String?) :
            DataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved

        data class OnError(val exception: Exception) : DataEvent()
    }

}