package com.iyr.ian.dao.models

import java.io.Serializable
import java.util.Objects

open class GeoLocation : Serializable {
    @JvmField
    var user_key: String = ""
    var l //GeoLocation
            : List<Double> = ArrayList<Double>()
    var g: String? = null
    var event_time: Long? = null


    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        //    if (o == null || getClass() != o.getClass()) return false;
        val that = o as GeoLocation?
        return user_key == that!!.user_key
    }

    override fun hashCode(): Int {
        return Objects.hash(user_key)
    }


}