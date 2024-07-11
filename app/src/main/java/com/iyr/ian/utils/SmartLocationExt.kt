package com.iyr.ian.utils

class SmartLocationExt {}

/**
 * Get current location as address
 */
/*
suspend fun Activity.getCurrentLocationAsAddress(): Resource<EventLocation?> {


    var toReturn: Resource<EventLocation?> =
        Resource.Error<EventLocation?>("Error undefined getting location")
    //   GlobalScope.launch{

    var location: Location? = null
    var letStop = false
    try {



        SmartLocation.with(AppClass.instance).location().oneFix()
            .start { p0 ->
                location = p0
                letStop = true

            }



        //   }
        while (letStop == false) {    // wait for address to be set
            delay(100)
        }

        location?.let { location ->
            var response = GeoFunctions
                .getInstance(this@getCurrentLocationAsAddress)
                .getAddressFromLatLng(location.latitude, location.longitude)

            var address = EventLocation()
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

            toReturn = Resource.Success<EventLocation?>(address) // return address
        } ?: run {
            toReturn = Resource.Error<EventLocation?>("Error getting location")


        }


        // return Resource.Error<EventLocation?>("Error undefined getting location")
    } catch (e: Exception) {
        return Resource.Error<EventLocation?>(e.localizedMessage)
        //  Toast.makeText(AppClass.instance.getCurrentActivity(), "Location requirements error", Toast.LENGTH_SHORT).show()
    }


    return toReturn
}

 */