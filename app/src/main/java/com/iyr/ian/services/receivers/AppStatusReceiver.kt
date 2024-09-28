package com.iyr.ian.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_ENTER_BACKGROUND
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_ENTER_FOREGROUND
import com.iyr.ian.services.location.ServiceLocation
import com.iyr.ian.services.location.isServiceRunning

class AppStatusReceiver(val service: ServiceLocation) : BroadcastReceiver() {

   // crea un constructor sin argumentos
    constructor() : this(ServiceLocation())



    override fun onReceive(context: Context, intent: Intent?) {

        if (intent?.action == BROADCAST_ACTION_ENTER_BACKGROUND) {
            // La aplicación ha pasado a segundo plano

            // Iniciar el servicio en background
            context.startService(Intent(context, ServiceLocation::class.java))
            // Notificar que ha pasado a segundo plano
            context.sendBroadcast(Intent(ServiceLocation.LOCATION_BACKGROUND_NOTIFICATION_ACTION))

            service.startForeground(
                AppConstants.BACKGROUND_STATE_NOTIFICATION_ID,
                service.createBackgroundNotification()
            )


        } else if (intent?.action == BROADCAST_ACTION_ENTER_FOREGROUND) {
            // La aplicación ha vuelto a primer plano

            if (context.isServiceRunning(ServiceLocation::class.java) == true) {
                // Detener el servicio en background
                context.stopService(Intent(context, ServiceLocation::class.java))

                // Borrar la notificación
                NotificationManagerCompat.from(context)
                    .cancel(AppConstants.BACKGROUND_STATE_NOTIFICATION_ID)
            }

            // Iniciar el servicio en foreground

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, ServiceLocation::class.java))
            } else {
                context.startService(Intent(context, ServiceLocation::class.java))
            }
        }

    }
}