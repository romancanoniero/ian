package com.iyr.ian.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.widget.Toast
import com.iyr.ian.app.AppClass
import com.iyr.ian.utils.GPSNotificationUtils
import com.iyr.ian.utils.showGpsEnableDialog

class GpsStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                // El estado del GPS ha cambiado, verifica si está habilitado o deshabilitado
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                if (isGpsEnabled) {
                    // El GPS se ha habilitado
                    Toast.makeText(context, "El GPS se ha habilitado", Toast.LENGTH_SHORT).show()

                } else {
                    // El GPS se ha deshabilitado
                    Toast.makeText(context, "El GPS se ha deshabilitado", Toast.LENGTH_SHORT).show()
                    if (!AppClass.instance.isInForeground) {
                        GPSNotificationUtils.showGPSActivationNotification(context)
                    } else {
                        context.showGpsEnableDialog()
                    }
                }
            }
        }
    }
}
