package com.iyr.ian.services.location

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder


class ServiceLocationManager private constructor(private val context: Context) {

    private var locationService: ServiceLocation? = null

    companion object {
        @Volatile
        private var instance: ServiceLocationManager? = null

        fun getInstance(context: Context): ServiceLocationManager {
            return instance ?: synchronized(this) {
                instance ?: ServiceLocationManager(context).also { instance = it }
            }
        }
    }

    fun bindLocationService(): ServiceLocation {
        if (locationService == null) {
            // Inicia el servicio de ubicación si aún no se ha iniciado
            val intent = Intent(context, ServiceLocation::class.java)
            context.startService(intent)
            // Enlaza al servicio de ubicación
            val serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    val serviceBinder = binder as ServiceLocation.ServiceLocationBinder
                    locationService = serviceBinder.getService()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    locationService = null
                }
            }
            val serviceIntent = Intent(context, ServiceLocation::class.java)
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        return locationService!!
    }
}
