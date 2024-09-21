package com.iyr.ian.ui.map

import android.content.res.ColorStateList
import android.location.Location
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polygon
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.ui.map.enums.CameraModesEnum
import com.iyr.ian.ui.map.models.CameraMode
import com.iyr.ian.utils.drawBounds
import com.iyr.ian.utils.geo.calcularLatitudNegativa
import com.iyr.ian.utils.geo.calcularLongitudNegativa
import com.iyr.ian.utils.getKey
import com.iyr.ian.utils.resizeDrawable
import com.iyr.ian.utils.zoomToBounds
import com.iyr.ian.utils.zoomToFitMarkers
import com.utsman.smartmarker.googlemaps.toLatLngGoogle
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal fun MapSituationFragment.setupCameraModesUI() {
    binding.goToMyPositionButton.imageTintList = ColorStateList.valueOf(
        getColor(
            requireContext(), R.color.colorPrimary
        )
    )
    binding.goToMyPositionButton.setOnClickListener {
        setCameraMode(CameraModesEnum.MY_LOCATION)
    }

    binding.centerInEventLocationButton.imageTintList = ColorStateList.valueOf(
        getColor(
            requireContext(), R.color.colorPrimary
        )
    )
    binding.centerInEventLocationButton.setOnClickListener {
        setCameraMode(CameraModesEnum.CENTER_IN_EVENT_LOCATION)
    }

    binding.zoomToFitButton.imageTintList = ColorStateList.valueOf(
        getColor(
            requireContext(), R.color.colorPrimary
        )
    )
    binding.zoomToFitButton.setOnClickListener {
        setCameraMode(CameraModesEnum.SHOW_ALL_MARKERS)
    }

    val viewersButtonDrawableTop = requireContext().resizeDrawable(
        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_eye_white)!!,
        toolbarIconWitdh,
        toolbarIconHeight
    )
}


internal fun MapSituationFragment.toggleCameraModeSelector(isOpen: Boolean) {
    if (!isOpen) openCameraModeSelector()
    else {
        setCameraMode(currentCameraMode.mode!!, currentCameraMode.additionalKey)
        closeCameraModeSelector()
    }
    isCameraSelectorOpen = !isOpen
}

internal fun MapSituationFragment.closeCameraModeSelector() {
    binding.cameraModeSection.visibility = GONE
}

internal fun MapSituationFragment.openCameraModeSelector() {
    val drawableTopBackArrowIcon = requireContext().resizeDrawable(
        AppCompatResources.getDrawable(
            requireContext(), R.drawable.ic_back_arrow_white
        )!!, toolbarIconWitdh, toolbarIconHeight
    )

    binding.cameraModeButton.setImageDrawable(drawableTopBackArrowIcon)
    binding.selectedCameraModeHint.setText(R.string.go_back)
    binding.cameraModeButton.imageTintList = ColorStateList.valueOf(
        getColor(
            requireContext(), R.color.white
        )
    )
    binding.cameraModeSection.visibility = VISIBLE
}


internal fun MapSituationFragment.setCameraMode(cameraMode: CameraModesEnum) {
    setCameraMode(cameraMode, "")
}

internal fun MapSituationFragment.setCameraMode(mode: CameraModesEnum, additionalKey: String?) {
    userFollowed = null
    currentCameraMode = CameraMode(mode)

    if (mode == CameraModesEnum.FOLLOW_USER) {
        if (currentEvent?.viewers?.containsKey(additionalKey) == true) {
            currentCameraMode = CameraMode(mode, additionalKey)
        } else {
            setCameraMode(CameraModesEnum.SHOW_ALL_MARKERS)
        }

    }

    updateSelectedCameraModeButton(mode, additionalKey)
    closeCameraModeSelector()
    currentCameraMode.let { modeObject ->
        currentCameraMode = modeObject
        updateCameraAccordingMode(currentCameraMode)
    }
}

