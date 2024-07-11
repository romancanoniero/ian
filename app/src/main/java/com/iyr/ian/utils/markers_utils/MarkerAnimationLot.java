package com.iyr.ian.utils.markers_utils;

import android.animation.TypeEvaluator;
import android.content.Context;
import android.util.Log;
import android.util.Property;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.iyr.ian.ui.map.helpers.MarkerUpdates;

import java.util.ArrayList;
import java.util.HashMap;

public class MarkerAnimationLot {

    static GoogleMap map;
    ArrayList<LatLng> _trips = new ArrayList<>();
    Marker _marker;
    LatLngInterpolator _latLngInterpolator = new LatLngInterpolator.Spherical();
    private HashMap<String, MarkerUpdates> pendingUpdates;

    public void animateLine(HashMap<String, MarkerUpdates> markersToUpdate, String markerKey, GoogleMap map, Marker marker, Context current, MarkerAnimationCallback callback) {

        pendingUpdates = markersToUpdate;
//
//        _trips.addAll(Trips);
        _marker = marker;
        animateMarker(markerKey, callback);
    }


    public void animateMarker(String markerKey, MarkerAnimationCallback callback) {
        Log.d("MARKER_ANIMATION 1", "animateMarker");


        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return _latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");


        //-----------------------
        ArrayList<LatLng> routePoints = pendingUpdates.get(markerKey).getRoute();

        MarkerAnimation.INSTANCE.animateMarkerToICS(_marker,
                routePoints.get(0),
                new SphericalLot(),
                new MarkerAnimationCallback() {
                    @Override
                    public void onAnimationStart() {

                    }

                    @Override
                    public void onAnimationEnd(@Nullable LatLng lastPosition) {
                        if (routePoints.size() > 1) {
                            callback.onPositionUpdate(routePoints.get(0));
                            routePoints.remove(0);
                            animateMarker(markerKey, callback);
                        } else {
                            callback.onAnimationEnd(lastPosition);
                        }

                    }

                    @Override
                    public void onPositionUpdate(@NonNull LatLng newPosition) {
                        Log.d("MARKER_POSITION", _marker.getTag() + "-" + _marker.getPosition() + " -  La nueva posicion es " + newPosition);
                        // callback.onPositionUpdate(newPosition);
                    }
                }
        );

        //-----------------------


  /*
        ObjectAnimator animator = ObjectAnimator.ofObject(_marker, property, typeEvaluator, _trips.get(0));

        //ObjectAnimator animator = ObjectAnimator.o(view, "alpha", 0.0f);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
                //  animDrawable.stop();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //  animDrawable.stop();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                //  animDrawable.stop();


            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //  animDrawable.stop();
                if (_trips.size() > 1) {
                    callback.onPositionUpdate(_trips.get(0));
                    _trips.remove(0);
                    animateMarker(callback);
                } else {
                    callback.onAnimationEnd();
                }
            }
        });

        animator.setDuration(300);
        animator.start();
*/

    }


}
