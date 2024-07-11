package com.iyr.ian.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.iyr.ian.Constants.Companion.LOCATION_BACKGROUND_REQUEST_CODE
import com.iyr.ian.Constants.Companion.LOCATION_REQUEST_CODE
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.IAcceptDenyDialog
import com.iyr.ian.services.location.ServiceLocation
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

fun Context.isGPSEnabled(): Boolean {
    val manager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}


fun Activity.requestLocationRequirements(): Boolean {
    return this.requestLocationRequirements(null)
}

fun Activity.requestLocationRequirements(callback: LocationRequirementsCallback?): Boolean {
    var currentActivity = this

    if (isGPSEnabled()) {
        if (haveBackgroundLocationPermissions()) {
            callback?.onRequirementsComplete()
        } else {
            val clickListener = object : IAcceptDenyDialog {
                override fun onAccept() {
                    this@requestLocationRequirements.requestBackgroundLocationPermission()
                }
            }
            currentActivity.showConfirmationDialog(
                getString(R.string.warning),
                getString(R.string.location_permisions_are_required),
                getString(R.string.authorize),
                getString(R.string.close),
                clickListener
            )
        }
    } else {
        var clickListener: IAcceptDenyDialog? = null
        clickListener = object : IAcceptDenyDialog {
            override fun onAccept() {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
        this.showConfirmationDialog(
            getString(R.string.gps),
            getString(R.string.location_setup_explanation),
            getString(R.string.enable),
            getString(R.string.close),
            clickListener
        )
        return false
    }
    return false
}


fun Activity.requestBackgroundLocationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ), LOCATION_BACKGROUND_REQUEST_CODE
        )
    } else {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            ), LOCATION_REQUEST_CODE
        )
    }
}


fun Context.checkLocationRequirementsFit(): Boolean {
    return if (isGPSEnabled()) {
        haveBackgroundLocationPermissions()
    } else {
        false
    }
    return false
}


fun Context.showGpsEnableDialog() {

    val builder = AlertDialog.Builder(this)
    builder.setTitle("Activar GPS")
    builder.setMessage("Se requiere el GPS para continuar. ¿Desea activarlo ahora?")
    builder.setPositiveButton("Activar") { _, _ ->
        openLocationSettings(this)
    }
    builder.setNegativeButton("Cancelar") { dialog, _ ->
        dialog.dismiss()
    }
    val dialog = builder.create()
    dialog.show()

}

fun openLocationSettings(context: Context) {

    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)

}

fun Context.startLocationService() {
    Log.d("BOOTMANAGER", "LOC_ Iniciando servicio de ubicación")
    val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ServiceLocation.ServiceLocationBinder
            val locationService = binder.getService()
            AppClass.instance.serviceLocationPointer = locationService
            // Ahora puedes interactuar con el servicio
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Maneja la desconexión del servicio aquí si es necesario
        }
    }
    val serviceIntent = Intent(this.applicationContext, ServiceLocation::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Log.d("BOOTMANAGER", "LOC_ startForegroundService(serviceIntent)")
        this.applicationContext.startForegroundService(serviceIntent)
    } else {
        Log.d("BOOTMANAGER", "LOC_ startService(serviceIntent)")
        this.applicationContext.startService(serviceIntent)
    }

    this.applicationContext.bindService(
        serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE
    )

}


@SuppressLint("MissingPermission")
suspend fun Context.getCurrentLocation(): Resource<LatLng?> {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(this)
    var toReturn: Resource<LatLng?> = Resource.Error<LatLng?>("Error undefined getting location")

    return if (checkLocationRequirementsFit()) {
        val location = fusedLocationClient.lastLocation.await()
        location?.let { location ->

            toReturn = Resource.Success<LatLng?>(LatLng(location.latitude, location.longitude)) // return address
        } ?: run {
            toReturn = Resource.Error<LatLng?>("Error getting location")
        }
        toReturn
    } else {
        toReturn
    }
}

