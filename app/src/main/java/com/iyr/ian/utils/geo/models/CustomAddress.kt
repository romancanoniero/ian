package com.iyr.ian.utils.geo.models

import com.google.android.libraries.places.api.model.AddressComponents
import com.google.maps.android.geometry.Bounds
import org.json.JSONObject

class CustomAddress {
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