internal fun MapSituationFragment.updateSelectedCameraModeButton(
    mode: CameraModesEnum, additionalKey: String?
) {
    when (mode) {
        CameraModesEnum.FREE_MODE -> {
            val drawableTop = requireContext().resizeDrawable(
                AppCompatResources.getDrawable(
                    requireContext(), R.drawable.ic_toolbar_camera_free_move
                )!!, toolbarIconWitdh, toolbarIconHeight
            )

            binding.cameraModeButton.setImageDrawable(drawableTop)

            binding.selectedCameraModeHint.setText(R.string.free)
        }

        CameraModesEnum.MY_LOCATION -> {

            val drawableTop = requireContext().resizeDrawable(
                AppCompatResources.getDrawable(
                    requireContext(), R.drawable.ic_toolbar_camera_my_position
                )!!, toolbarIconWitdh, toolbarIconHeight
            )
            binding.cameraModeButton.setImageDrawable(drawableTop)
            binding.selectedCameraModeHint.setText(R.string.to_me)
        }

        CameraModesEnum.CENTER_IN_EVENT_LOCATION -> {
            val drawableTop = requireContext().resizeDrawable(
                AppCompatResources.getDrawable(
                    requireContext(), R.drawable.ic_toolbar_camera_event_centered
                )!!, toolbarIconWitdh, toolbarIconHeight
            )
            binding.cameraModeButton.setImageDrawable(drawableTop)
            binding.selectedCameraModeHint.setText(R.string.center_in_event_location)
        }

        CameraModesEnum.SHOW_ALL_MARKERS -> {
            val drawableTop = requireContext().resizeDrawable(
                AppCompatResources.getDrawable(
                    requireContext(), R.drawable.ic_toolbar_camera_all_in_map
                )!!, toolbarIconWitdh, toolbarIconHeight
            )
            binding.cameraModeButton.setImageDrawable(drawableTop)
            binding.selectedCameraModeHint.setText(R.string.all)
        }

        CameraModesEnum.FOLLOW_USER -> {
        }
    }

    binding.cameraModeButton.imageTintList = ColorStateList.valueOf(
        getColor(
            requireContext(), R.color.white
        )
    )

}


internal fun MapSituationFragment.updateCameraAccordingMode(modeSelected: CameraMode) {

    Log.d("CAMERA_MODE", modeSelected.mode?.name.toString())
    when (modeSelected.mode) {
        CameraModesEnum.FREE_MODE -> {
        }

        CameraModesEnum.MY_LOCATION -> {
            goToMyLocationInMap()
        }

        CameraModesEnum.CENTER_IN_EVENT_LOCATION -> {
            centerInEventLocation()
        }

        CameraModesEnum.SHOW_ALL_MARKERS -> {
            zoomToFitAllMarkers()
        }

        CameraModesEnum.FOLLOW_USER -> {
            centerInUserPosition(modeSelected.additionalKey.toString())
        }

        null -> TODO()
    }
}

