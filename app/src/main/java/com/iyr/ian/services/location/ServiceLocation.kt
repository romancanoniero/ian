package com.iyr.ian.services.location


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.AppConstants.Companion.BACKGROUND_STATE_NOTIFICATION_ID
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.itag.ITagsService
import com.iyr.ian.repository.implementations.databases.realtimedatabase.CoreRepositoryImpl
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.utils.UIUtils.isApplicationVisible
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.getBatteryPercentage
import com.iyr.ian.utils.getCurrentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ServiceLocation : Service() {

    enum class Mode {
        PASSIVE,
        ACTIVE
    }

    inner class ServiceLocationBinder : Binder() {
        fun getService(): ServiceLocation {
            return this@ServiceLocation
        }
    }

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private val coreRepository = CoreRepositoryImpl()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val binder = ServiceLocationBinder()
    private var lastLocation: Location? = null
    private var mode = Mode.PASSIVE


    companion object {

        const val LOCATION_EXTRA = "LOCATION"
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
        const val LOCATION_BACKGROUND_NOTIFICATION_ACTION =
            "com.example.locationservice.LOCATION_BACKGROUND_NOTIFICATION_ACTION"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BOOTEO", "Creo el Service de Localizaion.")

        AppClass.instance.serviceLocationPointer = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestPermissions()


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val extras = intent?.extras
        // val autonomousStart = extras?.getString("autonomous_start")

        Log.d("BOOTEO", "Arranca el Servicio de localizacion")


        var notification = createBackgroundNotification(baseContext)

        startForeground(
            BACKGROUND_STATE_NOTIFICATION_ID, notification
        )

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        notificationManager.notify(BACKGROUND_STATE_NOTIFICATION_ID, notification)


        startLocationUpdates()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (areLocationPermissionsGranted(true)) {

            Log.d("BOOTEO", "Inicio el servicio de localizacion en modo ${mode.name}")

            GlobalScope.launch(Dispatchers.IO) {

                fusedLocationClient.requestLocationUpdates(
                    getUpdateParams(mode),
                    locationCallback,
                    Looper.getMainLooper()
                )

            }


        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            requestPermissions()
        }
    }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            lastLocation = p0.lastLocation

            AppClass.instance.onDeviceLocationChanged(lastLocation)
            Log.d(
                "SERVICELOCATION",
                "LOC_ Actualizando ubicacion ${lastLocation?.latitude}, ${lastLocation?.longitude}"
            )

            Log.d(
                "BOOTEO",
                "LOC_ Actualizando ubicacion ${lastLocation?.latitude}, ${lastLocation?.longitude}"
            )



            GlobalScope.launch(Dispatchers.IO) {
                var call = coreRepository.postCurrentLocation(
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    LatLng(lastLocation?.latitude ?: 0.0, lastLocation?.longitude ?: 0.0),
                    applicationContext.getBatteryPercentage()
                )
                Log.d("SERVICELOCATION", "LOC_ Termine de actualizar ")

            }

        }


    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun requestPermissions() {
        val permissionRequest = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        for (permission in permissionRequest) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (isApplicationVisible()) {
                    this.applicationContext.getCurrentActivity()?.let { activity ->

                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                permission
                            )
                        ) {
                            // Mostrar explicación
                        } else {
                            // Solicitar permiso

                            if (isApplicationVisible()) {
                                this.applicationContext.getCurrentActivity()?.let { activity ->
                                    ActivityCompat.requestPermissions(
                                        activity,
                                        permissionRequest,
                                        LOCATION_PERMISSION_REQUEST_CODE
                                    )
                                }
                            } else {
                                // Notificar al usuario
                                Toast.makeText(
                                    this,
                                    "Se necesitan permisos de ubicación para que el servicio funcione correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // TODO : Notificar al servidor que el usuario tiene apagado el GPS para que le mande una notificacion push
                            }
                        }
                    }

                }

            }
        }
    }

    private fun getUpdateParams(mode: Mode): LocationRequest {
        when (mode) {
            Mode.PASSIVE -> {
                // Actualizamos los parámetros de actualización
                return LocationRequest.create().apply {
                    priority = Priority.PRIORITY_LOW_POWER
                    interval = 60000
                    fastestInterval = 10000
                    smallestDisplacement = 100f
                }
            }

            Mode.ACTIVE -> {
                // Actualizamos los parámetros de actualización
                return LocationRequest.create().apply {
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                    interval = 4000
                    fastestInterval = 1000
                    smallestDisplacement = 100f
                }

            }
        }

    }


    fun setMode(mode: Mode) {
        if (mode != this.mode) {
            this.mode = mode
            Log.d("BOOTEO", "Cambio el servicio de localizacion al modo ${mode.name}")

            stopLocationUpdates()
            startLocationUpdates()
        }
    }


}

private var createdBackgroundChannel = false
private fun createBackgroundNotificationChannel(context: Context) {
    if (!createdBackgroundChannel && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name: CharSequence = context.getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_MIN
        val channel = NotificationChannel(ITagsService.FOREGROUND_CHANNEL_ID, name, importance)
        channel.setSound(null, null)
        channel.setShowBadge(false)
        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel)
            createdBackgroundChannel = true
        }
    }
}


fun createBackgroundNotification(context: Context): Notification {
    createBackgroundNotificationChannel(context)
    val builder = NotificationCompat.Builder(context, ITagsService.FOREGROUND_CHANNEL_ID)
    builder.setTicker(null).setSmallIcon(R.mipmap.ic_custom_launcher)
        .setContentTitle(context.getString(R.string.service_in_background))
        .setContentText(context.getString(R.string.service_description))
    val intent = Intent(context, MainActivity::class.java)
    val stackBuilder = TaskStackBuilder.create(context)
    stackBuilder.addParentStack(MainActivity::class.java)
    stackBuilder.addNextIntent(intent)
    val pendingIntent = stackBuilder.getPendingIntent(
        0, PendingIntent.FLAG_UPDATE_CURRENT
    )
    builder.setContentIntent(pendingIntent)
    return builder.build()
}
