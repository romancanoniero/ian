package com.iyr.ian.dao.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.annotations.NotNull
import com.iyr.ian.utils.support_models.MediaFile


enum class EventLocationType {
    FIXED,
    REALTIME
}

class Event() : Parcelable {

    var group_key: String? = null

    @Exclude
    var author_key: String? = null

    @Exclude
    var event_key: String = ""
    var time: Long = 0
    var expires_at: Long = 0

    var last_update: Long = 0

    @NotNull
    var event_type: String = ""

    @Exclude
    var author: EventAuthor? = null

    @NotNull
    var event_location_type = ""
    var location: EventLocation? = null
    var viewers: HashMap<String, EventFollower>? = null
    var status: String = ""
    var media: ArrayList<MediaFile>? = ArrayList<MediaFile>()
    var location_at_creation: GeoLocation? = null
    var travel_mode: String? = null
    var visibility: String? = null


    constructor(parcel: Parcel) : this() {
        author_key = parcel.readString().toString()
        event_key = parcel.readString().toString()
        time = parcel.readLong()
        last_update = parcel.readLong()
        event_type = parcel.readString().toString()
        status = parcel.readString().toString()
        travel_mode = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(author_key)
        parcel.writeString(event_key)
        parcel.writeLong(time)
        parcel.writeLong(last_update)
        parcel.writeString(event_type)
        parcel.writeString(status)
        parcel.writeString(travel_mode)
    }

    override fun describeContents(): Int {
        return 0
    }


    fun toEventFollowed(): EventFollowed {
        val newObject = EventFollowed()
        newObject.event_key = this.event_key
        newObject.event_type = this.event_type
        newObject.status = this.status
        newObject.visibility = EventVisibilityTypes.VISIBLE.toString()

        val author = EventAuthor()
        author.author_key = this.author!!.author_key
        author.display_name = this.author!!.display_name
        author.profile_image_path = this.author!!.profile_image_path
        newObject.author = author
        return newObject
    }


    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }


    }


}


