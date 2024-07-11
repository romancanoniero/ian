package com.iyr.ian.dao.repositories


import com.iyr.ian.dao.models.Event
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.flow.Flow

interface EventRepositoryInterface {

    suspend fun getEvent(eventKey: String): Resource<Event?>

    suspend fun extendEvent(userKey: String, eventKey: String): Resource<Boolean?>


    suspend fun listenEventFlow(eventKey: String): Flow<Resource<Event>>?

    suspend fun postLastTimeSeen(userKey: String, eventKey: String): Resource<Long?>

    suspend fun postEvent(event: Event): Resource<Event?>

    suspend fun closeEvent(
        eventKey: String,
        userKey: String,
        securityCode: String
    ): Resource<Boolean?>

 //   fun getEventViewersFlow(eventKey: String): Flow<Resource<EventRepository.EventViewersDataEvent?>>?
    suspend fun subscribeToEvent(userKey: String, notificationKey: String): Resource<Boolean?>
    suspend fun togleGoingStatus(eventKey: String, userKey: String, setTime: Boolean = true): Resource<Boolean?>
    suspend fun toggleCallAuthority(eventKey: String, userKey: String, setTime: Boolean = true): Resource<Boolean?>
    suspend fun checkIfEventExists(linkKey: String): Resource<Boolean>
}


abstract class EventRepository : EventRepositoryInterface {
    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = "events_locations"

    /*
    sealed class EventFollowedDataEvent {
        data class ChildAdded(val data: EventFollowed, val previousChildName: String?) :
            EventFollowedDataEvent()

        data class ChildChanged(val data: EventFollowed, val previousChildName: String?) :
            EventFollowedDataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class ChildRemoved(val data: EventFollowed) : EventFollowedDataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class OnChildMoved(val data: EventFollowed, val previousChildName: String?) :
            EventFollowedDataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved

        data class OnError(val exception: Exception) : EventFollowedDataEvent()
    }
*/

    sealed class EventDataEvent {
        data class OnChildAdded(val data: Event, val previousChildName: String? = null) :
            EventDataEvent()

        data class OnChildChanged(val data: Event, val previousChildName: String? = null) :
            EventDataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class OnChildRemoved(val data: String) : EventDataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class OnChildMoved(val data: Event, val previousChildName: String? = null) :
            EventDataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved

        data class OnError(val exception: Exception) : EventDataEvent()
    }




}