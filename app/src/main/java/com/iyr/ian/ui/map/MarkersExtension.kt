package com.iyr.ian.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.UserTypesEnum
import com.iyr.ian.ui.map.enums.CameraModesEnum
import com.iyr.ian.ui.map.enums.MapObjectsType
import com.iyr.ian.ui.map.models.EventMapObject
import com.iyr.ian.utils.MathUtils
import com.iyr.ian.utils.getKey
import com.iyr.ian.utils.getMarkerFromView
import com.iyr.ian.utils.getUserMarkerOptions
import com.iyr.ian.utils.markers_utils.MarkerAnimationCallback
import com.iyr.ian.utils.px
import com.iyr.ian.utils.turnMovingOn
import com.iyr.ian.viewmodels.UserViewModel
import com.utsman.smartmarker.moveMarkerSmoothly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.coroutines.resume

/**
 * Crea y agrega un marker al mapa
 */
internal fun MapSituationFragment.addNewMarkerToMap(eventFollower: EventFollower) {

    synchronized(lock) {

        val markerKey = eventFollower.user_key
        val markerLocation = LatLng(eventFollower.l[0], eventFollower.l[1])

        var userType = MapObjectsType.COMMON_USER
        if (eventFollower.is_author) {
            userType = MapObjectsType.AUTHOR
        }

        if (markerKey.compareTo(UserViewModel.getInstance().getUser()?.user_key ?: "") == 0) {
            userType = MapObjectsType.ME
        }
        val mapObject = EventMapObject(
            markerKey,
            userType,
            UserTypesEnum.valueOf(eventFollower.user_type.toString()),
            eventFollower.profile_image_path,
            markerLocation
        )
        mapObjectsMap[markerKey] = mapObject
        Log.d(
            "MARKERS", " Creo el Marker para el objeto ${markerKey}"
        )
        createAndAddMapElement(mapObject)
    }

}


internal fun MapSituationFragment.createAndAddMapElement(currentObject: EventMapObject) {
    if (markersMap.containsKey(currentObject.key) == false) {
        markersMap.put(currentObject.key, null)
        lifecycleScope.launch {
            val markerOptions = generateNewMarkerForEvent(
                currentObject.key,
                currentObject.type,
                currentObject.userType,
                currentObject.resourceLocation,
                currentObject.latLng
            )

            val markerOption = markerOptions["markerOptions"] as MarkerOptions
            val bitmap = markerOptions["bitmap"] as Bitmap


            val marker = mMap?.addMarker(markerOption)


            val markerBundle = Bundle()
            markerBundle.putString("key", currentObject.key)
            marker?.tag = markerBundle

            markersImagesMap.put(currentObject.key, bitmap)
            markersMap.put(currentObject.key, marker!!)
   /*
            if (!markersMap.containsKey("ripple_" + currentObject.key)) {
                Log.d("MARKERS", "Voy a generar el Ripple de ${currentObject.key}")
                //   generateMarkerRipple(currentObject.key, currentObject.latLng)
            }

    */
        }
    }
}

