package com.iyr.ian.dao.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.annotations.NotNull


enum class EventVisibilityTypes {
    VISIBLE, // visible para todos incluido para el autor.
    HIDDEN_FOR_AUTHOR,  // visible para los demas pero no para el autor
    PENDING // pendiente para todos...
}


class EventFollowed {

    @NotNull
    var event_key: String = ""

    @NotNull
    var event_type: String = ""

    @NotNull
    var author: EventAuthor = EventAuthor()

    @NotNull
    var status: String = ""

    @NotNull
    var visibility: String? = ""


    @Exclude
    var selected: Boolean = false

    var last_read: Long = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventFollowed

        if (event_key != other.event_key) return false

        return true
    }

    override fun hashCode(): Int {
        return event_key.hashCode()
    }

}