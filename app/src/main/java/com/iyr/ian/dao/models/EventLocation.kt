package com.iyr.ian.dao.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.firebase.database.Exclude
import com.google.firebase.database.annotations.NotNull

/*
public enum class EventLocationTypes {
    FIXED, DYNAMIC
}
*/

class EventLocation() : Parcelable {

    @NotNull
    var locationType: String? = null

    @NotNull
    var latitude: Double? = null

    @NotNull
    var longitude: Double? = null
    var formated_address: String? = null

    @Exclude
    @Transient
    var address_components: AddressComponents? = null
    @Exclude
    var floor_apt: String? = null
    var time: Long? = null

    constructor(parcel: Parcel) : this() {
        locationType = parcel.readString()
        latitude = parcel.readValue(Double::class.java.classLoader) as? Double
        longitude = parcel.readValue(Double::class.java.classLoader) as? Double
        formated_address = parcel.readString()
        address_components = parcel.readParcelable(AddressComponents::class.java.classLoader)
        floor_apt = parcel.readString()
        time = parcel.readValue(Long::class.java.classLoader) as? Long
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(locationType)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeString(formated_address)
        parcel.writeParcelable(address_components, flags)
        parcel.writeString(floor_apt)
        parcel.writeValue(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EventLocation> {
        override fun createFromParcel(parcel: Parcel): EventLocation {
            return EventLocation(parcel)
        }

        override fun newArray(size: Int): Array<EventLocation?> {
            return arrayOfNulls(size)
        }
    }

}