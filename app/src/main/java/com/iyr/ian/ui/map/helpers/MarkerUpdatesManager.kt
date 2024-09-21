package com.iyr.ian.ui.map.helpers

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.iyr.fewtouchs.utils.osrm.DownloadCallback
import com.iyr.fewtouchs.utils.osrm.OSRMResponse
import com.iyr.fewtouchs.utils.osrm.getTripBetweenCoordsAsync
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.UserTypesEnum
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.ui.map.enums.MapObjectsType
import com.iyr.ian.ui.map.models.EventMapObject
import com.iyr.ian.utils.MathUtils
import com.iyr.ian.utils.dp
import com.iyr.ian.utils.getMarkerFromView
import com.iyr.ian.utils.getUserMarkerOptions
import com.iyr.ian.utils.markers_utils.MarkerAnimationCallback
import com.iyr.ian.utils.markers_utils.MarkerAnimationLot
import com.iyr.ian.utils.px
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MarkerUpdates {
    var action = 1 // 1 = new ; 2 = update
    var route = ArrayList<LatLng>()
    var updating = false
}

class MarkerUpdatesManager(val activity: Activity, val map: GoogleMap) {

    var storageRepositoryImpl: StorageRepositoryImpl = StorageRepositoryImpl()


    var markersToUpdate: HashMap<String, MarkerUpdates> = HashMap()


    fun newMarker(eventMapObject: EventMapObject) {
        val callback = object : OnCompleteCallback {
            override fun onComplete(success: Boolean, result: Any?) {
                val markerOptions = result as MarkerOptions
                val marker = map.addMarker(markerOptions)!!
                val markerBundle = Bundle()
                markerBundle.putString("key", eventMapObject.key)
                startDropMarkerAnimation(marker)
                marker.tag = markerBundle

            }
        }

        Log.d("UPDATE_MARKERS", "Agrego como  pendiente a " + eventMapObject.key)

        //changesPending.add(currentObject.key)

        //          requireActivity().runOnUiThread {
        generateNewMarkerForEvent(
            eventMapObject.key,
            eventMapObject.type,
            eventMapObject.userType,
            eventMapObject.resourceLocation,
            eventMapObject.latLng,
            callback
        )

    }

