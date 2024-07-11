package com.iyr.ian.ui.interfaces

import android.view.View
import com.iyr.ian.dao.models.EventNotificationModel


interface INotificationPopup {
    fun showNotificationsDialog()
    fun notificationDeleteByKey(notification: EventNotificationModel, viewPressed: View)
    fun notificationsDeleteByEvent(
        eventKey: EventNotificationModel,
        viewPressed: View
    ) // Borra todas las notificaciones para un evento.
    fun onGoToChatPressed(channelKey : String, messageKey : String)
}