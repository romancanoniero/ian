package com.iyr.ian.utils.geo

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.SphericalUtil
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.geo.callbacks.LocationRequestCallback
import com.iyr.ian.utils.geo.models.CustomAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Collections
import java.util.Locale
import java.util.Random
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GeoFunctions(private val context: Context) {
    //-----
    fun getAddressFromLatLng(latitude: Double?, longitude: Double?, callback: OnCompleteCallback) {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation((latitude)!!, (longitude)!!, 1)!!
            if (Geocoder.isPresent()) {
                /*
                Toast.makeText(context, "geocoder present",
                        Toast.LENGTH_SHORT).show();
             */
                val address = CustomAddress()
                val returnAddress = addresses.get(0)
                address.latitude = returnAddress.latitude
                address.longitude = returnAddress.longitude
                address.country = returnAddress.countryName
                address.countryCode = returnAddress.countryCode
                address.city = returnAddress.adminArea
                address.suburb = returnAddress.subLocality
                address.streetName = returnAddress.thoroughfare
                address.streetNumber = returnAddress.subThoroughfare
                address.postalCode = returnAddress.postalCode
                val tempAddress = addresses.get(0).getAddressLine(0)
                // Extraigo el codigo postal_
                /*
                                int postalCodeStartPosition = 0;
                                int postalCodeLastPosition = 0;
                                try
                                {
                                 postalCodeStartPosition = temp_address.indexOf(returnAddress.getPostalCode());
                                 postalCodeLastPosition = temp_address.indexOf(" ", postalCodeStartPosition);
                                String postalCodeString = temp_address.substring(postalCodeStartPosition, postalCodeLastPosition);
                                }
                                catch (Exception ex)
                                {
                                    int pp=33;
                                }

                                //------
                                // tomo los datos del admin area
                                int adminAreaStartPosition = temp_address.indexOf(" ",postalCodeLastPosition );
                                int adminAreaEndPosition = temp_address.indexOf(",",adminAreaStartPosition + 1 );
                                String adminAreaString = temp_address.substring(adminAreaStartPosition, adminAreaEndPosition);
                                temp_address = temp_address.replace(adminAreaString,  "");
                // modifico el admin area
                                temp_address = temp_address.replace(adminAreaString,  returnAddress.getAdminArea());
                */

                address.formatedAddress = tempAddress
                /*
                String localityString = returnAddress.getLocality();
                String city = returnAddress.getCountryName();
                String region_code = returnAddress.getCountryCode();
                String zipcode = returnAddress.getPostalCode();
                StringBuilder str = new StringBuilder();
                str.append(localityString + " ");
                str.append(city + " ");
                str.append(region_code + " ");
             str.append(zipcode + " ");
*/
                // Add = str.toString();
                // longi.setText(str);
                callback.onComplete(true, address)
            } else Toast.makeText(
                context,
                "geocoder not present", Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            Toast.makeText(
                context, "Exception",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //-----------------------
    suspend fun getAddressFromLatLng(
        latitude: Double?,
        longitude: Double?
    ): Resource<CustomAddress> {


        val geocoder = Geocoder(context, Locale.getDefault())
        var addresses: List<Address>? = null
     return try {

            addresses = geocoder.getFromLocation((latitude)!!, (longitude)!!, 1)!!
            if (Geocoder.isPresent()) {
                val address = CustomAddress()
                val returnAddress = addresses.get(0)
                address.latitude = returnAddress.latitude
                address.longitude = returnAddress.longitude
                address.country = returnAddress.countryName
                address.countryCode = returnAddress.countryCode
                address.city = returnAddress.adminArea
                address.suburb = returnAddress.subLocality
                address.streetName = returnAddress.thoroughfare
                address.streetNumber = returnAddress.subThoroughfare
                address.postalCode = returnAddress.postalCode
                val tempAddress = addresses.get(0).getAddressLine(0)
                // Extraigo el codigo postal_
                /*
                                int postalCodeStartPosition = 0;
                                int postalCodeLastPosition = 0;
                                try
                                {
                                 postalCodeStartPosition = temp_address.indexOf(returnAddress.getPostalCode());
                                 postalCodeLastPosition = temp_address.indexOf(" ", postalCodeStartPosition);
                                String postalCodeString = temp_address.substring(postalCodeStartPosition, postalCodeLastPosition);
                                }
                                catch (Exception ex)
                                {
                                    int pp=33;
                                }

                                //------
                                // tomo los datos del admin area
                                int adminAreaStartPosition = temp_address.indexOf(" ",postalCodeLastPosition );
                                int adminAreaEndPosition = temp_address.indexOf(",",adminAreaStartPosition + 1 );
                                String adminAreaString = temp_address.substring(adminAreaStartPosition, adminAreaEndPosition);
                                temp_address = temp_address.replace(adminAreaString,  "");
                // modifico el admin area
                                temp_address = temp_address.replace(adminAreaString,  returnAddress.getAdminArea());
                */

                address.formatedAddress = tempAddress

                Resource.Success<CustomAddress>(address)
            } else {
                Resource.Error<CustomAddress>("geocoder not present")
            }
        } catch (e: IOException) {
            Resource.Error<CustomAddress>(e.message.toString())
        }


    }


    //   }).start();
    fun getCustomAddressFromGooglePlace(place: Place, callback: OnCompleteCallback) {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude, 1)
            if (Geocoder.isPresent()) {
                val selectedAddress = addresses?.get(0)!!
                val address = CustomAddress()
                address.latitude = place.latLng.latitude
                address.longitude = place.latLng.longitude
                address.country = selectedAddress.countryName
                address.countryCode = selectedAddress.countryCode
                address.city = selectedAddress.adminArea
                address.suburb = selectedAddress.subLocality
                address.streetName = selectedAddress.thoroughfare
                address.streetNumber = selectedAddress.subThoroughfare
                address.postalCode = selectedAddress.postalCode
                val tempAddress = selectedAddress.getAddressLine(0)
                address.formatedAddress = tempAddress
                callback.onComplete(true, address)
            }
        } catch (ex: Exception) {
        }
    }

    private fun getStreetNumber(component: AddressComponent): Boolean {
        return (component.types).contains(GOOGLE_PLACE_COMPONENT_TYPE_STREET_NUMBER)
    }

    private fun isStreetNumber(component: AddressComponent): Boolean {
        return (component.types).contains(GOOGLE_PLACE_COMPONENT_TYPE_STREET_NUMBER)
    }


    fun getLastKnownLocationAsAddress(
        activity: AppCompatActivity,
        callback: LocationRequestCallback
    ) {

        callback.onBeforeStart()

        //     Thread {

        //   activity.life

        //     GeoFunctions.getInstance(AppClass.instance)?.getLastKnownLocation { location ->


        activity.lifecycleScope.launch(Dispatchers.IO) {

            Nominatim.getAddressFromLatLng(
                AppClass.instance.lastLocation.value?.latitude!!,
                AppClass.instance.lastLocation.value?.longitude!!,
                object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        val nominatimAddress = result as NominatimAddress
                        val location = EventLocation()
                        location.latitude = nominatimAddress.latitude
                        location.longitude = nominatimAddress.longitude
                        location.formated_address = nominatimAddress.formatedAddress
                        // no existe en nominatim -- location.address_components = nominatimAddress!!.
                        val zipCode: String = nominatimAddress.postalCode.toString()
                        zipCode?.let { zip ->
                            if (location.formated_address?.startsWith(zip) == true) {
                                location.formated_address =
                                    location.formated_address?.replaceFirst("$zip,", "", true)
                            }
                        }

                        activity.runOnUiThread {
                            callback.onFinish(location)
                        }
                    }

                    override fun onError(exception: java.lang.Exception) {
                        //     super.onError(exception)
                        callback.onError(exception)
                    }
                })


        }

        //     }.start()


    }







    companion object {
        private const val ASSUMED_INIT_LATLNG_DIFF = 1.0
        private const val ACCURACY = 0.01f
        private const val GOOGLE_PLACE_COMPONENT_TYPE_STREET_NUMBER = "street_number"
        private lateinit var sSoleInstance: GeoFunctions
        fun getInstance(context: Context): GeoFunctions {
            if (!::sSoleInstance.isInitialized) { //if there is no instance available... create new one
                sSoleInstance = GeoFunctions(context)
            }
            return sSoleInstance
        }

        fun getDistanceBetweenTwoPoints(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Float {
            val distance = FloatArray(2)
            Location.distanceBetween(
                lat1, lon1,
                lat2, lon2, distance
            )
            return distance[0]
        }

        fun getDistanceTo(currentPosition: LatLng, pointPosition: LatLng): Float {
            return getDistanceBetweenTwoPoints(
                currentPosition.latitude,
                currentPosition.longitude,
                pointPosition.latitude,
                pointPosition.longitude
            )
        }

        fun formatDistance(distance: Float): String {
            var distance = distance
            val symbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
            val twoDForm = DecimalFormat("#.##", symbols)
            if (distance < 1000) {
                distance =
                    java.lang.Float.valueOf(twoDForm.format(distance.toDouble()).replace(",", "."))
                return twoDForm.format(distance.toDouble()) + " mts."
            } else {
                distance = java.lang.Float.valueOf(
                    twoDForm.format(
                        Convertions().convert(
                            "meters",
                            "kilometers",
                            java.lang.Float.valueOf(
                                twoDForm.format(distance.toDouble()).replace(",", ".")
                            ).toDouble()
                        )
                    ).replace(",", ".")
                )
                return twoDForm.format(distance.toDouble()) + " kms."
            }
        }

        fun toBounds(center: LatLng?, radiusInMeters: Double): LatLngBounds {
            val distanceFromCenterToCorner = radiusInMeters * sqrt(2.0)
            val southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
            val northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
            return LatLngBounds(southwestCorner, northeastCorner)
        }

        fun boundsWithCenterAndLatLngDistance(
            center: LatLng,
            latDistanceInMeters: Float,
            lngDistanceInMeters: Float
        ): LatLngBounds {
            var latDistanceInMeters = latDistanceInMeters
            var lngDistanceInMeters = lngDistanceInMeters
            latDistanceInMeters /= 2f
            lngDistanceInMeters /= 2f
            val builder = LatLngBounds.builder()
            val distance = FloatArray(1)
            run {
                var foundMax: Boolean = false
                var foundMinLngDiff: Double = 0.0
                var assumedLngDiff: Double = ASSUMED_INIT_LATLNG_DIFF
                do {
                    Location.distanceBetween(
                        center.latitude,
                        center.longitude,
                        center.latitude,
                        center.longitude + assumedLngDiff,
                        distance
                    )
                    val distanceDiff: Float = distance[0] - lngDistanceInMeters
                    if (distanceDiff < 0) {
                        if (!foundMax) {
                            foundMinLngDiff = assumedLngDiff
                            assumedLngDiff *= 2.0
                        } else {
                            val tmp: Double = assumedLngDiff
                            assumedLngDiff += (assumedLngDiff - foundMinLngDiff) / 2
                            foundMinLngDiff = tmp
                        }
                    } else {
                        assumedLngDiff -= (assumedLngDiff - foundMinLngDiff) / 2
                        foundMax = true
                    }
                } while (abs(distance[0] - lngDistanceInMeters) > lngDistanceInMeters * ACCURACY)
                val east: LatLng = LatLng(center.latitude, center.longitude + assumedLngDiff)
                builder.include(east)
                val west: LatLng = LatLng(center.latitude, center.longitude - assumedLngDiff)
                builder.include(west)
            }
            run {
                var foundMax: Boolean = false
                var foundMinLatDiff: Double = 0.0
                var assumedLatDiffNorth: Double = ASSUMED_INIT_LATLNG_DIFF
                do {
                    Location.distanceBetween(
                        center.latitude,
                        center.longitude,
                        center.latitude + assumedLatDiffNorth,
                        center.longitude,
                        distance
                    )
                    val distanceDiff: Float = distance[0] - latDistanceInMeters
                    if (distanceDiff < 0) {
                        if (!foundMax) {
                            foundMinLatDiff = assumedLatDiffNorth
                            assumedLatDiffNorth *= 2.0
                        } else {
                            val tmp: Double = assumedLatDiffNorth
                            assumedLatDiffNorth += (assumedLatDiffNorth - foundMinLatDiff) / 2
                            foundMinLatDiff = tmp
                        }
                    } else {
                        assumedLatDiffNorth -= (assumedLatDiffNorth - foundMinLatDiff) / 2
                        foundMax = true
                    }
                } while (abs(distance[0] - latDistanceInMeters) > latDistanceInMeters * ACCURACY)
                val north: LatLng = LatLng(center.latitude + assumedLatDiffNorth, center.longitude)
                builder.include(north)
            }
            run {
                var foundMax: Boolean = false
                var foundMinLatDiff: Double = 0.0
                var assumedLatDiffSouth: Double = ASSUMED_INIT_LATLNG_DIFF
                do {
                    Location.distanceBetween(
                        center.latitude,
                        center.longitude,
                        center.latitude - assumedLatDiffSouth,
                        center.longitude,
                        distance
                    )
                    val distanceDiff: Float = distance[0] - latDistanceInMeters
                    if (distanceDiff < 0) {
                        if (!foundMax) {
                            foundMinLatDiff = assumedLatDiffSouth
                            assumedLatDiffSouth *= 2.0
                        } else {
                            val tmp: Double = assumedLatDiffSouth
                            assumedLatDiffSouth += (assumedLatDiffSouth - foundMinLatDiff) / 2
                            foundMinLatDiff = tmp
                        }
                    } else {
                        assumedLatDiffSouth -= (assumedLatDiffSouth - foundMinLatDiff) / 2
                        foundMax = true
                    }
                } while (abs(distance[0] - latDistanceInMeters) > latDistanceInMeters * ACCURACY)
                val south: LatLng = LatLng(center.latitude - assumedLatDiffSouth, center.longitude)
                builder.include(south)
            }
            return builder.build()
        }

        /**
         * @param map The Map on which to compute the radius.
         * @return The circle radius of the area visible on map, in meters.
         */
        fun getRadiusVisibleOnMap(map: GoogleMap): Float {
            val visibleRegion = map.projection.visibleRegion
            val farRight = visibleRegion.farRight
            val farLeft = visibleRegion.farLeft
            val nearRight = visibleRegion.nearRight
            val nearLeft = visibleRegion.nearLeft
            val distanceWidth = FloatArray(1)
            Location.distanceBetween(
                (farRight.latitude + nearRight.latitude) / 2,
                (farRight.longitude + nearRight.longitude) / 2,
                (farLeft.latitude + nearLeft.latitude) / 2,
                (farLeft.longitude + nearLeft.longitude) / 2,
                distanceWidth
            )
            val distanceHeight = FloatArray(1)
            Location.distanceBetween(
                (farRight.latitude + nearRight.latitude) / 2,
                (farRight.longitude + nearRight.longitude) / 2,
                (farLeft.latitude + nearLeft.latitude) / 2,
                (farLeft.longitude + nearLeft.longitude) / 2,
                distanceHeight
            )
            val radius: Float
            radius = if (distanceWidth[0] > distanceHeight[0]) {
                distanceWidth[0]
            } else {
                distanceHeight[0]
            }
            return radius
        }

        fun getTimeTaken(source: Location, dest: Location): String {
            val meter = source.distanceTo(dest).toDouble()
            val kms = meter / 1000
            val kmsPerMin = 0.5
            val minsTaken = kms / kmsPerMin
            val totalMinutes = minsTaken.toInt()
            Log.d("ResponseT", "meter :$meter kms : $kms mins :$minsTaken")
            return if (totalMinutes < 60) {
                "$totalMinutes mins"
            } else {
                var minutes = (totalMinutes % 60).toString()
                minutes = if (minutes.length == 1) "0$minutes" else minutes
                (totalMinutes / 60).toString() + " hour " + minutes + "mins"
            }
        }

        fun getGeoLocationFromLatLng(context: Context, latLng: LatLng) {
            val addresses: List<Address>
            val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
            try {
                addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                )!! // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                val address =
                    addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val country = addresses[0].countryName
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName // Only if available else return NULL
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun getAddressStringRelativeToOtherAddress(
            formatedAddress: String,
            referenceLocation: Address
        ): String {
            var formatedAddress = formatedAddress
            if (formatedAddress.lastIndexOf(referenceLocation.countryName) > -1) {
                formatedAddress = formatedAddress.replace(", " + referenceLocation.countryName, "")
            }
            if (referenceLocation.locality != null && formatedAddress.lastIndexOf(referenceLocation.locality) > -1) {
                formatedAddress = formatedAddress.replace(referenceLocation.locality, "")
            }
            if (formatedAddress.lastIndexOf(referenceLocation.postalCode) > -1) {
                formatedAddress = formatedAddress.replace(referenceLocation.postalCode, "")
            }
            return formatedAddress
        }

        fun getAddressStringRelativeToOtherAddress(
            formatedAddress: String,
            referenceLocation: CustomAddress
        ): String {
            val auxAddress = Address(Locale.getDefault())
            auxAddress.countryName = referenceLocation.country
            auxAddress.postalCode = referenceLocation.postalCode.toString()
            auxAddress.locality = referenceLocation.suburb
            return getAddressStringRelativeToOtherAddress(formatedAddress, auxAddress)
        }

        /*

        public static float zoomForRadius(double distance, Activity ctx) {
            byte zoom = 1;
            WindowManager windowManager = (WindowManager) ctx.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            int widthInPixels = windowManager.getDefaultDisplay().getWidth();
            DisplayMetrics displayMetrics = new DisplayMetrics();
             windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            widthInPixels =  displayMetrics.widthPixels;
            double equatorLength = 6378136.28;//in meters
            double metersPerPixel = equatorLength / 256;
            while ((metersPerPixel * widthInPixels) > distance) {
                metersPerPixel /= 2;
                ++zoom;
            }
            if (zoom > 21)
                zoom = 21;
            if (zoom < 1)
                zoom = 1;
            return zoom;
        }
    */
        fun getZoomLevelFromCircle(circle: Circle?): Int {
            var zoomLevel = 11
            if (circle != null) {
                val radius = circle.radius + circle.radius / 2
                val scale = radius / 500
                zoomLevel = (16 - ln(scale) / ln(2.0)).toInt()
            }
            return zoomLevel
        }

        fun getZoomLevelFromRadius(_radius: Double): Int {
            val zoomLevel: Int
            val radius = _radius + _radius / 2
            val scale = radius / 500
            zoomLevel = (16 - ln(scale) / ln(2.0)).toInt()
            return zoomLevel
        }

        fun getRandomLocation(point: LatLng, radius: Int): LatLng {
            val randomPoints: MutableList<LatLng> = ArrayList()
            val randomDistances: MutableList<Float> = ArrayList()
            val myLocation = Location("")
            myLocation.latitude = point.latitude
            myLocation.longitude = point.longitude

            //This is to generate 10 random points
            for (i in 0..9) {
                val x0 = point.latitude
                val y0 = point.longitude
                val random = Random()

                // Convert radius from meters to degrees
                val radiusInDegrees = (radius / 111000f).toDouble()
                val u = random.nextDouble()
                val v = random.nextDouble()
                val w = radiusInDegrees * sqrt(u)
                val t = 2 * Math.PI * v
                val x = w * cos(t)
                val y = w * sin(t)

                // Adjust the x-coordinate for the shrinking of the east-west distances
                val newX = x / cos(y0)
                val foundLatitude = newX + x0
                val foundLongitude = y + y0
                val randomLatLng = LatLng(foundLatitude, foundLongitude)
                randomPoints.add(randomLatLng)
                val l1 = Location("")
                l1.latitude = randomLatLng.latitude
                l1.longitude = randomLatLng.longitude
                randomDistances.add(l1.distanceTo(myLocation))
            }
            //Get nearest point to the centre
            val indexOfNearestPointToCentre =
                randomDistances.indexOf(Collections.min(randomDistances))
            return randomPoints[indexOfNearestPointToCentre]
        }

        fun isLatLngBoundsInOther(
            newLatLngBounds: LatLngBounds,
            oldLatLngBounds: LatLngBounds
        ): Boolean {
            return (oldLatLngBounds.contains(
                LatLng(
                    newLatLngBounds.northeast.latitude,
                    newLatLngBounds.southwest.longitude
                )
            )
                    &&
                    oldLatLngBounds.contains(
                        LatLng(
                            newLatLngBounds.northeast.latitude,
                            newLatLngBounds.northeast.longitude
                        )
                    )
                    &&
                    oldLatLngBounds.contains(
                        LatLng(
                            newLatLngBounds.southwest.latitude,
                            newLatLngBounds.southwest.longitude
                        )
                    )
                    &&
                    oldLatLngBounds.contains(
                        LatLng(
                            newLatLngBounds.southwest.latitude,
                            newLatLngBounds.northeast.longitude
                        )
                    ))
        }
    }
}
/*
class CustomAddress() {
    //   public lateinit var boundingBox: String
    var jsonObject: JSONObject? = null
    var suburb: String? = null
    var city: String? = null
    var state: String? = null
    var country: String? = null
    var countryCode: String? = null
    var postalCode: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var formatedAddress: String? = null
    var boundingBox: Bounds? = null
    var streetName: String? = null
    var streetNumber: String? = null
    var addressComponentes: AddressComponents? = null
}


*/


fun LatLng.calcularCoordenadaOpuesta( vertice: LatLng): LatLng {
    val radioTierra = 6371.0 // Radio de la Tierra en kilómetros

    // Calcula la distancia entre el centro y el vértice
    val dLat = Math.toRadians(vertice.latitude - this.latitude)
    val dLon = Math.toRadians(vertice.longitude - this.longitude)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(this.latitude)) * cos(Math.toRadians(vertice.latitude)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val distancia = radioTierra * c

    // Calcula el ángulo entre el centro y el vértice
    val angulo = atan2(sin(dLon) * cos(Math.toRadians(vertice.latitude)), cos(Math.toRadians(this.latitude)) * sin(Math.toRadians(vertice.latitude)) - sin(Math.toRadians(this.latitude)) * cos(Math.toRadians(vertice.latitude)) * cos(dLon))

    // Calcula la coordenada opuesta
    val latitudOpuesta = asin(sin(Math.toRadians(this.latitude)) * cos(2 * distancia / radioTierra) + cos(Math.toRadians(this.latitude)) * sin(2 * distancia / radioTierra) * cos(angulo))
    val longitudOpuesta = Math.toRadians(this.longitude) + atan2(sin(angulo) * sin(2 * distancia / radioTierra) * cos(Math.toRadians(this.latitude)), cos(2 * distancia / radioTierra) - sin(Math.toRadians(this.latitude)) * sin(latitudOpuesta))

    return LatLng(Math.toDegrees(latitudOpuesta), Math.toDegrees(longitudOpuesta))
}


fun LatLng.calcularLongitudNegativa(puntoHorizontal: LatLng): LatLng {
    val longitudNegativa = this.longitude - (puntoHorizontal.longitude - this.longitude)
    return LatLng(this.latitude, longitudNegativa)
}


fun LatLng.calcularLatitudNegativa(puntoVertical: LatLng): LatLng {
    val latitudNegativa = this.latitude - (puntoVertical.latitude - this.latitude)
    return LatLng(latitudNegativa, this.longitude)
}