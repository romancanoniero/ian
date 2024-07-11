package com.iyr.ian.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsLeg
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.Duration
import com.google.maps.model.LatLng
import com.google.maps.model.TravelMode
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.coroutines.resume

class MapsApiUtils {
    private var context: Context? = null
    private var sSoleInstance: MapsApiUtils? = null

    fun getInstance(context: Context): MapsApiUtils {

        if (sSoleInstance == null) { //if there is no instance available... create new one
            this.context = context
            sSoleInstance = MapsApiUtils()
        }
        return sSoleInstance!!
    }

    /**
    Use Google's directions api to calculate the estimated time needed to
    drive from origin to destination by car.

    @param origin The address/coordinates of the origin (see {@link DirectionsApiRequest#origin(String)} for more information on how to format the input)
    @param destination The address/coordinates of the destination (see {@link DirectionsApiRequest#destination(String)} for more information on how to format the input)

    @return The estimated time needed to travel human-friendly formatted
     */
    fun getDurationForRoute(origin: String, destination: String, travelMode: TravelMode): String {
        // - We need a context to access the API
        val geoApiContext: GeoApiContext =
            GeoApiContext.Builder().apiKey(context?.getString(R.string.google_api_key)).build()


        // - Perform the actual request
        val directionsResult: DirectionsResult =
            DirectionsApi.newRequest(geoApiContext).mode(travelMode).origin(origin)
                .destination(destination).await()

        // - Parse the result
        val route: DirectionsRoute = directionsResult.routes[0]
        val leg: DirectionsLeg = route.legs[0]
        val duration: Duration = leg.duration
        return duration.humanReadable
    }


    fun getDurationForRoute(origin: LatLng, destination: LatLng, travelMode: TravelMode): String {
        // - We need a context to access the API
        val geoApiContext: GeoApiContext =
            GeoApiContext.Builder().apiKey(context?.getString(R.string.google_api_key)).build()

        // - Perform the actual request
        val directionsResult: DirectionsResult =
            DirectionsApi.newRequest(geoApiContext).mode(travelMode).origin(origin)
                .destination(destination).await()

        // - Parse the result
        val route: DirectionsRoute = directionsResult.routes[0]
        val leg: DirectionsLeg = route.legs[0]
        val duration: Duration = leg.duration
        return duration.humanReadable
    }


    fun getCarRotation(startLL: LatLng, endLL: LatLng): Float {
        val latDifference: Double = kotlin.math.abs(startLL.lat - endLL.lat)
        val lngDifference: Double = kotlin.math.abs(startLL.lng - endLL.lng)
        var rotation = -1F
        when {
            startLL.lat < endLL.lat && startLL.lng < endLL.lng -> {
                rotation = Math.toDegrees(kotlin.math.atan(lngDifference / latDifference)).toFloat()
            }

            startLL.lat >= endLL.lat && startLL.lng < endLL.lng -> {
                rotation =
                    (90 - Math.toDegrees(kotlin.math.atan(lngDifference / latDifference)) + 90).toFloat()
            }

            startLL.lat >= endLL.lat && startLL.lng >= endLL.lng -> {
                rotation =
                    (Math.toDegrees(kotlin.math.atan(lngDifference / latDifference)) + 180).toFloat()
            }

            startLL.lat < endLL.lat && startLL.lng >= endLL.lng -> {
                rotation =
                    (90 - Math.toDegrees(kotlin.math.atan(lngDifference / latDifference)) + 270).toFloat()
            }
        }
        return rotation
    }

    fun polyAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(0, 100)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 4000
        return valueAnimator
    }

    fun carAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 3000
        valueAnimator.interpolator = LinearInterpolator()
        return valueAnimator
    }


    fun getDirectionData(srcPlace: String, destPlace: String) {

        Thread {
            val urlStringBuilder = StringBuilder()
            urlStringBuilder.append("http://maps.google.com/maps?f=d&hl=en")
            urlStringBuilder.append("&saddr=")
            urlStringBuilder.append(srcPlace)
            urlStringBuilder.append("&daddr=")
            urlStringBuilder.append(destPlace)
            urlStringBuilder.append("&ie=UTF8&0&om=0&output=xml")

            val urlString: String = urlStringBuilder.toString()

            /*
                    var urlString: String = "http://maps.google.com/maps?f=d&hl=en&saddr="
                    +srcPlace + "&daddr=" + destPlace
                    +"&ie=UTF8&0&om=0&output=kml"
            */
            Log.d("URL", urlString)

            val doc: Document? = null
            var urlConnection: HttpURLConnection? = null
            var url: URL? = null
            var pathContent: String = ""

            try {

                url = URL(urlString)

                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.setRequestMethod("GET")

                urlConnection.addRequestProperty("User-Agent", "***SDK/1.0")
                urlConnection.setRequestProperty("Cache-Control", "no-cache")


                //     urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true)
                urlConnection.connect()
                val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                val db: DocumentBuilder = dbf.newDocumentBuilder()
                var doc = db.parse(urlConnection.inputStream)

            } catch (e: Exception) {

                var p = 3
            }

            val nl: NodeList? = doc?.getElementsByTagName("LineString")

            for (s in 0..nl?.length!!) {
                val rootNode: Node = nl.item(s)
                val configItems: NodeList = rootNode.childNodes
                for (x in 0..configItems.length) {
                    val lineStringNode: Node = configItems.item(x)
                    val path: NodeList = lineStringNode.childNodes
                    pathContent = path.item(0).nodeValue
                }
            }
            var tempContent = pathContent.split(" ")
//                return tempContent;
        }.start()


    }
}


