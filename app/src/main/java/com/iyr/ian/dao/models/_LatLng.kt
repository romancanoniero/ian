package com.iyr.ian.dao.models

import java.io.Serializable

open class _LatLng : Serializable {
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        //    if (o == null || getClass() != o.getClass()) return false;
        val that = o as _LatLng?
        return latitude == that!!.latitude && longitude == that.longitude
    }


}