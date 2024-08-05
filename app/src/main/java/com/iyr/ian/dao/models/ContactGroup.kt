package com.iyr.ian.dao.models

import com.google.gson.Gson
import java.io.Serializable
import java.util.Locale
import java.util.Objects


class ContactGroup : Serializable {

    var list_key: String = ""
    var list_name: String = ""
    var members: HashMap<String, UserInNotificationList> = HashMap<String, UserInNotificationList>()




    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ContactGroup
        return list_key == that.list_key
    }

    override fun hashCode(): Int {
        return Objects.hash(list_key)
    }

    fun copy(): ContactGroup { //Get another instance of YourClass with the values like this!
        val json = Gson().toJson(this)
        return Gson().fromJson(json, ContactGroup::class.java)
    }

    override fun toString(): String {
        return list_name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } +" - ("+ members.size.toString() +")"
    }
}