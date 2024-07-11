package com.iyr.ian.services.location


import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.repository.implementations.databases.realtimedatabase.CoreRepositoryImpl
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.splash.SplashActivity
import com.iyr.ian.utils.getBatteryPercentage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ServiceLocation_anterior : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val coreRepository = CoreRepositoryImpl()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val binder = ServiceLocationBinder()
    private lateinit var locationRequest: LocationRequest


    private val defaultInterval: Long = TimeUnit.MINUTES.toMillis(1)
    private val defaultFastestInterval: Long = TimeUnit.SECONDS.toMillis(10)
    private val defaultSmallestDisplacement: Float = 50f
    private val defaultPriority = Priority.PRIORITY_HIGH_ACCURACY


    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ServiceLocation", "LOC_ Metodo onStartCommand")
        startForeground(FOREGROUND_SERVICE_ID, createForegroundNotification())
        if (!::locationRequest.isInitialized) {
            locationRequest = LocationRequest.create()
        }
        updateLocationRequest(
            defaultInterval,
            defaultFastestInterval,
            defaultSmallestDisplacement,
            defaultPriority
        )


        coroutineScope.launch {
            applyLocationUpdates()
            Log.d("ServiceLocation", "LOC_ applyLocationUpdates")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        removeLocationUpdates()
    }


    fun updateLocationRequest(interval: Long, fastestInterval: Long, priority: Int) {
        locationRequest.interval = interval
        locationRequest.fastestInterval = fastestInterval
        locationRequest.priority = priority
    }

    private fun updateLocationRequest(
        interval: Long,
        fastestInterval: Long,
        smallestDisplacement: Float,
        priority: Int
    ) {
        locationRequest.interval = interval
        locationRequest.fastestInterval = fastestInterval
        locationRequest.priority = priority
        locationRequest.smallestDisplacement = smallestDisplacement

        Log.d("ServiceLocation", "LOC_ updateLocationRequest")
    }

    /**
     * Aplica los cambios y actualiza los parametros de actualizacion del servicio
     */
    private fun applyLocationUpdates() {

        Log.d("ServiceLocation", "LOC_ Evento applyLocationUpdates ")


        if (::locationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        //    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {

            Log.d("ServiceLocation", "LOC_ activo el requestLocationUpdates ")


            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )

            Log.d("ServiceLocation", "LOC_ applyLocationUpdates")
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
                Log.d("ServiceLocation", "LOC_ LocationAvalaibility $p0")
            }

            override fun onLocationResult(locationResult: LocationResult) {

                Log.d("ServiceLocation", "LOC_ Evento onLocationResult ")

                locationResult.lastLocation?.let { location ->
                    // Aquí puedes manejar la ubicación obtenida
                    val latLng = LatLng(location.latitude, location.longitude)

                    serviceScope.launch(Dispatchers.IO) {
                        AppClass.instance.onDeviceLocationChanged(locationResult.lastLocation)
                        Log.d("LOCATION", "LOC_ Actualizando ubicacion $latLng")
                        var call = coreRepository.postCurrentLocation(
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            latLng,
                            applicationContext.getBatteryPercentage()
                        )
                        Log.d("LOCATION", "LOC_ Termine de actualizar ")
                    }
                }
            }
        }
    }


    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    private fun createForegroundNotification(): Notification {

        // Crear un intent para abrir la actividad principal de tu aplicación
        //val intent = Intent(applicationContext, SplashActivity::class.java)
        val app = application as AppClass
        val intent = if (!app.alreadyStarted) {
            Intent(applicationContext, SplashActivity::class.java)
        } else {
            Intent(applicationContext, MainActivity::class.java)
        }

        // Configurar el PendingIntent con el intent
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0, // Un identificador único para el PendingIntent (puedes usar cualquier valor aquí)
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT // FLAG_UPDATE_CURRENT actualiza el PendingIntent si ya existe
        )

        val channelId = "location_channel_id"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Obteniendo ubicación")
            .setContentText("La aplicación está rastreando tu ubicación en segundo plano.")
            .setSmallIcon(R.mipmap.ic_custom_launcher)
            .setContentIntent(pendingIntent) // Configurar el PendingIntent aquí
            .setAutoCancel(true) // Cerrar la notificación al hacer clic en ella
            .build()

        startForeground(FOREGROUND_SERVICE_ID, notification)
        return notification
    }

    companion object {
        private const val FOREGROUND_SERVICE_ID = 12345
    }

    inner class ServiceLocationBinder : Binder() {
        fun getService(): ServiceLocation_anterior {
            return this@ServiceLocation_anterior
        }
    }
}