internal suspend fun MapSituationFragment.generateNewMarkerForEvent(
    key: String,
    type: MapObjectsType,
    userType: UserTypesEnum?,
    resourceLocation: Any?,
    location: LatLng
): HashMap<String, Any> = suspendCancellableCoroutine { continuation ->
    val markerPosition = location
    var zoomFactor: Float = 1f
    zoomFactor = if (mMap!!.cameraPosition.zoom <= 10) {
        .4f
    } else if (mMap!!.cameraPosition.zoom > 10 && mMap!!.cameraPosition.zoom <= 16) {
        //var auxFact = mMap!!.cameraPosition.zoom / 16
        mMap!!.cameraPosition.zoom / 16
    } else 1f

    var newMarker: MarkerOptions? = null

    when (type) {
        MapObjectsType.EVENT_MARKER -> {
            val markerOptions = generateEventMarker(
                key, EventTypesEnum.valueOf(resourceLocation as String), location
            )
            continuation.resume(markerOptions)
        }

        MapObjectsType.AUTHOR -> {
            lifecycleScope.launch {
                val markerOptions = mMap!!.getUserMarkerOptions(
                    markerPosition, key, resourceLocation as String, true
                )
                continuation.resume(markerOptions)
            }

        }

        MapObjectsType.COMMON_USER -> {
            when (userType) {
                UserTypesEnum.COMMON_USER -> {
                    lifecycleScope.launch {
                        val markerOptions = mMap!!.getUserMarkerOptions(
                            markerPosition, key, resourceLocation as String, false
                        )
                        continuation.resume(markerOptions)
                    }


                }

                UserTypesEnum.POLICE_CAR -> {
                    var iconWidth = 18.px
                    iconWidth = (zoomFactor * iconWidth).toInt()
                    Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                    var iconAspectRatio = "v,43.70:100"
                    val dimensionsMap =
                        MathUtils.calculateNewSizeWithRatio(iconWidth, "H,43.70:100")
                    Log.d(
                        "ZOOM_VARIATION", "dimensionsMap = $dimensionsMap"
                    )
                    val markerOptions = getVehicleMarkerOptions(
                        markerPosition,
                        R.drawable.vehicle_police_car,
                        dimensionsMap["width"].toString().toInt(),
                        dimensionsMap["height"].toString().toInt()
                    )
//                        callback.onComplete(true, markerOptions)
                    continuation.resume(markerOptions)
                }

                UserTypesEnum.AMBULANCE -> {
                    var iconWidth = 18.px
                    iconWidth = (zoomFactor * iconWidth).toInt()

                    Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                    var iconAspectRatio = "v,43.70:100"
                    val dimensionsMap =
                        MathUtils.calculateNewSizeWithRatio(iconWidth, "H,48.97:100")
                    Log.d(
                        "ZOOM_VARIATION", "dimensionsMap = $dimensionsMap"
                    )
                    val markerOptions = getVehicleMarkerOptions(
                        markerPosition,
                        R.drawable.vehicle_ambulance,
                        dimensionsMap["width"].toString().toInt(),
                        dimensionsMap["height"].toString().toInt()
                    )
//                        callback.onComplete(true, markerOptions)
                    continuation.resume(markerOptions)

                }

                UserTypesEnum.FIRE_TRUCK -> {

                    var iconWidth = 18.px
                    iconWidth = (zoomFactor * iconWidth).toInt()
                    Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                    var iconAspectRatio = "v,43.70:100"
                    val dimensionsMap =
                        MathUtils.calculateNewSizeWithRatio(iconWidth, "H,33.11:100")
                    Log.d(
                        "ZOOM_VARIATION", "dimensionsMap = $dimensionsMap"
                    )
                    val markerOptions = getVehicleMarkerOptions(
                        markerPosition,
                        R.drawable.vehicle_firetruck,
                        dimensionsMap["width"].toString().toInt(),
                        dimensionsMap["height"].toString().toInt()
                    )
//                        callback.onComplete(true, markerOptions)
                    continuation.resume(markerOptions)
                }

                else -> {}
            }

        }

        else -> {}
    }


}


/**
 * Genera un marker para el tipo de evento que selecciono.
 */
internal fun MapSituationFragment.generateEventMarker(
    eventKey: String, eventType: EventTypesEnum, latLng: LatLng
): HashMap<String, Any> {

    val responseMap = HashMap<String, Any>()

    val eventMarker = MarkerOptions().position(latLng)

    val eventIcon = generateEventIcon(eventType)
    //--------------------------
    val markerImage = getMarkerFromView(
        AppClass.instance,
        R.layout.custom_marker_event_fat,
        eventIcon,
        0,
        AppClass.instance.resources.getDimension(R.dimen.marker_user_image_size).toInt()
            .toFloat(),
        0
    )

    //--------------------------

    if (markerImage != null) {
        eventMarker.icon(BitmapDescriptorFactory.fromBitmap(markerImage))
    }

    responseMap.put("markerOptions", eventMarker)
    responseMap.put("bitmap", markerImage!!)

//        return eventMarker
    return responseMap
}

