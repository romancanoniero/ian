package com.iyr.ian.dao.models

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class LocationUpdate :  Serializable {

  var time: Long = 0
    lateinit var location: LatLng
    lateinit var visibilityStatus: String



}