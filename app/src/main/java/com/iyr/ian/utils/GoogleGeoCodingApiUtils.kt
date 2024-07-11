package com.iyr.ian.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.maps.model.AddressComponentType
import com.iyr.ian.BuildConfig
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback


class GoogleGeoCodingApiUtils(val context: Context) {
    private var sSoleInstance: GoogleGeoCodingApiUtils? = null

    fun getInstance(context: Context): GoogleGeoCodingApiUtils {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            sSoleInstance = GoogleGeoCodingApiUtils(context)
        }
        return sSoleInstance!!
    }

    fun getAddressComponent(
        components: AddressComponents?,
        componentType: AddressComponentType
    ): Any? {
        if (!(components is AddressComponents)) {
            throw Exception("Address Components is not an array")
        }


        for (i in 0..components.asList().size - 1) {
            val component: AddressComponent = components.asList()[i]
            if (component.types.contains(componentType.toString())) {
                return component.name
            }
        }

        return null

    }

    @SuppressLint("MissingPermission")
    fun getPlaceAtCurrentLocation(callback: OnCompleteCallback) {

        throw Exception("No usar esta API")

        AppClass.instance.getCurrentActivity()?.requestPermissionsLocation()

        if (!Places.isInitialized()) {

            Places.initialize(
                context,
                BuildConfig.MAPS_API_KEY
            )
        }

        val mPlaceDetectionClient = Places.createClient(this.context)


        val placeFields: List<Place.Field> = listOf(
            Place.Field.ID,
            Place.Field.ADDRESS
        )

        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        val placeResult = mPlaceDetectionClient.findCurrentPlace(request)
        placeResult.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val placeId = (task.result as FindCurrentPlaceResponse).placeLikelihoods[0].place.id
                val placeRequest = FetchPlaceRequest.newInstance(
                    placeId,
                    listOf(
                        Place.Field.ID,
                        Place.Field.ADDRESS,
                        Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.LAT_LNG
                    )
                )
                val placeInfoResult = mPlaceDetectionClient.fetchPlace(placeRequest)
                placeInfoResult.addOnCompleteListener { response ->
                    callback.onComplete(true, response.result.place)
                }

/*
                val response = (task.result)!!
                for (placeLikelihood: PlaceLikelihood in response.placeLikelihoods) {
                    var pepe = 3

                    /*
                        Log.i(
                            PackageManagerCompat.LOG_TAG, java.lang.String.format(
                                "Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getAddress(),
                                placeLikelihood.getLikelihood()
                            )
                        )
                        */

                }

            */
            } else {
                //     Log.e(PackageManagerCompat.LOG_TAG, "Error finding current location")
            }
        }

    }

    /*
    private fun isStreetNumber(component: AddressComponent): Boolean {

        return (component.types as ArrayList<AddressComponentType>).contains(AddressComponentType.STREET_NUMBER)
    }


    fun isStreetAddress(component: AddressComponent): Boolean {
        return (component.types as ArrayList<AddressComponentType>).contains(AddressComponentType.ROUTE)
    }

    fun isState(component: AddressComponent): Boolean {
        //  return component.types.indexOf("administrative_area_level_1")>-1
        return (component.types as ArrayList<AddressComponentType>).contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1)


    }

    fun isCity(component: AddressComponent): Boolean {
        return (component.types as ArrayList<AddressComponentType>).contains(AddressComponentType.SUBLOCALITY)
        return true
    }

    fun isCountry(component: AddressComponent): Boolean {
        return (component.types as ArrayList<AddressComponentType>).contains(AddressComponentType.COUNTRY)
        //    return true
    }

    fun isPostalCode(component: AddressComponent): Boolean {
        //return component.types.indexOf("postal_code")>-1
        return (component.types as ArrayList<AddressComponentType>).contains(AddressComponentType.POSTAL_CODE)
        //return true
    }
    */
}