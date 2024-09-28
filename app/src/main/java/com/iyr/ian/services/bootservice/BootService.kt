package com.iyr.ian.services.bootservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITagsService
import com.iyr.ian.services.location.ServiceLocation
import com.iyr.ian.services.receivers.AppStatusReceiver
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.isGPSEnabled
import com.iyr.ian.utils.showGPSActivationNotification
import com.iyr.ian.utils.showPermissionNotification

class BootService : Service() {

    private var serviceConnection: ServiceConnection? = null

    companion object {
        private const val TAG =
            "BOOT_SERVICE" // Identificador único para la tarea en segundo plano

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()
        startForeground(1, createNotification())
         serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as ServiceLocation.ServiceLocationBinder
                val service = binder.getService()
                val receiver = AppStatusReceiver(service)
                val filter = IntentFilter().apply {
                    addAction(AppConstants.BROADCAST_ACTION_ENTER_BACKGROUND)
                    addAction(AppConstants.BROADCAST_ACTION_ENTER_FOREGROUND)
                }
                LocalBroadcastManager.getInstance(this@BootService)
                    .registerReceiver(receiver, filter)
                LocalBroadcastManager.getInstance(this@BootService)
                    .sendBroadcast(Intent(AppConstants.BROADCAST_ACTION_ENTER_BACKGROUND))
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // Handle service disconnected
            }
        }
        Log.d("BOOTEO", "Voy a llamar al servicio de ubicacion.")
        if (baseContext.isGPSEnabled()) {
            if (baseContext.areLocationPermissionsGranted()) {
                Log.d("BOOTEO", "Quiero iniciar el  servicio de ubicacion.")
                val serviceIntent = Intent(this, ServiceLocation::class.java).apply {
                    putExtra("autonomous_start", true)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                bindService(serviceIntent, serviceConnection!!, BIND_AUTO_CREATE)

                // --------- ITAG SERVICE
                if (SessionApp.getInstance(applicationContext).isBTPanicButtonEnabled) {
                    Log.d(TAG, "EMG_ Inicio servicio de ITag")
                    startTagsService()

                }


            } else {
                Log.d("BOOTEO", "Los permisos de ubicacion no estaban activados.")
                baseContext.showPermissionNotification()
            }
        } else {
            Log.d("BOOTEO", "El GPS no estaba activado.")
            baseContext.showGPSActivationNotification()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceConnection?.let {
            unbindService(it)
        }
        Log.d("BootService", "Servicio de booteo destruido.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "BOOT_SERVICE_CHANNEL",
                "Boot Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "BOOT_SERVICE_CHANNEL")
            .setContentTitle("Boot Service")
            .setContentText("El servicio de booteo está en ejecución.")
            .setSmallIcon(R.drawable.logo_cuadrado_fondo_blanco)
            .build()
    }

    private fun startTagsService() {
        val serviceIntent = Intent(this, ITagsService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        ITag.initITag(applicationContext)
        AppClass.instance.initializeITags()
    }
}