    private fun generateNewMarkerForEvent(
        key: String,
        type: MapObjectsType,
        userType: UserTypesEnum?,
        resourceLocation: Any?,
        location: LatLng,
        callback: OnCompleteCallback
    ) {
        val markerPosition = location
        var zoomFactor: Float = 1f
        zoomFactor = if (map.cameraPosition.zoom <= 10) {
            .4f
        } else if (map.cameraPosition.zoom > 10 && map.cameraPosition.zoom <= 16) {
            //var auxFact = mMap!!.cameraPosition.zoom / 16
            map.cameraPosition.zoom / 16
        } else
            1f

        var newMarker: MarkerOptions? = null

        when (type) {
            MapObjectsType.EVENT_MARKER -> {
                val markerOptions = generateEventMarker(
                    key,
                    EventTypesEnum.valueOf(resourceLocation as String),
                    location
                )
                callback.onComplete(
                    true, markerOptions
                )
            }

            MapObjectsType.COMMON_USER -> {

                when (userType) {
                    UserTypesEnum.COMMON_USER -> {
                        val callback: OnCompleteCallback = object : OnCompleteCallback {
                            override fun onComplete(success: Boolean, result: Any?) {
                                val markerOptions = result as MarkerOptions
                                callback.onComplete(true, markerOptions)
                            }
                        }

                        map.getUserMarkerOptions(
                            markerPosition,
                            key,
                            resourceLocation as String,
                            false,
                            callback
                        )
                    }

                    UserTypesEnum.POLICE_CAR -> {
                        var iconWidth = 18.px
                        iconWidth = (zoomFactor * iconWidth).toInt()
                        Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                        var iconAspectRatio = "v,43.70:100"
                        val dimensionsMap =
                            MathUtils.calculateNewSizeWithRatio(iconWidth, "H,43.70:100")
                        Log.d(
                            "ZOOM_VARIATION",
                            "dimensionsMap = $dimensionsMap"
                        )
                        val markerOptions =
                            getVehicleMarkerOptions(
                                markerPosition,
                                R.drawable.vehicle_police_car,
                                dimensionsMap["width"].toString().toInt(),
                                dimensionsMap["height"].toString().toInt()
                            )
                        callback.onComplete(true, markerOptions)
                    }

                    UserTypesEnum.AMBULANCE -> {
                        var iconWidth = 18.px
                        iconWidth = (zoomFactor * iconWidth).toInt()

                        Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                        var iconAspectRatio = "v,43.70:100"
                        val dimensionsMap =
                            MathUtils.calculateNewSizeWithRatio(iconWidth, "H,48.97:100")
                        Log.d(
                            "ZOOM_VARIATION",
                            "dimensionsMap = $dimensionsMap"
                        )
                        val markerOptions =
                            getVehicleMarkerOptions(
                                markerPosition,
                                R.drawable.vehicle_ambulance,
                                dimensionsMap["width"].toString().toInt(),
                                dimensionsMap["height"].toString().toInt()
                            )
                        callback.onComplete(true, markerOptions)
                    }

                    UserTypesEnum.FIRE_TRUCK -> {

                        var iconWidth = 18.px
                        iconWidth = (zoomFactor * iconWidth).toInt()
                        Log.d("ZOOM_VARIATION", "icon witdh = $iconWidth")
                        var iconAspectRatio = "v,43.70:100"
                        val dimensionsMap =
                            MathUtils.calculateNewSizeWithRatio(iconWidth, "H,33.11:100")
                        Log.d(
                            "ZOOM_VARIATION",
                            "dimensionsMap = $dimensionsMap"
                        )
                        val markerOptions =
                            getVehicleMarkerOptions(
                                markerPosition,
                                R.drawable.vehicle_firetruck,
                                dimensionsMap["width"].toString().toInt(),
                                dimensionsMap["height"].toString().toInt()
                            )
                        callback.onComplete(true, markerOptions)
                    }

                    else -> {}
                }

            }

            else -> {}
        }


    }


