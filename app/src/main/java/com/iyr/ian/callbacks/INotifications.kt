package com.iyr.ian.callbacks

import android.view.View
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.EventNotificationModel

interface INotifications {
    fun showNotificationsDialog(){}
    fun notificationDeleteByKey(notification: EventNotificationModel, viewPressed: View) {}
    fun notificationsDeleteByEvent(
        eventKey: EventNotificationModel,
        viewPressed: View
    ) {}// Borra todas las notificaciones para un evento.

    fun contactRequestAccept(contact: Contact){}
    fun onAgreeToAssist(notificationKey: String, eventKey: String){}
    fun goToEvent(eventKey: String){}


    fun onGoToChatPressed(channelKey : String, messageKey : String){}
    fun onError(exception: Exception)
}