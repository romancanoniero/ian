package com.iyr.ian.utils

import android.animation.ValueAnimator
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.utsman.smartmarker.SmartLatLon
import com.utsman.smartmarker.SmartUtil
import com.utsman.smartmarker.googlemaps.moveMarkerSmoothly
import com.utsman.smartmarker.googlemaps.rotateMarker

class SmatMarkerExtensions

fun Context.moveMarkerSmoothlyExt(
    marker: Marker,
    newLatLng: LatLng,
    rotate: Boolean?
): ValueAnimator {
    val animator = moveMarkerSmoothly(marker, newLatLng)
    animator.start()
    val f = SmartUtil.getAngle(
        SmartLatLon(marker.position.latitude, marker.position.longitude),
        SmartLatLon(newLatLng.latitude, newLatLng.longitude)
    ).toFloat()
    rotateMarker(marker, f, rotate)
    return animator
}