fun GoogleMap.moveMapCamera(latLng: com.google.android.gms.maps.model.LatLng) {
    this.moveMapCamera(latLng, 18f)
}

fun GoogleMap.moveMapCamera(latLng: com.google.android.gms.maps.model.LatLng, zoomLevel: Float) {
    val camera = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
    moveCamera(camera)
}

fun GoogleMap.animateMapCamera(zoom: Float) {

    animateCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition.target, zoom))
}

fun GoogleMap.animateMapCamera(latLng: com.google.android.gms.maps.model.LatLng) {
    animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
}


fun GoogleMap.zoomToFitMarkers(
    positions: ArrayList<com.google.android.gms.maps.model.LatLng>,
    width: Int,
    height: Int,
    callback: OnCompleteCallback?
) {
//Calculate the markers to get their position
    val b: LatLngBounds.Builder = LatLngBounds.Builder()
    for (latLng: com.google.android.gms.maps.model.LatLng in positions) {
        b.include(latLng)

    }
    val bounds: LatLngBounds = b.build()


    //Change the padding as per needed
//    var cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,width, width, 20.px);
    val cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 20.px)
    animateCamera(cu)
    callback?.onComplete(true, bounds)
}
/*
suspend fun GoogleMap.zoomToFitMarkers(
    positions: ArrayList<com.google.android.gms.maps.model.LatLng>,
    center: LatLng,
    force: Boolean
): Boolean {
    try {
        //Calculate the markers to get their position
        val b: LatLngBounds.Builder = LatLngBounds.Builder()
        for (latLng: com.google.android.gms.maps.model.LatLng in positions) {
            b.include(latLng)
        }
        val bounds: LatLngBounds = b.build()


        //Change the padding as per needed
//    var cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,width, width, 20.px);
        val cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 20.px)
        animateCamera(cu)
        return true
    } catch (exception: Exception) {
        return false
    }
}
*/

suspend fun GoogleMap.zoomToFitMarkers(
    markers: ArrayList<Marker>,
    center: LatLng,
    force: Boolean
): Boolean {
    try {
        //Calculate the markers to get their position
        val b: LatLngBounds.Builder = LatLngBounds.Builder()
        for (marker: Marker in markers) {
            b.include(marker.position)
        }
        // agrego la posicion central
        var centralPoint : com.google.android.gms.maps.model.LatLng = com.google.android.gms.maps.model.LatLng(center.lat,center.lng)
        b.include(centralPoint)
        val bounds: LatLngBounds = b.build()


        //Change the padding as per needed
//    var cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,width, width, 20.px);
        val cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 20.px)
        animateCamera(cu)
        return true
    } catch (exception: Exception) {
        return false
    }
}

fun GoogleMap.zoomToBounds(bounds: LatLngBounds): LatLngBounds {
//Calculate the markers to get their position
    //Change the padding as per needed
    val cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 20)
    animateCamera(cu)
    return bounds
}


fun GoogleMap.animateView(ll: com.google.android.gms.maps.model.LatLng) {
    val cameraPosition = CameraPosition.Builder().target(ll).zoom(15.5f).build()
    animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}

fun getMarkerFromView(
    context: Context, layout: Int, userImage: Bitmap?, res: Int, heightVale: Float, npos: Int
): Bitmap? {

    val view = LayoutInflater.from(context).inflate(layout, null)
    val root = view.findViewById<ConstraintLayout>(R.id.root)/*
      view.measure(0,0)
        root.layoutParams = ConstraintLayout.LayoutParams(0, 45)
    */


    if (view.findViewById<View>(R.id.user_image) != null) {
        val imageView = view.findViewById<CircleImageView>(R.id.user_image) as CircleImageView
        imageView.setImageBitmap(userImage)
        if (res != 0) {
            imageView.setBackgroundResource(res)
        }
    }


    var imageWidth: Int = heightVale.toInt()
    var imageHeight: Int =
        heightVale.toInt()
    return context.loadBitmapFromView(view, imageWidth, imageHeight)
}




