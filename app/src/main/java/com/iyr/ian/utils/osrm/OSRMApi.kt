package com.iyr.fewtouchs.utils.osrm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import com.iyr.ian.AppConstants.Companion.USER_AGENT
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.utils.checkLocationRequirementsFit
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.geo.GeoFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.util.concurrent.TimeUnit


class OSRMApi {

    companion object {

        private var mInstance: OSRMApi? = null

        @JvmStatic
        val instance: OSRMApi
            get() {
                if (mInstance == null) {
                    mInstance = OSRMApi()
                    //Initialize Loader
                }
                return mInstance!!
            }

    }


    private val useragent = "InstantAlertNetwork / 1.0.0"


    fun getRoutBetweenCoords(points: Array<LatLng>): OSRMResponse? {
        val baseUrlBuffer: StringBuilder = java.lang.StringBuilder()
        baseUrlBuffer.append("https://router.project-osrm.org/route/v1/")
        baseUrlBuffer.append("foot")
        baseUrlBuffer.append("/")
        var pointsCount = 0
        points.forEach { latLng ->
            if (pointsCount > 0) {
                baseUrlBuffer.append(";")
            }
            baseUrlBuffer.append(latLng.longitude.toString())
            baseUrlBuffer.append(",")
            baseUrlBuffer.append(latLng.latitude.toString())
            pointsCount++
        }
        baseUrlBuffer.append("?steps=true")
        val baseUrl = baseUrlBuffer.toString()

        //"http://router.project-osrm.org/route/v1/driving/13.388860,52.517037;13.397634,52.529407;13.428555,52.523219?overview=false"

        val client: OkHttpClient = OkHttpClient()
        val builder = baseUrl.toHttpUrlOrNull()?.newBuilder()
        val request = Request.Builder()
            .url(builder.toString())
            .addHeader("User-Agent", useragent)
            .addHeader("Referer", "https://github.com/sun-jiao/LocalizedGeocoder/")
            .build()

        val call: Call = client.newCall(request)
        var response: Response? = null
        val thread: Thread = Thread(Runnable {
            try {
                response = call.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread.start()
        thread.join()
        Log.i("ORSMApi", request.toString())
        if (response == null)
            return null
        else {
            Log.i("ORSMApi", response.toString())
            if (response!!.body == null)
                return null
            else {
                val str = response?.body!!.string()

                //  var pp =
                return Gson().fromJson<OSRMResponse>(str, OSRMResponse::class.java)
                Log.i("ORSMApi", str)
                str
            }
        }


    }


    fun getNearestToRoad(latLng: LatLng): OSRMResponse? {
        val baseUrlBuffer: StringBuilder = java.lang.StringBuilder()
        baseUrlBuffer.append("https://router.project-osrm.org/nearest/v1/")
        baseUrlBuffer.append("foot")
        baseUrlBuffer.append("/")
        baseUrlBuffer.append(latLng.longitude.toString())
        baseUrlBuffer.append(",")
        baseUrlBuffer.append(latLng.latitude.toString())
        baseUrlBuffer.append("?number=1&bearings=0,20")
        val baseUrl = baseUrlBuffer.toString()

        //"http://router.project-osrm.org/route/v1/driving/13.388860,52.517037;13.397634,52.529407;13.428555,52.523219?overview=false"

        val client: OkHttpClient = OkHttpClient()
        val builder = baseUrl.toHttpUrlOrNull()?.newBuilder()

        val request = Request.Builder()
            .url(builder.toString())
            .addHeader("User-Agent", useragent)
            .addHeader("Referer", "https://github.com/sun-jiao/LocalizedGeocoder/")
            .build()

        val call: Call = client.newCall(request)
        var response: Response? = null
        val thread: Thread = Thread(Runnable {
            try {
                response = call.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread.start()
        thread.join()
        Log.i("ORSMApi", request.toString())
        if (response == null)
            return null
        else {
            Log.i("ORSMApi", response.toString())
            if (response!!.body == null)
                return null
            else {
                val str = response?.body!!.string()

                //  var pp =
                return Gson().fromJson<OSRMResponse>(str, OSRMResponse::class.java)
                Log.i("ORSMApi", str)
                str
            }
        }


    }

    fun downloadUrl(url: String): String? {

        val client: OkHttpClient = OkHttpClient()
        val builder = url.toHttpUrlOrNull()?.newBuilder()

        val request = Request.Builder()
            .url(builder.toString())
            .addHeader("User-Agent", USER_AGENT)
            .addHeader("Referer", "https://github.com/sun-jiao/LocalizedGeocoder/")
            .build()

        val call: Call = client.newCall(request)
        var response: Response? = null
        // val thread: Thread = Thread(Runnable {
        try {
            response = call.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //   })
        //   thread.start()
        //   thread.join()
        Log.i("ORSMApi", request.toString())
        return if (response == null)
            null
        else {
            Log.i("ORSMApi", response.toString())
            if (response.body == null)
                null
            else {
                val str = response.body!!.string()

                Log.i("ORSMApi", str)
                str
            }
        }

    }


}


fun Context.getRoutBetweenCoordsByLib(points: ArrayList<LatLng>) {

    Thread {
        val roadManager: RoadManager = OSRMRoadManager(this, USER_AGENT)
        roadManager.addRequestOption("locale=" + resources.configuration.locale.language)
        roadManager.addRequestOption("vehicle=foot")
        roadManager.addRequestOption("geometries=geojson")
        val waypoints = ArrayList<GeoPoint>()
        points.forEach { latLng ->
            waypoints.add(GeoPoint(latLng.latitude, latLng.longitude))
        }
        val road = roadManager.getRoad(waypoints)

        val roadOverlay: Polyline = RoadManager.buildRoadOverlay(road)

    }.start()


}

fun GoogleMap.animateMarkerRoutBetweenCoordsByLib(
    activity: Activity,
    marker: Marker,
    points: ArrayList<LatLng>,
    rotate: Boolean,
    callback: OnCompleteCallback
) {


    Thread {
        val roadManager: RoadManager = OSRMRoadManager(activity, USER_AGENT)
        roadManager.addRequestOption(
            "locale=" + activity.resources.configuration.locale.language
        )
        roadManager.addRequestOption("vehicle=foot")
        val waypoints = ArrayList<GeoPoint>()
        points.forEach { latLng ->
            waypoints.add(GeoPoint(latLng.latitude, latLng.longitude))
        }
        val road = roadManager.getRoad(waypoints)


        val roadOverlay: Polyline = RoadManager.buildRoadOverlay(road)



        callback.onComplete(true, roadOverlay)
        //           roadOverlay.actualPoints.forEach { point ->
//                SmartMarker.moveMarkerSmoothly(marker, LatLng(point.latitude, point.longitude),rotate)
//            }


    }.start()


}


//------------------------

interface DownloadCallback {
    fun onDownload(data: String?)
}

class AsyncDownloadClass(val url: String, val callback: DownloadCallback) : ViewModel() {

    fun execute() = viewModelScope.launch {
        onPreExecute()
        val result = doInBackground() // runs in background thread without blocking the Main Thread
        onPostExecute(result)
    }

    private suspend fun doInBackground(): String =
        withContext(Dispatchers.IO) { // to run code in Background Thread

            var data: String? = ""
            // do async work
            Log.d("POLYLINES - 2 - INNER", "------------ 1 -------------")


            try {
                data = downloadUrl(url)

            } catch (e: Exception) {
                e.printStackTrace()
            }
//        delay(1000) // simulate async work
            return@withContext data.toString()
        }

    // Runs on the Main(UI) Thread
    private fun onPreExecute() {
        // show progress
    }

    // Runs on the Main(UI) Thread
    private fun onPostExecute(result: String) {
        // hide progress
        Log.d("POLYLINES - 2 - INNER", "------------ 2 -------------")
        callback.onDownload(result)
    }


    private fun downloadUrl(url: String): String? {
        val useragent = "InstantAlertNetwork / 1.0.0"
        val client: OkHttpClient = OkHttpClient()
        val builder = url.toHttpUrlOrNull()?.newBuilder()

        val request = Request.Builder()
            .url(builder.toString())
            .addHeader("User-Agent", useragent)
            .addHeader("Referer", "https://github.com/sun-jiao/LocalizedGeocoder/")
            .build()

        val call: Call = client.newCall(request)
        var response: Response? = null
        // val thread: Thread = Thread(Runnable {
        try {
            response = call.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //   })
        //   thread.start()
        //   thread.join()
        Log.i("ORSMApi", request.toString())
        return if (response == null)
            null
        else {
            Log.i("ORSMApi", response.toString())
            if (response.body == null)
                null
            else {
                val str = response.body!!.string()

                Log.i("ORSMApi", str)
                str
            }
        }

    }
}

fun Context.getTripBetweenCoordsAsync(
    markerKey: String,
    points: ArrayList<LatLng>,
    callback: DownloadCallback
) {
    val baseUrlBuffer: StringBuilder = java.lang.StringBuilder()
    baseUrlBuffer.append("https://router.project-osrm.org/trip/v1/")
    baseUrlBuffer.append("foot")
    baseUrlBuffer.append("/")
    var pointsCount = 0
    points.forEach { latLng ->
        if (pointsCount > 0) {
            baseUrlBuffer.append(";")
        }
        baseUrlBuffer.append(latLng.longitude.toString())
        baseUrlBuffer.append(",")
        baseUrlBuffer.append(latLng.latitude.toString())
        pointsCount++
    }
    baseUrlBuffer.append("?geometries=geojson&steps=true&roundtrip=false&source=first&destination=last&annotations=true")
    val baseUrl = baseUrlBuffer.toString()


    val asyncDownload: AsyncDownloadClass = AsyncDownloadClass(baseUrl, callback)
    asyncDownload.execute()

    Log.d("OSMR", baseUrl)

    Log.d("POLYLINES - INNER", "------------ 1 -------------")
    val thread: Thread = Thread(Runnable {
        var data: String? = ""
        try {
            data = downloadUrl(baseUrl)
            Log.d("POLYLINES - INNER", "------------ 2 -------------")
            callback.onDownload(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    })
    thread.start()
    thread.join()


}

fun downloadUrl(url: String): String? {
    val useragent = "InstantAlertNetwork / 1.0.0"
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val builder = url.toHttpUrlOrNull()?.newBuilder()


    val request = Request.Builder()
        .url(builder.toString())
        .addHeader("User-Agent", useragent)
        .addHeader("Referer", "https://github.com/sun-jiao/LocalizedGeocoder/")
        .build()

    val call: Call = client.newCall(request)


    var response: Response? = null
    // val thread: Thread = Thread(Runnable {
    try {
        response = call.execute()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    //   })
    //   thread.start()
    //   thread.join()
    Log.i("ORSMApi", request.toString())
    return if (response == null)
        null
    else {
        Log.i("ORSMApi", response.toString())
        if (response.body == null)
            null
        else {
            val str = response.body!!.string()

            Log.i("ORSMApi", str)
            str
        }
    }

}


class OSRMResponse {
    var code: String = ""
    var routes: ArrayList<OSRMRouteModel> = ArrayList<OSRMRouteModel>()
    var waypoints: ArrayList<OSMWaypoint> = ArrayList<OSMWaypoint>()
    var trips: ArrayList<OSMTrip> = ArrayList<OSMTrip>()
}

class OSRMRouteModel {
    var distance: Double = 0.0
    var geometry: String = ""
    var legs: List<OSMLeg> = ArrayList<OSMLeg>()
    var weight: Double = 0.0
    var weight_name: String = ""

}


class Geometry {
    var coordinates: List<List<Double>>? = null
    var type: String? = null
}


class OSMLeg {
    var distance: Double? = null
    var duration: Double? = null
    var steps: List<OSRMStep>? = null
}


class OSRMStep {
    var distance: Double = 0.0
    var driving_side: String = ""
    var duration: Double = 0.0
    var geometry: Geometry = Geometry()
    var intersections: List<OSRMIntersection> = ArrayList<OSRMIntersection>()
    var maneuver: OSRManeuver = OSRManeuver()
    var mode: String = ""
    var name: String = ""
    var weight: Double = 0.0

}

class OSRMIntersection {
    var bearings: List<Int> = ArrayList<Int>()
    var entry: List<Boolean> = ArrayList<Boolean>()
    var location: List<Double> = ArrayList<Double>()
    var out: Int = 0
}


class OSRManeuver {
    var bearing_after: Int = 0
    var bearing_before: Int = 0
    var location: List<Double> = ArrayList<Double>()
    var type: String = ""
}


class OSMWaypoint {
    var distance: Double = 0.0
    var hint: String = ""
    var location: List<Double> = ArrayList<Double>()
    var name: String = ""
    var nodes: List<Long> = ArrayList<Long>()
}


////---------------


class OSMTrip {
    var distance: Double = 0.0
    var duration: Double = 0.0
    var geometry: Geometry = Geometry()
    var legs: List<OSMLeg> = ArrayList<OSMLeg>()
    var weight: Double = 0.0
    var weight_name: String = ""
}

/*
class Intersection {
    var bearings: List<Int> = listOf()
    var entry: List<Boolean> = listOf()
    var `in`: Int = 0
    var location: List<Double> = listOf()
    var `out`: Int = 0
}

class Maneuver {
    var bearing_after: Int = 0
    var bearing_before: Int = 0
    var location: List<Double> = listOf()
    var modifier: String = ""
    var type: String = ""
}
*/

@SuppressLint("MissingPermission")
suspend fun Activity.getCurrentLocationAsAddress(): Resource<EventLocation?> {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(this)
    var toReturn: Resource<EventLocation?> =
        Resource.Error<EventLocation?>("Error undefined getting location")

    return if (checkLocationRequirementsFit()) {
        val location = fusedLocationClient.lastLocation.await()
        location?.let { location ->
            var response = GeoFunctions
                .getInstance(this)
                .getAddressFromLatLng(location.latitude, location.longitude)

            var address = EventLocation()
            try {
                with(response.data) {
                    address.latitude = this?.latitude!!
                    address.longitude = this.longitude
                    address.formated_address = this.formatedAddress
                    // no existe en nominatim -- location.address_components = nominatimAddress!!.
                    val zipCode: String = this.postalCode.toString()
                    zipCode?.let { zip ->
                        if (address.formated_address?.startsWith(zip) == true) {
                            address.formated_address =
                                address.formated_address?.replaceFirst(
                                    "$zip,", "", true
                                )
                        }
                    }

                }
                toReturn = Resource.Success<EventLocation?>(address) // return address
            } catch (ex: Exception) {
                toReturn = Resource.Error<EventLocation?>(ex.message.toString()) // return address
            }

        } ?: run {
            toReturn = Resource.Error<EventLocation?>("Error getting location")


        }
        toReturn
    } else {
        Resource.Error<EventLocation?>("Error: No location permissions")

    }
}