// Optimizado
internal fun MapSituationFragment.generateEventIcon(
    eventType: EventTypesEnum, imageHeight: Int = 48.px
): Bitmap? {
    var eventIcon: Bitmap? = null
    when (eventType) {
        EventTypesEnum.SEND_POLICE -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.poli
            )
        }

        EventTypesEnum.SEND_AMBULANCE -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.ambulance_big
            )
        }

        EventTypesEnum.SEND_FIREMAN -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.fireman_big
            )
        }

        EventTypesEnum.ROBBER_ALERT -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.suspicius_big
            )
        }

        EventTypesEnum.SCORT_ME -> {
            eventIcon = BitmapFactory.decodeResource(
                resources, R.drawable.ic_destination_flag
            )
        }

        EventTypesEnum.KID_LOST -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.kid_lot_big
            )
        }

        EventTypesEnum.PET_LOST -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.pet_lost_big
            )
        }

        EventTypesEnum.PERSECUTION -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.persecution_big
            )
        }

        EventTypesEnum.MECANICAL_AID -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.mecanical_aid_big
            )
        }

        EventTypesEnum.PANIC_BUTTON -> {
            eventIcon = BitmapFactory.decodeResource(
                requireContext().resources, R.drawable.marker_sos
            )
        }

        else -> {
        }

    }

    eventIcon?.let { bitmap ->
        val newWidth = (imageHeight * bitmap.width) / bitmap.height
        eventIcon = Bitmap.createScaledBitmap(bitmap, newWidth, imageHeight, false)
    }

    return eventIcon
}


internal fun MapSituationFragment.getVehicleMarkerOptions(
    ll: LatLng, drawableId: Int, width: Int, height: Int
): HashMap<String, Any> {
    val responseMap = HashMap<String, Any>()

    val bitmapFactory = BitmapFactory.decodeResource(resources, drawableId)
    val bitmap: Bitmap = Bitmap.createScaledBitmap(
        bitmapFactory, width, height, false
    )
    val bitmapDescriptor2 = BitmapDescriptorFactory.fromBitmap(bitmap)
    val viewerMarker: MarkerOptions =
        MarkerOptions().position(ll).flat(true).icon(bitmapDescriptor2)

    responseMap.put("markerOptions", viewerMarker)
    responseMap.put("bitmap", bitmap)

//        return viewerMarker
    return responseMap
}


/**
 * Uso actual
 * Desplaza un marker en el mapa
 */
internal suspend fun MapSituationFragment.moveMarker(marker: Marker) {

    //        marcar el marker cuando se esta moviendo y si se esta moviendo
    //        que solo agregue a la cola
    //        y sino que se ejecute
    marker.turnMovingOn()
    val channel = pendingPositions[marker]
    channel?.let {
        var lastPosition: LatLng? = null
        for (position in it) {
            marker.getKey()?.let { key ->
                marker.moveMarkerSmoothly(position, false)
                //      markersMap["ripple_" + key]!!.moveMarkerSmoothly(position, false)
            }
            while (BigDecimal(marker.position.latitude).setScale(
                    7, RoundingMode.HALF_UP
                ) != BigDecimal(position.latitude).setScale(
                    7, RoundingMode.HALF_UP
                ) || BigDecimal(marker.position.longitude).setScale(
                    7, RoundingMode.HALF_UP
                ) != BigDecimal(position.longitude).setScale(7, RoundingMode.HALF_UP)
            ) {

                delay(100)
            }
            marker.position = position


            lifecycleScope.launch(Dispatchers.Main) {
                if (currentCameraMode.mode != CameraModesEnum.FREE_MODE) {
                    updateCameraAccordingMode(currentCameraMode)
                }
            }

        }
        it.cancel()  // Cancela el Channel
        //marker.turnMovingOff()
    }
    pendingPositions.remove(marker)  // Elimina el registro de pendingPositions
}