    private fun generateEventMarker(
        eventKey: String,
        eventType: EventTypesEnum,
        latLng: LatLng
    ): MarkerOptions {
        val eventMarker = MarkerOptions()
            .position(latLng)

        var showEventPosition = false

        when (eventType) {
            EventTypesEnum.SEND_POLICE -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_police))
            }

            EventTypesEnum.SEND_AMBULANCE -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_ambulance))
            }

            EventTypesEnum.SEND_FIREMAN -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_fire))
            }

            EventTypesEnum.ROBBER_ALERT -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_robbery))
            }

            EventTypesEnum.SCORT_ME -> {
                showEventPosition = true
                val imageBitmap = BitmapFactory.decodeResource(
                    activity.resources,
                    R.drawable.ic_destination_flag
                )
                val resizedBitmap =
                    Bitmap.createScaledBitmap(imageBitmap, 48.px, 48.px, false)
                eventMarker.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
            }

            EventTypesEnum.KID_LOST -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_kid_lost))
            }

            EventTypesEnum.PET_LOST -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_pet_lost))
            }

            EventTypesEnum.PERSECUTION -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_persecution))
            }

            EventTypesEnum.MECANICAL_AID -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_mechanical_aid))
            }

            EventTypesEnum.PANIC_BUTTON -> {
                showEventPosition = true
                eventMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sos))
            }

            else -> {}
        }
        return eventMarker
    }

    private fun getVehicleMarkerOptions(
        ll: LatLng,
        drawableId: Int,
        width: Int,
        height: Int
    ): MarkerOptions {
        val bitmapFactory = BitmapFactory.decodeResource(activity.resources, drawableId)
        val bitmap: Bitmap = Bitmap.createScaledBitmap(
            bitmapFactory,
            width,
            height,
            false
        )
        val bitmapDescriptor2 = BitmapDescriptorFactory.fromBitmap(bitmap)
        val viewerMarker: MarkerOptions =
            MarkerOptions().position(ll).flat(true).icon(bitmapDescriptor2)
        return viewerMarker
    }

    private fun getUserMarkerOptions(
        ll: LatLng,
        userKey: String,
        userName: String,
        fileName: String,
        isAuthor: Boolean,
        callback: OnCompleteCallback
    ) {


        //   val bitmapDescriptor = BitmapDescriptorFactory.fromResource(drawableId)
        val viewerMarker: MarkerOptions = MarkerOptions().position(ll).flat(true)

        //----------------

        GlobalScope.launch(Dispatchers.IO)
        {

            val tempDirectory: String =
                activity.getDir("cache", Context.MODE_PRIVATE).toString() + "/"

//    var localFileName = tempDirectory + "/" + generateRandomString() + ".mp4"


            var localFileName = storageRepositoryImpl.downloadStoredItem(
                AppConstants.PROFILE_IMAGES_STORAGE_PATH,
                userKey,
                fileName,
                tempDirectory
            ).data

            var markerIcon: Bitmap? = null
            var resource: Bitmap = BitmapFactory.decodeFile(localFileName)
            if (!isAuthor) {
                markerIcon = getMarkerFromView(
                    activity,
                    R.layout.custom_marker_pin_viewer_circle_point,
                    resource,
                    0,
                    64.dp.toFloat(),
                    0
                )
            } else {
                markerIcon = getMarkerFromView(
                    activity,
                    R.layout.custom_marker_pin_viewer_circle_point_author,
                    resource,
                    0,
                    64.dp.toFloat(),
                    0
                )
            }

            viewerMarker.icon(
                BitmapDescriptorFactory.fromBitmap(
                    markerIcon!!
                )
            )
            callback.onComplete(true, viewerMarker)

        }
/*


        GlideApp.with(activity)
            .asBitmap()
            .load(storageReference)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    var markerIcon: Bitmap? = null
                    if (!isAuthor) {
                        markerIcon = getMarkerFromView(
                            activity,
                            R.layout.custom_marker_pin_viewer_circle_point,
                            resource,
                            0,
                            64.dp.toFloat(),
                            0
                        )
                    } else {
                        markerIcon = getMarkerFromView(
                            activity,
                            R.layout.custom_marker_pin_viewer_circle_point_author,
                            resource,
                            0,
                            64.dp.toFloat(),
                            0
                        )
                    }

                    viewerMarker.icon(
                        BitmapDescriptorFactory.fromBitmap(
                            markerIcon
                        )
                    )
                    callback.onComplete(true, viewerMarker)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

*/
    }


    private fun startDropMarkerAnimation(marker: Marker) {

        val target: LatLng = marker.position
        val handler = Handler(Looper.getMainLooper())
        val start: Long = SystemClock.uptimeMillis()
        val proj: Projection = map.projection
        val targetPoint: Point = proj.toScreenLocation(target)
        val duration = (200 + targetPoint.y * 0.6).toLong()
        val startPoint: Point = proj.toScreenLocation(marker.position)
        startPoint.y = 0
        val startLatLng: LatLng = proj.fromScreenLocation(startPoint)
        val interpolator: LinearOutSlowInInterpolator = LinearOutSlowInInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed: Long = SystemClock.uptimeMillis() - start
                val t: Float = interpolator.getInterpolation(elapsed.toFloat() / duration)
                val lng = t * target.longitude + (1 - t) * startLatLng.longitude
                val lat = t * target.latitude + (1 - t) * startLatLng.latitude
                marker.position = LatLng(lat, lng)
                if (t < 1.0) {
                    // Post again 16ms later == 60 frames per second
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    fun moveMarker(context: Context, marker: Marker, location: LatLng) {

        val markerInfo = marker.tag as Bundle
        val markerKey = markerInfo.getString("key").toString()

        val points = java.util.ArrayList<LatLng>()
        if (getMarkerInfo(marker).containsKey("last_point_calculated")) {
            val lastLatLng = LatLng(
                getMarkerInfo(marker).getDoubleArray("last_point_calculated")?.get(0)!!,
                getMarkerInfo(marker).getDoubleArray("last_point_calculated")?.get(1)!!
            )
            points.add(lastLatLng)
        } else {
            points.add(marker.position)
        }
        points.add(location)

        Log.d("RUTAS", "Agrego nuevo tramo $points")

        // Agrego como destino calculado el punto de destino.
        getMarkerInfo(marker).putDoubleArray(
            "last_point_calculated",
            doubleArrayOf(points.last().latitude, points.last().longitude)
        )


        if (points[0] != points[points.size - 1]) {

            Log.d("POINT ", "Solicito la Ruta para $markerKey")

            val callback = object : OnCompleteCallback {
                override fun onComplete(success: Boolean, key: Any?) {
                    super.onComplete(success, key)

                    var pp = 3
                    val markerKey = key
                    if (!markersToUpdate.get(markerKey)!!.updating) {

                        val animationCallback = object : MarkerAnimationCallback {
                            override fun onAnimationStart() {
                                // TODO("Not yet implemented")
                            }

                            override fun onAnimationEnd(lastPosition: LatLng?) {
                                //TODO("Not yet implemented")
                                markersToUpdate[markerKey]!!.updating = false

                            }

                            override fun onPositionUpdate(newPosition: LatLng) {
                                // TODO("Not yet implemented")
                            }
                        }
                        Log.d("MOVE_MARKERS", "INICIO UNA NUEVA ANIMACION")
                        moveMarkerAlongRoute(
                            marker,
                            points,
                            animationCallback
                        )
                    } else {
                        Log.d("MOVE_MARKERS", "deberia agregarlo a la ya existente")
                    }
                }
            }

            getRouteForMarker(
                marker,
                points,
                callback
            )

        } else {
            Log.d("POINT ", "El destino y el fin es el mismo para  $markerKey")

        }
    }


    private fun getRouteForMarker(
        marker: Marker,
        points: java.util.ArrayList<LatLng>,
        callback: OnCompleteCallback
    ) {
        val markerInfo = marker.tag as Bundle
        val markerKey = markerInfo.getString("key").toString()

        Log.d("RUTAS", "inicio un thread para markerKey =$markerKey")
        Thread {
            activity.getTripBetweenCoordsAsync(
                markerKey,
                points,
                object : DownloadCallback {
                    override fun onDownload(data: String?) {
                        val response = Gson().fromJson<OSRMResponse>(
                            data!!,
                            OSRMResponse::class.java
                        )
                        try {

                            Log.d("ROUTE", "inicio = " + points[0].toString())

                            var routePoints = java.util.ArrayList<LatLng>()
                            //points.add(points.get(0))
                            if (!markersToUpdate.containsKey(markerKey)) {
                                markersToUpdate[markerKey] = MarkerUpdates()
                            }
                            var point = LatLng(0.0, 0.0)
                            response.trips[0].legs[0].steps?.forEach { step ->
                                step.geometry.coordinates!!.forEach { geo ->
                                    point = LatLng(geo[1], geo[0])
                                    val markerRoute = markersToUpdate[markerKey]?.route!!
                                    if (markerRoute.size == 0 || point != markerRoute[markerRoute.size - 1]
                                    ) {
                                        markersToUpdate[markerKey]?.route?.add(point)
                                    }
                                }

                            }
                            activity.runOnUiThread {
                                callback.onComplete(true, markerKey)

                            }

                        } catch (ex: Exception) {
                            Log.d("UPDATE_MARKERS", "Error buscando la ruta " + ex.localizedMessage)
                        }
                    }
                })
        }.start()
    }

    private fun getMarkerInfo(marker: Marker): Bundle {
        val markerInfo = marker.tag as Bundle
        return markerInfo
    }


    private fun moveMarkerAlongRoute(
        marker: Marker,
        points: java.util.ArrayList<LatLng>,
        callback: MarkerAnimationCallback
    ) {


        val markerInfo = marker.tag as Bundle
        val markerKey = markerInfo.getString("key").toString()

        activity.runOnUiThread {
            MarkerAnimationLot().animateLine(
                markersToUpdate,
                markerKey,
                map,
                marker,
                activity,
                callback
            )

        }


    }
}