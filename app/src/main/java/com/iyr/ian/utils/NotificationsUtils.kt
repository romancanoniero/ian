package com.iyr.ian.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.iyr.ian.R


object NotificationsUtils {


class PushNotification()
{
    constructor(notificationType: String) : this() {
        this.notificationType = notificationType
    }

    var action : String? = null
    var notificationKey  : String? = null
    var notificationType  : String? = null
    var linkKey  : String? = null

}

    init {

    }


    fun showNotification(context: Context, title: String, messageBody: String): Notification {
        return showNotification(
            context,
            context.getString(R.string.default_notification_channel_id),
            NotificationCompat.PRIORITY_DEFAULT,
            R.mipmap.ic_custom_launcher,
            title,
            messageBody,
            null,
            null
        )

    }

    fun showNotification(
        context: Context,
        title: String,
        messageBody: String,
        pendingIntent: PendingIntent
    ): Notification {


        return showNotification(
            context,
            context.getString(R.string.default_notification_channel_id),
            NotificationCompat.PRIORITY_DEFAULT,
            R.mipmap.ic_custom_launcher,
            title,
            messageBody,
            pendingIntent,
            null

        )
    }

    fun showNotification(
        context: Context,
        title: String,
        messageBody: String,
        extras: Bundle?
    ): Notification {
        return showNotification(
            context,
            context.getString(R.string.default_notification_channel_id),
            NotificationCompat.PRIORITY_DEFAULT,
            R.mipmap.ic_custom_launcher,
            title,
            messageBody,
            null,
            extras
        )
    }




    fun showNotification(
        context: Context,
        priority: Int,
        icon: Int,
        title: String,
        messageBody: String
    ): Notification {
        return showNotification(
            context,
            context.getString(R.string.default_notification_channel_id),
            priority,
            R.mipmap.ic_custom_launcher,
            title,
            messageBody,
            null,
            null
        )

    }


    private fun showNotification(
        context: Context,
        channelId: String,
        priority: Int,
        icon: Int,
        title: String,
        messageBody: String,
        pendingIntent: PendingIntent?,
        extras: Bundle?
    ): Notification {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setPriority(priority)
            .setContentTitle(title)
            .setContentText(messageBody)
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }
        return builder.build()

    }

}

fun main(args: Array<String>) {
    //ServicesUtils.printVarName()
}

