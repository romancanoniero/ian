package com.iyr.ian.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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


fun Context.showPermissionNotification() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationChannelId = "location_permission_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(notificationChannelId, "Location Permissions", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notification = NotificationCompat.Builder(this, notificationChannelId)
        .setContentTitle("Permisos de Ubicación Requeridos")
        .setContentText("La aplicación necesita permisos de ubicación y GPS activado. Toque para configurar.")
        .setSmallIcon(R.drawable.ic_baseline_location_on_24)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1, notification)
}

fun Context.showGPSActivationNotification() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationChannelId = "gps_activation_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(notificationChannelId, "GPS Activation", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notification = NotificationCompat.Builder(this, notificationChannelId)
        .setContentTitle("Activación de GPS Requerida")
        .setContentText("La aplicación necesita que el GPS esté activado. Toque para configurar.")
        .setSmallIcon(R.drawable.ic_satellite)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(2, notification)
}

fun main(args: Array<String>) {
    //ServicesUtils.printVarName()
}

