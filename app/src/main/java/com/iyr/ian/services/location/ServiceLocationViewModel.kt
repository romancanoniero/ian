package com.iyr.ian.services.location


import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ServiceLocationViewModel : ViewModel() {
    private val _latestLocation = MutableStateFlow<LatLng?>(null)
    val latestLocation: StateFlow<LatLng?> = _latestLocation

    fun updateLocation(newLocation: LatLng) {
        _latestLocation.value = newLocation
    }
}
