package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationListRepositoryInterface {
    suspend fun listNotificationList(userKey: String): Resource<ArrayList<ContactGroup>?>
    suspend fun contactsGroupsByUserFlow(userKey: String): Flow<NotificationListRepository.DataEvent>

    /***
     * Inserta un grupo de Contactos
     */
    suspend fun insertGroup(groupName: String): Resource<ContactGroup>

    suspend fun deleteGroup(ownerKey: String, listKey: String): Resource<Boolean?>
    suspend fun contactsGroupsByUserListFlow(userKey: String): Flow<ArrayList<ContactGroup>>
    suspend fun addContactToGroup(userKey: String, contact: Contact, group: ContactGroup): Resource<Boolean?>
    suspend fun removeContactFromGroup(userKey: String, toString: String, toString1: String): Resource<Boolean?>

    suspend fun setNotificationsAsRead(userKey: String, vararg notificationKeys: String): Resource<Boolean?>
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


    sealed class DataEvent {
        data class ChildAdded(val data: ContactGroup, val previousChildName: String?) :
            DataEvent()
        {
            val notification = data
        }
        data class ChildChanged(val data: ContactGroup, val previousChildName: String?):
            DataEvent()

        data class ChildRemoved(val data: ContactGroup) :
            DataEvent()

        data class onChildMoved(val data: ContactGroup, val previousChildName: String?) :
            DataEvent()
    }

}