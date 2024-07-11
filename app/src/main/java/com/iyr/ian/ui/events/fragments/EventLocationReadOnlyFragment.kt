package com.iyr.ian.ui.events.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import at.markushi.ui.CircleButton
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.model.AddressComponentType
import com.iyr.fewtouchs.utils.osrm.getCurrentLocationAsAddress
import com.iyr.ian.Constants.Companion.AUTOCOMPLETE_REQUEST_CODE
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.TravelMode
import com.iyr.ian.databinding.FragmentEventLocationReadOnlyBinding
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.events.EventsFragmentViewModel
import com.iyr.ian.ui.events.OnPostFragmentInteractionCallback
import com.iyr.ian.utils.GoogleGeoCodingApiUtils
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.geo.GeoFunctions
import com.iyr.ian.utils.geo.models.CustomAddress
import com.iyr.ian.utils.getEventTypeDrawable
import com.iyr.ian.utils.getEventTypeName
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.px
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EventLocationReadOnlyFragment(
    //  var thisEvent: Event,
    val callback: OnPostFragmentInteractionCallback,
    val eventsFragmentViewModel: EventsFragmentViewModel,
    val mainActivityViewModel: MainActivityViewModel
) : Fragment() {
    private lateinit var binding: FragmentEventLocationReadOnlyBinding
    private var locationAsAddress: CustomAddress? = null
    private var lastLocationAquired: LatLng? = null
    private var editButton: CircleButton? = null
    private var mSelectedLocation: CustomAddress? = null
    private var eventTravelMode: String? = null

    private var keyboardWasShownOnce: Boolean = false
    private var invisibleViewBottomDefault: Int? = null

    private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        var keyboardVisible = false
        val r = Rect()
        this.binding.root.getGlobalVisibleRect(r)

        if (this.invisibleViewBottomDefault == null) {
            val transparentViewRect = Rect()
            this.binding?.transparentView?.getGlobalVisibleRect(transparentViewRect)
            this.invisibleViewBottomDefault = this.binding?.transparentView?.bottom
            keyboardWasShownOnce = false
        } else {
            val transparentViewRect = Rect()
            this.binding.transparentView?.getGlobalVisibleRect(transparentViewRect)
            keyboardVisible =
                this.invisibleViewBottomDefault!! >= this.binding.transparentView?.bottom!!
        }
        if (keyboardVisible) {
            this.requireActivity().currentFocus?.let { objeto ->
                val keypadHeight = this.binding.root.height!! - r.bottom
                val scrollY = (objeto.bottom + keypadHeight) - r.bottom
                this.binding?.avatarArea?.layoutParams?.height = 80.px
                this.binding?.avatarArea?.requestLayout()
                this.binding?.scrollView?.smoothScrollTo(0, scrollY)
                this.binding?.confirmButton?.visibility = View.GONE
                keyboardWasShownOnce = true
            }
        } else {
            if (keyboardWasShownOnce == true) {
                this.binding?.confirmButton?.visibility = View.VISIBLE
                this.binding.avatarArea?.layoutParams?.height =
                    this.requireContext().resources.getDimension(R.dimen.box_xsuperbig).toInt()
                this.binding?.avatarArea?.requestLayout()
            }
        }
  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this.eventTravelMode == null) {
            this.eventTravelMode = TravelMode.CAR.name
        }
    }

    private val pair: Pair<EventTypesEnum, Parcelable?>
        get() {
            val eventType =
                EventTypesEnum.valueOf(this.arguments?.getString("event_type").toString())
            val eventLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.arguments?.getParcelable("event_location", EventLocation::class.java)
            } else {
                this.arguments?.getParcelable("event_location")

            }
            return Pair(eventType, eventLocation)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        this.binding =
            FragmentEventLocationReadOnlyBinding.inflate(this.layoutInflater, container, false)

        if (this.eventsFragmentViewModel.fixedLocation.value == null && this.eventsFragmentViewModel.event.value?.event_type != EventTypesEnum.SCORT_ME.name) {


            this.mainActivityViewModel.onLocationSearchStart()
            this.binding.addressReadOnly.isEnabled = false
            this.binding.addressReadOnly.setText("")
            this.binding.addressReadOnly.hint = this.getString(R.string.getting_location)

            this.lifecycleScope.launch(Dispatchers.IO) {
            //    Looper.prepare()
                val address = this@EventLocationReadOnlyFragment.requireActivity()
                    .getCurrentLocationAsAddress()
                when (address) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            this@EventLocationReadOnlyFragment.mainActivityViewModel.onLocationSearchEnd()
                        }

                        requireActivity().showErrorDialog("Error de GeoCoding", address.message.toString())
                    }

                    is Resource.Loading -> {
                        // no hago nada porque no lo estoy escuchando
                    }

                    is Resource.Success -> {
                        val result = address.data
                        withContext(Dispatchers.Main) {
                            this@EventLocationReadOnlyFragment.mainActivityViewModel.onLocationSearchEnd()

                            result?.let {
                                this@EventLocationReadOnlyFragment.binding.addressReadOnly.setText(
                                    it.formated_address.toString()
                                )
                                this@EventLocationReadOnlyFragment.eventsFragmentViewModel.onFixedLocation(
                                    result
                                )
                                this@EventLocationReadOnlyFragment.binding.addressReadOnly.isEnabled =
                                    true
                            }
                        }
                    }
                }
            }
            /*
                if (location != null) {

                    getAddressFromLatLng(
                        com.google.maps.model.LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )
                    mainActivityViewModel.onLocationSearchEnd()
                } else {

                    SmartLocation.with(context).location().oneFix().start { location ->

                        if (location != null) {
                            getAddressFromLatLng(
                                com.google.maps.model.LatLng(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                            mainActivityViewModel.onLocationSearchEnd()
                        } else {
                            mainActivityViewModel.onLocationSearchEnd()
                            mainActivityViewModel.hideLoader()
                            mainActivityViewModel.showError("No se puede obtener su Ubicación")
                            binding.addressReadOnly.isEnabled = true
                        }
                    }
                }

             */
        }
        /*
                binding?.floorFieldApt?.setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        binding?.confirmButton?.visibility = View.GONE
                    }
                    else
                    {
                        binding?.confirmButton?.visibility = View.VISIBLE
                    }
                }
        */
        return this.binding.root
    }

    /***
     * Obtiene la direccion a partir de la latitud y la longitud y actualiza los campos
     */
    private fun getAddressFromLatLng(latLng: com.google.maps.model.LatLng) {
        this.lifecycleScope.launch {
            val response =
                GeoFunctions.getInstance(this@EventLocationReadOnlyFragment.requireContext())
                    .getAddressFromLatLng(latLng.lat, latLng.lng)

            val address = EventLocation()
            with(response.data) {
                address.latitude = this?.latitude!!
                address.longitude = this.longitude
                address.formated_address = this.formatedAddress
                // no existe en nominatim -- location.address_components = nominatimAddress!!.
                val zipCode: String = this.postalCode.toString()
                zipCode?.let { zip ->
                    if (address.formated_address?.startsWith(zip) == true) {
                        address.formated_address =
                            address.formated_address?.replaceFirst(
                                "$zip,", "", true
                            )
                    }
                }
            }

            if (address != null) {
                this@EventLocationReadOnlyFragment.mainActivityViewModel.hideLoader()
                this@EventLocationReadOnlyFragment.eventsFragmentViewModel.onFixedLocation(address)
                this@EventLocationReadOnlyFragment.binding.addressReadOnly.isEnabled = true

                try {
                    this@EventLocationReadOnlyFragment.binding.addressInputLayout.hint =
                        this@EventLocationReadOnlyFragment.getString(R.string.address_to_delibery)
                    this@EventLocationReadOnlyFragment.binding.addressReadOnly.setText(address.formated_address.toString())
                } catch (ex: Exception) {
                    Log.e("GETTING_ADDRESS", ex.message.toString())
                }

            }
        }
    }

    private fun setupObservers() {

        this.eventsFragmentViewModel.eventType.observe(this) { eventTypeName ->

            (AppClass.instance.getCurrentActivity() as MainActivity).setTitleBarTitle(
                this.requireContext().getEventTypeName(
                    eventTypeName!!
                )
            )
            this.binding.avatarImage.setImageDrawable(
                this.requireContext().getEventTypeDrawable(
                    eventTypeName
                )
            )
            when (eventTypeName) {
                EventTypesEnum.SCORT_ME.name -> {
                    this.binding.locationSelectorTitleLine1.setText(R.string.where_do_you_go)
                    this.binding.addressInputLayout.hint =
                        this.requireContext().getString(R.string.hint_introduce_your_destination)
                    this.binding.travelModeSection.visibility = View.VISIBLE
                }

                else -> {
                    this.binding.locationSelectorTitleLine1.setText(R.string.address_to_delibery)
                    //   binding.addressReadOnly.setHint(getString(R.string.hint_introduce_events_place))
                    this.binding.travelModeSection.visibility = View.GONE
                }
            }

        }

        this.eventsFragmentViewModel.fixedLocation.observe(this) { location ->
            if (location != null) {
                this.binding.addressReadOnly.setText(location.formated_address)
            } else {
                this.binding.addressReadOnly.setText("")
            }
            this.binding.addressAptInputLayout.visibility = View.VISIBLE
            this.binding.floorFieldApt.requestFocus()
        }

    }

    private fun cancelObservers() {
        this.eventsFragmentViewModel.eventType.removeObservers(this)
        this.eventsFragmentViewModel.fixedLocation.removeObservers(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //  super.onActivityResult(requestCode, resultCode, data)

        if (resultCode === Activity.RESULT_OK && requestCode === AUTOCOMPLETE_REQUEST_CODE) {
            val place = Autocomplete.getPlaceFromIntent(data)
            this.setCurrentLocationAsAddress(place)
            val location = EventLocation()
            location.latitude = place.latLng.latitude
            location.longitude = place.latLng.longitude
            location.formated_address = place.address
            location.address_components = place.addressComponents
            this.eventsFragmentViewModel.onFixedLocation(location)
            //updateUI()
            this.requireContext().hideKeyboard(this.requireView())
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.setupUI()
    }


    private fun setupUI() {

        var eventType = this.eventsFragmentViewModel.eventType.value


        this.binding.floorFieldApt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // No se usa
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // No se usa
            }

            override fun afterTextChanged(text: Editable?) {
                this@EventLocationReadOnlyFragment.eventsFragmentViewModel.setFloorAndApt(text.toString())
            }
        })


        //     this.binding.addressReadOnly.isEnabled = false
        this.binding.addressReadOnly.isClickable = true
        this.binding.addressReadOnly.isFocusable = false
        this.binding.addressReadOnly.setOnClickListener {
            val fields: List<Place.Field> = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG
            )


            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields
            )
                //.setCountry("NG") //NIGERIA
                .build(this.requireContext())
            this.startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)

        }
        /*
              this.binding.addressReadOnly.onFocusChangeListener =
                  View.OnFocusChangeListener { _, focused ->
                      if (focused) {
                          val fields: List<Place.Field> = listOf(
                              Place.Field.ID,
                              Place.Field.NAME,
                              Place.Field.ADDRESS,
                              Place.Field.ADDRESS_COMPONENTS,
                              Place.Field.LAT_LNG
                          )


                          val intent = Autocomplete.IntentBuilder(
                              AutocompleteActivityMode.FULLSCREEN, fields
                          )
                              //.setCountry("NG") //NIGERIA
                              .build(this.requireContext())
                          this.startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)

                      }
                  }
      */
        if (!Places.isInitialized()) {
            Places.initialize(
                this.requireContext(), this.requireContext().getString(R.string.google_maps_key)
            )
        }



        this.binding.radiogroupTravelMode.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_by_car -> {
                    this.eventTravelMode = TravelMode.CAR.name
                }

                R.id.rb_by_scooter -> {
                    this.eventTravelMode = TravelMode.MOTORCYCLE.name
                }

                R.id.rb_by_bicycle -> {
                    this.eventTravelMode = TravelMode.BICYCLE.name
                }

                R.id.rb_by_walk -> {
                    this.eventTravelMode = TravelMode.WALKING.name
                }
            }
        }

        //--------------------
        this.binding.confirmButton.setOnClickListener {

            val floorApt = this.eventsFragmentViewModel.floorAndApt.value
            if (!floorApt.isNullOrEmpty()) {
                val originalAddress =
                    this.eventsFragmentViewModel.fixedLocation.value?.formated_address
                val streetNumber = (this.mSelectedLocation as CustomAddress).streetNumber
                val insertPosition: Int? = originalAddress?.indexOf(streetNumber.toString())
                    ?.plus(streetNumber.toString().length)

                val newAddress =
                    StringBuilder(originalAddress!!).insert(insertPosition!!, " " + floorApt)
                        .toString()
                //   eventLocation?.formated_address = newAddress
                this.eventsFragmentViewModel.setFormatedAddress(newAddress)
            }

            this.callback.OnSwitchFragmentRequest(
                R.id.event_fragment_aditional_media_selector, this.arguments
            )
        }

        this.binding.editButton.setOnClickListener {
            this.callback.OnSwitchFragmentRequest(
                R.id.event_fragment_location_manual_input,
                this.arguments
            )
        }

