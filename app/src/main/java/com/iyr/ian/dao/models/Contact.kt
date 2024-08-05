package com.iyr.ian.dao.models

import com.google.firebase.database.Exclude
import com.google.gson.Gson
import com.iyr.ian.utils.support_models.MediaFile
import java.io.Serializable
import java.util.Objects


class Contact : Serializable {


    var notification_key: String = ""

    @Exclude
    var user_key: String? = null
    var author_key: String? = null
    var display_name: String? = null
    var image: MediaFile? = null
    var status: String? = null
    var telephone_number: String? = null
    var email: String? = null
    var have_phone: Boolean? = null
    var add_to_speed_dial: Boolean? = null
    var groups: HashMap<String, ContactGroup>? = null

    @Exclude
    var _search_match_by: String? = null

    @Exclude
    var _search_status: String? = null


    override fun equals(o: Any?): Boolean {
        /*
             if (this === o) return true
             if (o == null || javaClass != o.javaClass) return false
      */
        val that = o as Contact

        return user_key == that.user_key
    }

    override fun hashCode(): Int {
        return Objects.hash(user_key)
    }

    fun copy(): Contact { //Get another instance of YourClass with the values like this!
        val json = Gson().toJson(this)
        return Gson().fromJson(json, Contact::class.java)
    }
}