var lastPolygon: Polygon? = null
var circles = ArrayList<Circle>()
internal fun MapSituationFragment.centerInEventLocation() {

    lifecycleScope.launch(Dispatchers.Main) {

        var zoomBounds = LatLngBounds.builder()
        circles?.forEach { circle ->
            circle.remove()
        }


        var eventPosition: LatLng? = null
        val myLocation = AppClass.instance.lastLocation.value
        val myPosition: LatLng = LatLng(myLocation!!.latitude, myLocation.longitude)


        currentEvent?.let { event: Event ->
            if (event.event_location_type == EventLocationType.FIXED.name) {
                eventPosition = LatLng(event.location?.latitude!!, event.location?.longitude!!)
            } else {
                viewModel.getFollower(event.author!!.author_key)?.let { follower ->
                    eventPosition = LatLng(follower.l[0], follower.l[1])
                }
            }

            val existingBounds = LatLngBounds.builder()


            // Agrego la posicion del usuario actual porque no se muestra en el mapa
            existingBounds.include(myPosition)


            // Agrego el resto de los markers que no sean el ripple
            markersMap.values.forEach { marker ->
                if (marker?.getKey()?.startsWith("ripple_") == false) {
                    existingBounds.include(marker?.position!!)
                }
            }

            var elementsBounds = existingBounds.build()

            val centerLocation = Location("center").apply {
                latitude = eventPosition!!.latitude
                longitude = eventPosition!!.longitude
            }

            // 1 - calculo el punto horizonal derecho y el punto horizontal izquierdo

            var horDerLat = LatLng(eventPosition!!.latitude, elementsBounds.northeast.longitude)
            val horDerLocation = Location("horDer").apply {
                latitude = eventPosition!!.latitude
                longitude = elementsBounds.northeast.longitude
            }


            var horIzqLat = LatLng(eventPosition!!.latitude, elementsBounds.southwest.longitude)
            val horIzqLocation = Location("horIzq").apply {
                latitude = eventPosition!!.latitude
                longitude = elementsBounds.southwest.longitude
            }

            val farthestLongitude =
                if (centerLocation.distanceTo(horDerLocation) > centerLocation.distanceTo(
                        horIzqLocation
                    )
                ) {
                    horDerLocation.toLatLngGoogle()
                } else {
                    horIzqLocation.toLatLngGoogle()
                }
            val puntoHorizontalNegativo =
                eventPosition?.calcularLongitudNegativa(farthestLongitude)

            // agrego a los bounds su punto original y su punto negativo
            existingBounds.include(farthestLongitude)
            existingBounds.include(puntoHorizontalNegativo!!)


            //--------------------------
            var vertSupLat =
                LatLng(elementsBounds.northeast.latitude, eventPosition!!.longitude)
            val vertSupLocation = Location("vertSup").apply {
                latitude = elementsBounds.northeast.latitude
                longitude = eventPosition!!.longitude
            }
            var vertInfLat =
                LatLng(elementsBounds.southwest.latitude, eventPosition!!.longitude)

            val vertInfLocation = Location("vertInf").apply {
                latitude = elementsBounds.southwest.latitude
                longitude = eventPosition!!.longitude


            }

            val farthestLatitude =
                if (centerLocation.distanceTo(vertSupLocation) > centerLocation.distanceTo(
                        vertInfLocation
                    )
                ) {
                    vertSupLocation.toLatLngGoogle()
                } else {
                    vertInfLocation.toLatLngGoogle()
                }

            val puntoVerticalNegativo = eventPosition?.calcularLatitudNegativa(farthestLatitude)


            // agrego a los bounds su punto original y su punto negativo
            existingBounds.include(farthestLatitude)
            existingBounds.include(puntoVerticalNegativo!!)

            /*
                            circles.add(
                                mMap?.drawControlCircle(
                                    myPosition, getColor(requireContext(), R.color.material_green500), 1000.0
                                )!!
                            )

                            circles.add(mMap?.drawControlCircle(farthestLongitude, R.color.red)!!)
                            circles.add(mMap?.drawControlCircle(puntoHorizontalNegativo!!, R.color.red)!!)
                            circles.add(mMap?.drawControlCircle(farthestLatitude, R.color.blue)!!)
                            circles.add(mMap?.drawControlCircle(puntoVerticalNegativo!!, R.color.blue)!!)
            */
            elementsBounds = existingBounds.build()

            if (lastPolygon != null) {
                lastPolygon?.remove()
            }

            lastPolygon = mMap?.drawBounds(elementsBounds)

            mMap?.zoomToBounds(elementsBounds)

        }
    }
}

internal fun MapSituationFragment.centerInUserPosition(userFollowed: String) {
    val viewer = currentEvent?.viewers?.get(userFollowed)
    if (viewer != null) {
        val userLatLng = LatLng(viewer.l[0], viewer.l[1])
        Log.d("CAMARA", userLatLng.toString())
        val camera = CameraUpdateFactory.newLatLng(userLatLng)
        mMap!!.animateCamera(camera)
    }
}


internal fun MapSituationFragment.goToMyLocationInMap() {

    mMyLocation?.let { latLng ->
        val camera = CameraUpdateFactory.newLatLng(latLng)
        //mMap!!.moveCamera(camera)
        mMap!!.animateCamera(camera)
    }
}

internal fun MapSituationFragment.zoomToFitAllMarkers() {
    val positions = ArrayList<LatLng>()
    mapObjectsMap.values.forEach { objectMap ->
        positions.add(objectMap.latLng)
    }
    // agrego la ubicacion actual.
    lifecycleScope.launch(Dispatchers.IO) {
        SmartLocation.with(context).location().oneFix().start { location ->
            val latLng: LatLng = LatLng(location.latitude, location.longitude)
            positions.add(latLng)

        }
    }
    val mapCanvas = mapView.requireView()

    val mapWidth = mapCanvas.width
    var mapHeight = mapCanvas.height
    mMap?.zoomToFitMarkers(positions, mapWidth, mapWidth, null)
}