//        updateUI()
    }
    /*
        private fun updateUI() {
            var fixedLocation = eventsFragmentViewModel.fixedLocation.value
            var eventType = eventsFragmentViewModel.eventType.value

        }
    */
    /*
        private fun getMyLocation(target: Int) {


            lifecycleScope.launch(Dispatchers.IO) {
                var call = GeoFunctions.getInstance(requireContext()).getAddressFromLatLng(
                    lastLocationAquired!!.latitude, lastLocationAquired!!.longitude
                )


                var pepe = 33
            }

            GeoFunctions.getInstance(requireContext())
                .getAddressFromLatLng(lastLocationAquired!!.latitude,
                    lastLocationAquired!!.longitude,
                    object : OnCompleteCallback {
                        override fun onError(exception: Exception) {
                            requireActivity().showErrorDialog(exception.localizedMessage.toString())
                        }

                        override fun onComplete(success: Boolean, result: Any?) {
                            locationAsAddress = result as CustomAddress?/*
                                            GeoFunctions.getInstance(AppClass.instance)?.getLastKnownLocation {
                                                object : LocationSource.OnLocationChangedListener {
                                                    override fun onLocationChanged(location: Location) {
                                                    }
                                                }
                                            }
                        */
                            activity!!.runOnUiThread {
                                setCurrentLocationAsAddress(locationAsAddress)
                                val location = EventLocation()
                                //               location.locationType = EventLocationTypes.FIXED.name
                                location.latitude = locationAsAddress!!.latitude
                                location.longitude = locationAsAddress!!.longitude
                                location.formated_address = locationAsAddress!!.formatedAddress

    //                        eventLocation = location
                                eventsFragmentViewModel.onFixedLocation(location)
                                when (target) {
                                    R.id.target_location_read_only -> {
                                        binding.addressReadOnly.setText(locationAsAddress!!.formatedAddress.toString())
                                    }
                                }
                                //     updateUI()
                                requireActivity().hideLoader()
                            }


                        }
                    })
        }
    */
