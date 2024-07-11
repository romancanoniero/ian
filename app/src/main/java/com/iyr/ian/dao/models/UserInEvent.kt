package com.iyr.ian.dao.models

import java.util.*

class UserInEvent {
    var user_key: String = ""
    var display_name: String = ""
    var profile_image_path: String = ""
    var event_time: Long? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as UserInEvent
        return user_key == that.user_key
    }

    override fun hashCode(): Int {
        return Objects.hash(user_key)
    }
}