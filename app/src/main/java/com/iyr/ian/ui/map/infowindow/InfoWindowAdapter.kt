package com.iyr.ian.ui.map.infowindow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.iyr.ian.AppConstants.Companion.PROFILE_IMAGES_STORAGE_PATH
import com.iyr.ian.databinding.DialogInfowindowPopupBinding
import com.iyr.ian.utils.assignFileImageTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class InfoWindowAdapter(
    private val context: Context,
    private val infoWindowData: LiveData<InfoWindowData>,
    private val lifecycleOwner: LifecycleOwner
) : GoogleMap.InfoWindowAdapter {

    private var isVisible = false
    private lateinit var binding: DialogInfowindowPopupBinding
    private var currentMarker: Marker? = null

    init {
        infoWindowData.observe(lifecycleOwner) {
            currentMarker?.let { marker: Marker ->
                /*
                GlobalScope.launch(Dispatchers.Main)
                {
                     */
                if (!isVisible) {
                    isVisible = true
                    marker.showInfoWindow()
                } else {
                    getInfoWindow(marker)

                }

                //}

            }

        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        currentMarker = marker
        // Inflar el layout personalizado basado en el tipo de marcador
        binding = DialogInfowindowPopupBinding.inflate(LayoutInflater.from(context))

        infoWindowData.value?.let { data ->

            updateInfoWindowContent(marker, data)
            /*
            binding.displayName.text = data.display_name
            binding.phoneNumber.text = data.telephoneNumber
            val filePath = PROFILE_IMAGES_STORAGE_PATH + "/" + data.user_key + "/"
            context.assignFileImageTo(
                data.profile_image_path,
                filePath.toString(),
                binding.profileImage
            )
            binding.distanceToEvent.text = data.l[0].toString() + data.l[1].toString()

        }

        // Personalizar el layout según el tipo de marcador
        when (marker.tag) {
            "type1" -> {
                // Personalizar para type1
            }

            "type2" -> {
                // Personalizar para type2
            }
            // Agregar más tipos según sea necesario
        }
*/
        }
        return binding.root
    }


    fun updateInfoWindowContent(marker: Marker, data: InfoWindowData) {
        binding.displayName.text = data.display_name
        binding.phoneNumber.text = data.telephoneNumber
        val filePath = PROFILE_IMAGES_STORAGE_PATH + "/" + data.user_key + "/"

        GlobalScope.launch(Dispatchers.IO) {
            context.assignFileImageTo(
                data.profile_image_path,
                filePath,
                binding.profileImage
            )
        }
        binding.distanceToEvent.text = data.l[0].toString() + data.l[1].toString()

        // Personalizar el layout según el tipo de marcador
        when (marker.tag) {
            "type1" -> {
                // Personalizar para type1
            }

            "type2" -> {
                // Personalizar para type2
            }
            // Agregar más tipos según sea necesario
        }
    }

    override fun getInfoContents(marker: Marker): View? {
        // Similar a getInfoWindow, pero se puede personalizar aún más si es necesario
        return null
    }
}