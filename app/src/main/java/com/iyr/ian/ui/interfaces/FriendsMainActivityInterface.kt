package com.iyr.ian.ui.interfaces

import com.iyr.ian.dao.models.Contact


interface FriendsMainActivityInterface {

    fun contactRefuse(eventKey: Contact, notificationKey: String)
}