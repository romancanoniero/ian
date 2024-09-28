package com.iyr.ian.ui.map.infowindow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.iyr.ian.AppConstants.Companion.PROFILE_IMAGES_STORAGE_PATH
import com.iyr.ian.R
import com.iyr.ian.databinding.DialogInfowindowPopupBinding
import com.iyr.ian.utils.assignFileImageTo
import com.iyr.ian.utils.geo.GeoFunctions
import com.iyr.ian.utils.getBitmapFromVectorDrawable
import com.iyr.ian.utils.isValidPhoneNumber
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomInfoWindowAdapter(
    private val context: Context,
    private val infoWindowData: LiveData<InfoWindowData>,
    private val lifecycleOwner: LifecycleOwner
) : GoogleMap.InfoWindowAdapter {

    private var refLocation: LatLng? = null
    private lateinit var binding: DialogInfowindowPopupBinding
    private var currentMarker: Marker? = null
    private var lastInfoWindowData: InfoWindowData? = null

    init {
        /*
        infoWindowData.observe(lifecycleOwner)
        { data ->
            currentMarker?.let { marker ->
                lastInfoWindowData = data
                updateInfoWindowContent(data)
                marker.showInfoWindow()
            }
        }
*/
    }

    fun setSelectedMarker(marker: Marker?) {
        currentMarker = marker
    }


    override fun getInfoWindow(marker: Marker): View? {
        currentMarker = marker
        binding = DialogInfowindowPopupBinding.inflate(LayoutInflater.from(context))

        var eventKey: String = MapSituationFragmentViewModel.getInstance().auxEventKey.value ?: ""
        var markerKey: String = (marker.tag as Bundle)["key"].toString()

        MapSituationFragmentViewModel.getInstance().onConnectToInfoWindowData(
            eventKey, markerKey
        )
        MapSituationFragmentViewModel.getInstance().infoWindowData.observe(lifecycleOwner)
        { data ->
            lastInfoWindowData = data
            updateInfoWindowContent(data)
            marker.showInfoWindow()
        }
        return binding.root
    }

    override fun getInfoContents(p0: Marker): View? {
        // TODO("Not yet implemented")
        return null
    }

    /*
            override fun getInfoWindow(marker: Marker): View {

                currentMarker = marker
                binding = DialogInfowindowPopupBinding.inflate(LayoutInflater.from(context))
                infoWindowData.value?.let { data ->
                    updateInfoWindowContent(marker, data)
                    //        marker.showInfoWindow()
                }
                return binding.root
            }
        */

    private fun updateInfoWindowContent(data: InfoWindowData) {
        binding.displayName.text = data.display_name
        val filePath = PROFILE_IMAGES_STORAGE_PATH + "/" + data.user_key + "/"

        //  if (binding.profileImage.tag == null || binding.profileImage.tag != data.profile_image_path) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            context.assignFileImageTo(data.profile_image_path, filePath, binding.profileImage)
            // binding.profileImage.tag = data.profile_image_path
        }
        //  }

        val iconSize = context.resources.getDimension(R.dimen.circle_11).toInt()

        if ((data.telephoneNumber ?: "").isValidPhoneNumber()) {
            binding.phoneNumber.text = data.telephoneNumber
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val distanceIcon: Bitmap =
                    context.getBitmapFromVectorDrawable(R.drawable.ic_already_called_small)
                        .scale(iconSize, iconSize)

                withContext(Dispatchers.Main) {
                    binding.distanceToEvent.setCompoundDrawablesWithIntrinsicBounds(
                        distanceIcon.toDrawable(
                            context.resources
                        ), null, null, null
                    )
                    binding.phoneNumber.visibility = View.VISIBLE
                }
            }
        } else {
            binding.phoneNumber.visibility = View.GONE
        }

        val distance = GeoFunctions.getDistanceBetweenTwoPoints(
            refLocation?.latitude ?: 0.0,
            refLocation?.longitude ?: 0.0,
            data.l[0],
            data.l[1]
        )

        val distanceIcon: Bitmap =
            context.getBitmapFromVectorDrawable(R.drawable.map).scale(iconSize, iconSize)
        binding.distanceToEvent.setCompoundDrawablesWithIntrinsicBounds(
            distanceIcon.toDrawable(
                context.resources
            ), null, null, null
        )
        binding.distanceToEvent.text = GeoFunctions.formatDistance(distance)

        binding.distanceToEvent.forceLayout()
    }

    private var imageJob: Job? = null


    fun updateProgressBarColor(progressBar: ProgressBar, progress: Int) {
        val drawable = progressBar.progressDrawable as LayerDrawable
        val progressDrawable = drawable.findDrawableByLayerId(android.R.id.progress) as ClipDrawable

        when {
            progress > 50 -> {
                val colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                progressDrawable.drawable?.colorFilter = colorFilter
            }

            progress in 15..50 -> {
                val ratio = (progress - 15) / 35.0f
                val red = (255 * (1 - ratio)).toInt()
                val yellow = (255 * ratio).toInt()
                val colorFilter =
                    PorterDuffColorFilter(Color.rgb(red, yellow, 0), PorterDuff.Mode.SRC_IN)
                progressDrawable.drawable?.colorFilter = colorFilter
            }

            else -> {
                val colorFilter = PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                progressDrawable.drawable?.colorFilter = colorFilter
            }
        }
        progressBar.progress = progress
    }


    fun updateEventLocation(latLng: LatLng) {
        refLocation = latLng
    }
}