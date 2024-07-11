package com.iyr.ian.dao.models

import java.io.Serializable


class UnreadMessages : Serializable {


    var qty: Long = 0
    var chat_room_key: String = ""
    override fun equals(other: Any?): Boolean {

        if (javaClass != other?.javaClass) return false

        other as UnreadMessages

        if (chat_room_key != other.chat_room_key) return false

        return true
    }

    override fun hashCode(): Int {
        return chat_room_key.hashCode()
    }


}