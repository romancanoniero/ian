package com.iyr.ian.dao.models

import java.util.*

class EventAuthor {
    var author_key: String = ""
    var display_name: String = ""
    var profile_image_path: String = ""


    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as EventAuthor
        return author_key == that.author_key
    }

    override fun hashCode(): Int {
        return Objects.hash(author_key)
    }
}