//----------- RECEIVERS -------------------------------------------------


    fun setCurrentLocationAsAddress(locationAsAddress: CustomAddress?) {
        this.mSelectedLocation = locationAsAddress
    }

    private fun setCurrentLocationAsAddress(place: Place): CustomAddress {
        val geoCodingUtils = GoogleGeoCodingApiUtils(this.requireContext())
        val customAddres = CustomAddress()
        customAddres.latitude = place.latLng.latitude
        customAddres.longitude = place.latLng.longitude
        customAddres.formatedAddress = place.address
        customAddres.streetNumber = geoCodingUtils.getAddressComponent(
            place.addressComponents, AddressComponentType.STREET_NUMBER
        ).toString()
        customAddres.addressComponentes = place.addressComponents

        this.mSelectedLocation = customAddres

        return customAddres
        var pp = 3

    }

    fun newInstance(
        thisEvent: Event, callback: OnPostFragmentInteractionCallback
    ): EventLocationReadOnlyFragment {
        val fragment =
            EventLocationReadOnlyFragment(
                callback,
                this.eventsFragmentViewModel,
                this.mainActivityViewModel
            )
        val args = Bundle()
        fragment.arguments = args
        return fragment
    }


    override fun onResume() {
        super.onResume()
        this.setupObservers()
        this.binding.root.viewTreeObserver.addOnGlobalLayoutListener(this.onGlobalLayoutListener)


    }

    override fun onPause() {
        super.onPause()
        this.cancelObservers()
        this.binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this.onGlobalLayoutListener)

    }
}