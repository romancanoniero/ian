package com.iyr.ian.push

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.iyr.ian.R

class MessagesHelper {


}

fun Context.showNotification(remoteMessage: RemoteMessage) {
    // Implementa la lógica para mostrar la notificación cuando la aplicación está en segundo plano
    // Utiliza el código proporcionado en el paso 5 del ejemplo anterior.

    val channelId = getString(R.string.default_notification_channel_id)
    val notificationBuilder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.mipmap.ic_custom_launcher_foreground)
        .setContentTitle(remoteMessage.notification?.title)
        .setContentText(remoteMessage.notification?.body).setAutoCancel(true)

    val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(0, notificationBuilder.build())
}