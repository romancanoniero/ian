package com.iyr.ian.ui.interfaces

import com.iyr.ian.dao.models.Contact


interface FriendsFragmentInterface {

    fun inviteNewFriend(message: String)
    fun inviteExistingUser(record: Contact)
    fun contactRemove(eventKey: Contact)
    fun cancelInvitation(userKey: Contact)
    fun resendInvitation(record: Contact)

}