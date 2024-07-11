package com.iyr.ian.utils.markers_utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.util.Property
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.iyr.ian.app.AppClass
import com.iyr.ian.utils.MapsApiUtils
import com.iyr.ian.utils.animateView
import kotlin.math.abs
import kotlin.math.atan


interface MarkerAnimationCallback {
    fun onAnimationStart()
    fun onAnimationEnd(lastPosition: LatLng?)
    fun onPositionUpdate(newPosition: LatLng)
}


object MarkerAnimation {
    fun animateMarkerToGB(
        marker: Marker,
        finalPosition: LatLng?,
        latLngInterpolator: LatLngInterpolator
    ) {
        val startPosition = marker.position
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = 3000f
        handler.post(object : Runnable {
            var elapsed: Long = 0
            var t = 0f
            var v = 0f
            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)
                marker.position = latLngInterpolator.interpolate(v, startPosition, finalPosition)

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun animateMarkerToHC(
        marker: Marker,
        finalPosition: LatLng?,
        latLngInterpolator: LatLngInterpolator
    ) {
        val startPosition = marker.position
        val valueAnimator = ValueAnimator()
        valueAnimator.addUpdateListener { animation ->
            val v = animation.animatedFraction
            val newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition)
            marker.position = newPosition
        }
        valueAnimator.setFloatValues(0f, 1f) // Ignored.
        valueAnimator.duration = 3000
        valueAnimator.start()
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun animateMarkerToICS(
        marker: Marker,
        finalPosition: LatLng?,
        latLngInterpolator: LatLngInterpolator
    ) {
        //      Log.d("MARKER_ANIMATION", "Muevo a "+marker.tag.toString()+ " desde "+marker.position.toString() + " a "+finalPosition.toString())

        animateMarkerToICS(marker, finalPosition, latLngInterpolator, null)
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun animateMarkerToICS(
        marker: Marker,
        finalPosition: LatLng?,
        latLngInterpolator: LatLngInterpolator,
        callback: MarkerAnimationCallback?
    ) {
        Log.d(
            "MARKER_ANIMATION",
            "Muevo a " + marker.tag.toString() + " desde " + marker.position.toString() + " a " + finalPosition.toString()
        )


        val typeEvaluator = TypeEvaluator<LatLng> { fraction, startValue, endValue ->
            latLngInterpolator.interpolate(
                fraction,
                startValue,
                endValue
            )
        }

        val property = Property.of(
            Marker::class.java, LatLng::class.java, "position"
        )
        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = 800
        callback?.let { _callback ->
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                    _callback.onAnimationStart()
                }

                override fun onAnimationEnd(p0: Animator) {

                    _callback.onAnimationEnd(finalPosition)
                }

                override fun onAnimationCancel(p0: Animator) {
                    _callback.onAnimationEnd(finalPosition)
                }

                override fun onAnimationRepeat(p0: Animator) {
                    _callback.onAnimationStart()
                }
            })

        }
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val v: Float = animation.animatedFraction
                val newPosition = latLngInterpolator.interpolate(
                    v,
                    marker.position,
                    finalPosition
                )
                marker.position = newPosition
                callback?.onPositionUpdate(newPosition)
                // Log.d("MARKER_ANIMATION", newPosition.toString())
            }
        })
        animator.start()
    }


    fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = abs(begin.latitude - end.latitude)
        val lng = abs(begin.longitude - end.longitude)
        if (begin.latitude < end.latitude && begin.longitude < end.longitude) return Math.toDegrees(
            atan(lng / lat)
        )
            .toFloat() else if (begin.latitude >= end.latitude && begin.longitude < end.longitude) return (90 - Math.toDegrees(
            atan(lng / lat)
        ) + 90).toFloat() else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude) return (Math.toDegrees(
            atan(lng / lat)
        ) + 180).toFloat() else if (begin.latitude < end.latitude && begin.longitude >= end.longitude) return (90 - Math.toDegrees(
            atan(lng / lat)
        ) + 270).toFloat()
        return (-1).toFloat()
    }


    fun updateCarLoc(
        map: GoogleMap,
        movingMarker: Marker,
        ll: com.google.maps.model.LatLng
    ) {
        var currentLL: com.google.maps.model.LatLng? = ll
        val property = Property.of(
            Marker::class.java, LatLng::class.java, "position"
        )
        var previousLL: com.google.maps.model.LatLng? = com.google.maps.model.LatLng(
            property.get(movingMarker).latitude,
            property.get(movingMarker).longitude
        )
        if (previousLL == null) {
            val currentLL = ll
            previousLL = currentLL
            movingMarker.position = LatLng(currentLL.lat, currentLL.lng)
            movingMarker.setAnchor(0.5f, 0.5f)
            // ver que hacemos con esto  animateView(currentLL!!)
        } else {
            previousLL = currentLL
            currentLL = ll


            val valueAnimator = MapsApiUtils().getInstance(AppClass.instance).carAnimator()
            //    val valueAnimator = MapsApiUtils().getInstance(AppClass.instance).getCarRotation(previousLL!!,currentLL!!)
            valueAnimator.addUpdateListener { va ->
                if (currentLL != null && previousLL != null) {
                    val multiplier = va.animatedFraction
                    val nxtLoc = com.google.maps.model.LatLng(
                        multiplier * currentLL.lat + (1 - multiplier) * previousLL.lat,
                        multiplier * currentLL.lng + (1 - multiplier) * previousLL.lng
                    )
                    movingMarker.position = LatLng(nxtLoc.lat, nxtLoc.lng)
                    val rotation = MapsApiUtils().getInstance(AppClass.instance)
                        .getCarRotation(previousLL, nxtLoc)
                    if (!rotation.isNaN()) {
                        movingMarker.rotation = rotation
                    }
                    movingMarker.setAnchor(0.5f, 0.5f)
                    map.animateView(
                        LatLng(
                            nxtLoc.lat,
                            nxtLoc.lng
                        )
                    )
                }
            }
            valueAnimator.start()
        }
    }
}