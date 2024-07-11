package com.iyr.ian.utils.geo

import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Bounds
import com.iyr.ian.callbacks.OnCompleteCallback
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.Locale


/**
 *
 * @author sun-jiao (孙娇）
 *
 */


class NominatimAddress(
    jsonObject: JSONObject,
    private val streetName: String,
    private val streetNumber: String,
    val suburb: String,
    private val city: String,
    val state: String,
    val country: String,
    private val countryCode: String,
    val postalCode: Int,
    val latitude: Double,
    val longitude: Double,
    var formatedAddress: String,
    val boundingBox: Bounds
) {

    init {
        formatAddress()
    }
    //   public lateinit var boundingBox: String

    private fun formatAddress() {
        //-----

        when (countryCode) {
            "ar" -> {
                val builder = StringBuilder()
                builder.append(this.streetName)
                    .append(" ")
                    .append(this.streetNumber)
                    .append(", ")
                    .append(this.city)
                this.formatedAddress = builder.toString()

            }
            else -> {
                this.formatedAddress = "Pais desconocido"

            }
        }
    }
}


class Nominatim//according to Nominatim ToS, user agent is necessary

    (
    @NonNull private val latitude: Double,
    @NonNull private val longitude: Double,
    @NonNull private val language: String,
    @NonNull private val useragent: String,
    private val baseUrl: String = "https://nominatim.openstreetmap.org/reverse?"
) {
    val TAG = "Nominatim"


    companion object {

        private const val userAgent = "SunjiaoLocalizedGeocoderSample / 1.0.0"
        fun getAddressFromLatLng(lat: Double, lng: Double, callback: OnCompleteCallback) {
            var instance: Nominatim? = null
            val thread = Thread {
                Looper.prepare()
                instance = Nominatim(lat, lng, Locale.getDefault().displayLanguage, userAgent)
            }
            thread.start()
            thread.join()
            //          AppClass.instance.getCurrentActivity()?.runOnUiThread {
            callback.onComplete(true, instance!!.getAddress())

            //        }

        }

        fun getAddressFromLatLng(
            lat: Double,
            lng: Double,
            zoomLevel: Int,
            callback: OnCompleteCallback
        ) {
            Thread {
                Looper.prepare()
                val instance = Nominatim(lat, lng, Locale.getDefault().displayLanguage, userAgent)


                callback.onComplete(true, instance.getAddress(zoomLevel))
            }.start()

        }


        const val NOMINATIM_ZOOM_STATE = 7
        const val NOMINATIM_ZOOM_CITY = 11
        const val NOMINATIM_ZOOM_SUBURB = 13
        const val NOMINATIM_ZOOM_STREET = 16
        const val NOMINATIM_ZOOM_BUILDING = 18

    }


    constructor(latitude: Double, longitude: Double, language: String, useragent: String)
            : this(
        latitude,
        longitude,
        language,
        useragent,
        "https://nominatim.openstreetmap.org/reverse?"
    )

    constructor(geoPoint: LatLng, language: String, useragent: String, baseUrl: String)
            : this(geoPoint.latitude, geoPoint.longitude, language, useragent, baseUrl)

    constructor(geoPoint: LatLng, language: String, useragent: String)
            : this(geoPoint.latitude, geoPoint.longitude, language, useragent)

    constructor(
        latitudeF: Float,
        longitudeF: Float,
        language: String,
        useragent: String,
        baseUrl: String
    )
            : this(latitudeF.toDouble(), longitudeF.toDouble(), language, useragent, baseUrl)

    constructor(latitudeF: Float, longitudeF: Float, language: String, useragent: String)
            : this(latitudeF.toDouble(), longitudeF.toDouble(), language, useragent)


    private fun getJSON(): String? {
        return getJSON(NOMINATIM_ZOOM_BUILDING)
    }

    private fun getJSON(zoom: Int): String? {
        val client: OkHttpClient = OkHttpClient()
        val builder = baseUrl.toHttpUrlOrNull()?.newBuilder()
        builder?.addQueryParameter("format", "json")
        builder?.addQueryParameter("lat", latitude.toString())
        builder?.addQueryParameter("lon", longitude.toString())
        builder?.addQueryParameter("zoom", zoom.toString())
        builder?.addQueryParameter("addressdetails", "1")
        builder?.addQueryParameter("accept-language", language)
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
        Log.i(TAG, request.toString())
        return if (response == null)
            null
        else {
            Log.i(TAG, response.toString())
            if (response!!.body == null)
                null
            else {
                val str = response?.body!!.string()
                Log.i(TAG, str)
                str
            }
        }
    }

    fun getAddress(): NominatimAddress? {
        return getAddress(NOMINATIM_ZOOM_BUILDING)
    }

    fun getAddress(zoom: Int): NominatimAddress? {
        val str = getJSON(zoom)
        if (str != null) {
            val json: JSONObject = JSONObject(str)
            Log.i(TAG, json.toString())
            if (json != null && !json.isNull("address") && !json.isNull("display_name")) {
                val coords = json.getString("boundingbox")
                    .replace("[", "")
                    .replace("]", "")
                    .replace('"', ' ').split(",")
                val boundings: Bounds = Bounds(
                    coords[0].toDouble(),
                    coords[1].toDouble(),
                    coords[2].toDouble(),
                    coords[3].toDouble()
                )


                var retStreetName = ""
                try {
                    retStreetName = json.getJSONObject("address").getString("road")
                } catch (ex: java.lang.Exception) {
                }

                var retStreetNumber = ""
                try {
                    retStreetNumber = json.getJSONObject("address").getString("house_number")
                } catch (ex: java.lang.Exception) {
                }


                var retSuburb = ""
                try {
                    retSuburb = json.getJSONObject("address").getString("suburb")
                } catch (ex: java.lang.Exception) {
                }

                var retCity = ""
                try {
                    retCity = json.getJSONObject("address").getString("city")
                } catch (ex: java.lang.Exception) {
                }

                var retState = ""
                try {
                    retState = json.getJSONObject("address").getString("state") as String
                } catch (ex: java.lang.Exception) {
                }

                var retCountry = ""
                try {
                    retCountry = json.getJSONObject("address").getString("country")
                } catch (ex: java.lang.Exception) {
                }

                var retPostalCode = 0
                try {
                    retPostalCode = json.getJSONObject("address").getInt("postcode")
                } catch (ex: java.lang.Exception) {
                }

                return try {
                    NominatimAddress(
                        json.getJSONObject("address"),
                        retStreetName,
                        retStreetNumber,
                        retSuburb,
                        retCity,
                        retState,
                        retCountry,
                        json.getJSONObject("address").getString("country_code") as String,
                        retPostalCode,
                        latitude,
                        longitude,
                        json.getString("display_name"),
                        boundings
                    )
                }
                catch (exception : Exception)
                {
                    val ee=33
                    null
                }
            } else
                return null
        } else
            return null
    }


}