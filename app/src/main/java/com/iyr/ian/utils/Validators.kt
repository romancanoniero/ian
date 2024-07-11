package com.iyr.ian.utils

import android.app.Activity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Patterns
import com.iyr.ian.ui.base.EventCloseToExpireDialogCallback
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.ui.base.PulseValidationCallback
import com.iyr.ian.ui.singletons.EventCloseToExpire
import com.iyr.ian.ui.singletons.PulseValidation

object Validators {
    fun isValidMail(s: String?): Boolean {
        return !TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches()
    }

    fun isValidPassword(s: String, minLenght: Int): Boolean {
        return !TextUtils.isEmpty(s) && s.length >= minLenght
    }

    fun isValidPhoneNumber(s: String): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(s) && s.length >= 12
    }


    fun Activity.requestStatusConfirmationSingleton(
        requestType: PulseRequestTarget,
        callback: PulseValidationCallback?
    ) {

        PulseValidation.getInstance().show(this, requestType, callback)
    }

    fun Activity.showIsCloseToExpireDialogSingleton(
        eventKey: String,
        extras: Bundle?,
        callback: EventCloseToExpireDialogCallback?
    ) {

        EventCloseToExpire.getInstance().show(this, eventKey, extras, callback)
    }


}