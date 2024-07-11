package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.NotificationList
import com.iyr.ian.utils.coroutines.Resource

interface NotificationListRepositoryInterface {
    suspend fun listNotificationList(userKey: String): Resource<ArrayList<NotificationList>?>
}

abstract class NotificationListRepository : NotificationListRepositoryInterface {
/*
 sealed class DataEvent {
        data class ChildAdded(val data: EventNotificationModel, val previousChildName: String?) :
            DataEvent()
        {
            val notification = data as EventNotificationModel
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
*/


    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = "users_notifications_groups"
}