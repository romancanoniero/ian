package com.iyr.ian.ui.interfaces

interface ISpeedDialAdapter {
    fun makeAPhoneCall(phoneNumber: String)
    fun sendSMSInvitation(contactName : String, phoneNumber: String)
}