package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationsRepositoryInterface {

    /*
    * Envia el Token de notificacion al Servidor.
    * */
    suspend fun registerNotificationsToken(token: String): Resource<Boolean?>

    suspend fun getDataFlow(userKey: String): Flow<NotificationsRepository.DataEvent>
    suspend fun onMessageRead(userKey: String, eventKey: String, messageKey: String): Resource<Boolean?>
    suspend fun onAllMessagesRead(userKey: String, eventKey: String): Resource<Boolean?>
    fun updateNotificationStatusByUserKey(userKey: String, status: String): Resource<Boolean?>
}

abstract class NotificationsRepository : NotificationsRepositoryInterface {

 sealed class DataEvent {
        data class ChildAdded(val data: EventNotificationModel, val previousChildName: String?) :
            DataEvent()
        {
            val notification = data
        }
        data class ChildChanged(val data: EventNotificationModel, val previousChildName: String?):
            DataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class ChildRemoved(val data: EventNotificationModel) :
            DataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class onChildMoved(val data: EventNotificationModel, val previousChildName: String?) :
            DataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved
    }



    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = "events_notifications"
}