fun GoogleMap.drawBounds( limites: LatLngBounds): Polygon {
    val opcionesRecuadro = PolygonOptions()
        .add(limites.southwest)
        .add(com.google.android.gms.maps.model.LatLng(limites.southwest.latitude, limites.northeast.longitude))
        .add(limites.northeast)
        .add(com.google.android.gms.maps.model.LatLng(limites.northeast.latitude, limites.southwest.longitude))
        .add(limites.southwest)

   return this.addPolygon(opcionesRecuadro)
}


fun GoogleMap.drawControlCircle(latLng: com.google.android.gms.maps.model.LatLng, resColor : Int, newRadius : Double = 100.0): Circle {

    val circleOptions = com.google.android.gms.maps.model.CircleOptions()
        .center(latLng)
        .radius(newRadius)
        .strokeWidth(2f)
        .strokeColor(resColor)
        .fillColor(resColor)
    return this.addCircle(circleOptions)
}
/*
fun GoogleMap.generateMarkerIconForEvent(viewer: Viewer, callback: OnCompleteCallback) {

    var zoomFactor: Float = 1f
    if (cameraPosition.zoom <= 10) {
        zoomFactor = .4f
    } else if (cameraPosition.zoom > 10 && cameraPosition.zoom <= 16) {
        zoomFactor = cameraPosition.zoom / 16
    } else
        zoomFactor = 1f

    when (viewer.user_type) {
        UserType.COMMON_USER.name -> {
            var callback: OnCompleteCallback = object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {
                    var markerOptions = result as MarkerOptions
                    callback.onComplete(true, markerOptions)
                }
            }

            getUserMarkerOptions(
                markerPosition,
                viewer.user_key,
                viewer.display_name,
                viewer.profile_image_path,
                viewer.is_author,
                callback
            )
        }
        UserType.POLICE_CAR.name -> {

            var iconWidth = 18.px
            iconWidth = (zoomFactor * iconWidth).toInt()

            Log.d("ZOOM_VARIATION", "icon witdh = " + iconWidth.toString())
            var iconAspectRatio = "v,43.70:100"
            val dimensionsMap = MathUtils.calculateNewSizeWithRatio(iconWidth, "H,43.70:100")
            Log.d("ZOOM_VARIATION", "dimensionsMap = " + dimensionsMap.toString())
            val markerOptions =
                getVehicleMarkerOptions(
                    markerPosition,
                    R.drawable.vehicle_police_car,
                    dimensionsMap.get("width").toString().toInt(),
                    dimensionsMap.get("height").toString().toInt()
                )
            callback.onComplete(true, markerOptions)

        }
        UserType.AMBULANCE.name -> {
            var iconWidth = 18.px
            iconWidth = (zoomFactor * iconWidth).toInt()

            Log.d("ZOOM_VARIATION", "icon witdh = " + iconWidth.toString())
            var iconAspectRatio = "v,43.70:100"
            val dimensionsMap = MathUtils.calculateNewSizeWithRatio(iconWidth, "H,48.97:100")
            Log.d("ZOOM_VARIATION", "dimensionsMap = " + dimensionsMap.toString())
            val markerOptions =
                getVehicleMarkerOptions(
                    markerPosition,
                    R.drawable.vehicle_ambulance,
                    dimensionsMap.get("width").toString().toInt(),
                    dimensionsMap.get("height").toString().toInt()
                )
            callback.onComplete(true, markerOptions)
        }

        UserType.FIRE_TRUCK.name -> {
            var iconWidth = 18.px
            //          var zoomLevelDefault = closestZoom
            //            var zoomFactor = (mMap!!.cameraPosition.zoom * 100) / zoomLevelDefault
            //            iconWidth = ((zoomFactor / 100) * iconWidth).toInt()
            iconWidth = (zoomFactor * iconWidth).toInt()

            Log.d("ZOOM_VARIATION", "icon witdh = " + iconWidth.toString())
            var iconAspectRatio = "v,43.70:100"
            val dimensionsMap = MathUtils.calculateNewSizeWithRatio(iconWidth, "H,33.11:100")
            Log.d("ZOOM_VARIATION", "dimensionsMap = " + dimensionsMap.toString())
            val markerOptions =
                getVehicleMarkerOptions(
                    markerPosition,
                    R.drawable.vehicle_firetruck,
                    dimensionsMap.get("width").toString().toInt(),
                    dimensionsMap.get("height").toString().toInt()
                )
            callback.onComplete(true, markerOptions)
        }

    }


}
*/
fun GoogleMap.getUserMarkerOptions(
    ll: com.google.android.gms.maps.model.LatLng,
    userKey: String,
    resourceLocation: Any,
    isAuthor: Boolean,
    callback: OnCompleteCallback
) {

    val storageRepository: StorageRepositoryImpl = StorageRepositoryImpl()

    val viewerMarker: MarkerOptions = MarkerOptions().position(ll).flat(true)
    if (resourceLocation is String) {
        val profileImagePath: String = resourceLocation.toString()

        GlobalScope.launch(Dispatchers.IO) {
            val storageReference = storageRepository.getFileUrl(
                AppConstants.PROFILE_IMAGES_STORAGE_PATH,
                userKey,
                profileImagePath
            )

            /*
                        FirebaseStorage.getInstance().getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                            .child(userKey).child(profileImagePath)
            */
            GlideApp.with(AppClass.instance).asBitmap().load(storageReference)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap?>?
                    ) {
                        var markerIcon: Bitmap? = null
                        if (!isAuthor) {
                            markerIcon = getMarkerFromView(
                                AppClass.instance,
                                R.layout.custom_marker_pin_viewer_circle_point,
                                resource,
                                0,
                                AppClass.instance.resources.getDimension(R.dimen.marker_user_image_size)
                                    .toFloat(),
                                0
                            )
                        } else {
                            markerIcon = getMarkerFromView(
                                AppClass.instance,
                                R.layout.custom_marker_pin_viewer_circle_point_author,
                                resource,
                                0,
                                AppClass.instance.resources.getDimension(R.dimen.marker_user_image_size)
                                    .toInt().toFloat(),
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

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })

        }


    }
}


