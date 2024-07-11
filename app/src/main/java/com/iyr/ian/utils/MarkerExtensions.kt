package com.iyr.ian.utils

import android.os.Bundle
import com.google.android.gms.maps.model.Marker

class MarkerExtensions {
}


fun Marker.getKey(): String? {
    if (this.tag == null) {
        this.tag = Bundle()
    }
    var tagMap: Bundle = this.tag as Bundle
    return tagMap.getString("key")
}


fun Marker.isMoving(): Boolean {
    if (this.tag == null) {
        this.tag = Bundle()
    }
    var tagMap: Bundle = this.tag as Bundle
    return (tagMap.getBoolean("is_moving") ?: false) as Boolean
}

fun Marker.turnMovingOn() {
    if (this.tag == null) {
        this.tag = Bundle()
    }
    var tagMap: Bundle = this.tag as Bundle
    tagMap.putBoolean("is_moving", true)
}


fun Marker.turnMovingOff() {
    if (this.tag == null) {
        this.tag = Bundle()
    }
    var tagMap: Bundle = this.tag as Bundle
    tagMap.remove("is_moving")
}