package com.iyr.ian.utils.markers_utils;

import com.google.android.gms.maps.model.LatLng;

public interface LatLngInterpolatorLot {
    LatLng interpolate(float fraction, LatLng a, LatLng b);
}