// Método para calcular el tamaño del marcador en función del nivel de zoom
fun GoogleMap.calculateMarkerSize(baseMarkerSize : Int, zoomLevel: Float): Float {
    // Ajusta esta escala según tus preferencias y prueba
    val scale = 1.0f

    // Calcula el tamaño del marcador en función del nivel de zoom
    var markerSize: Float = baseMarkerSize * zoomLevel * scale
    // Limita el tamaño del marcador para evitar que sea demasiado pequeño o grande
    markerSize = Math.min(Math.max(markerSize, 8.0f), 20.0f)
    return markerSize
}

//-------------------------------------
suspend fun GoogleMap.getUserMarkerOptions(
    ll: com.google.android.gms.maps.model.LatLng,
    userKey: String,
    resourceLocation: Any,
    isAuthor: Boolean
): MarkerOptions = suspendCancellableCoroutine { continuation ->

    val storageRepository: StorageRepositoryImpl = StorageRepositoryImpl()

    val viewerMarker: MarkerOptions = MarkerOptions().position(ll).flat(true)
    if (resourceLocation is String) {
        val profileImagePath: String = resourceLocation.toString()


        GlobalScope.launch(Dispatchers.IO) {

            var localStorageFolder = AppConstants.PROFILE_IMAGES_STORAGE_PATH + userKey+"/"
            var fileLocation = FirebaseStorage.getInstance()
                .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                .child(userKey)
                .child(profileImagePath)
                .downloadUrlWithCache(AppClass.instance,localStorageFolder)

            GlideApp.with(AppClass.instance)
                .asBitmap()
                .load(fileLocation)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap?>?
                    ) {
                        var markerIcon: Bitmap? = null
                        if (!isAuthor) {
                            markerIcon = getMarkerFromView(
                                AppClass.instance,
                                R.layout.custom_marker_pin_viewer_circle_point,
                                resource,
                                0,
                                AppClass.instance.resources.getDimension(R.dimen.marker_user_image_size)
                                    .toFloat(),
                                0
                            )
                        } else {
                            markerIcon = getMarkerFromView(
                                AppClass.instance,
                                R.layout.custom_marker_pin_viewer_circle_point_author,
                                resource,
                                0,
                                AppClass.instance.resources.getDimension(R.dimen.marker_user_image_size)
                                    .toInt().toFloat(),
                                0
                            )
                        }

                        viewerMarker.icon(
                            BitmapDescriptorFactory.fromBitmap(
                                markerIcon!!
                            )
                        )
                        continuation.resume(viewerMarker)
//                    callback.onComplete(true, viewerMarker)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

  }
}