internal fun MapSituationFragment.calculateMarkerSize(zoomLevel: Float): Int {
    val baseSize =
        requireContext().resources.getDimension(R.dimen.marker_user_image_size) // Tamaño base del marcador en px
    val scaleFactor = zoomLevel / 10 // Factor de escala basado en el nivel de zoom
    return (baseSize * scaleFactor).toInt()
}

internal fun MapSituationFragment.updateMarkersSize() {
    val zoomLevel = mMap?.cameraPosition?.zoom ?: return
    val newSize = calculateMarkerSize(zoomLevel)

    markersMap.forEach { (key, marker) ->
        if (marker != null) {
            try {
                val bitmap: Bitmap = markersImagesMap.get(key)!!
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newSize, newSize, false)
                marker?.setIcon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
            } catch (e: Exception) {
                Log.d("ERROR", e.message.toString())
            }
        }
    }
}



private fun moveMarkerAlongRoute(
    marker: Marker, points: ArrayList<LatLng>, callback: MarkerAnimationCallback
) {

    //     MarkerAnimationLot().animateLine(points, mMap!!, marker, context, callback);

}

/*
private fun drawMarkerPath(markerKey: String) {

    return
    requireActivity().runOnUiThread(object : Runnable {
        override fun run() {
            Log.d("POLYLINES", "dibujo el camino de $markerKey")
            Log.d("POLYLINES", "------------ 1 -------------")
            var path: ArrayList<LatLng> = ArrayList<LatLng>()
            val polyLine: Polyline?

            polyLine = if (mapRoutes.containsKey(markerKey)) {
                mapRoutes[markerKey]!!
            } else {
                mMap?.addPolyline(PolylineOptions())
            }
            mapObjectsMap[markerKey]?.previousLocations?.values?.let {

                polyLine?.points?.clear()
                val movementPoints =
                    mapObjectsMap[markerKey]?.previousLocations?.values!!.toTypedArray()
                val points: ArrayList<LatLng> = ArrayList<LatLng>()
                movementPoints.forEach { movement ->
                    points.add(LatLng(movement.latitude!!, movement.longitude!!))
                }
                polyLine?.points = points
                mapRoutes.put(markerKey, polyLine!!)
            }
        }
    })


}
*/

/*
    private fun getRouteAndMoveMarker(
        key: String, tempPoints: ArrayList<LatLng>, callback: MarkerAnimationCallback
    ) {

        Thread {
            changesPending.add(key)
            requireContext().getTripBetweenCoordsAsync(key, tempPoints, object : DownloadCallback {
                override fun onDownload(data: String?) {
                    val response = Gson().fromJson<OSRMResponse>(
                        data!!, OSRMResponse::class.java
                    )
                    try {

                        Log.d("ROUTE", "inicio = " + tempPoints[0].toString())

                        val points = ArrayList<LatLng>()

                        points.add(tempPoints[0])

                        response.trips[0].legs[0].steps?.forEach { step ->
                            step.geometry.coordinates!!.forEach { geo ->
                                val point = LatLng(geo[1], geo[0])
                                points.add(point)
                                //  Log.d("ROUTE", "paso = " + point.toString())

                                // drawMarkerPath(marker.id)
                            }
                        }
                        points.add(tempPoints[1])

                        Log.d("ROUTE", "termino = " + tempPoints[1].toString())

                        Log.d(
                            "UPDATE_MARKERS ", "path generation end"
                        )

                        requireActivity().runOnUiThread {}
                    } catch (ex: Exception) {
                        Log.d("UPDATE_MARKERS", "Error buscando la ruta")
                    }
                }
            })
        }.start()
    }

* */

/*

    private suspend fun resizeMarkers(zoom: Float) {
        lifecycleScope.launch(Dispatchers.Default) {
            //   var factor = ((zoom * iconHeight) / markerZoomDefault) / markerZoomDefault

            var newHeight = mMap?.calculateMarkerSize(markerIconHeight, zoom)!!
            val factor = (zoom / markerZoomDefault)
        }
    }

 */