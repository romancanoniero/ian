package com.iyr.ian.utils

import android.app.Activity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil

class GooglePlayExtensions

fun Activity.isGooglePlayInstalled(): Boolean {
    val status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
    if (status == ConnectionResult.SUCCESS) {
        return true
    } else {
        GooglePlayServicesUtil.getErrorDialog(status, this, 10)!!.show()
    }
    return false
}