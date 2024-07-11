package com.iyr.ian.ui.interfaces

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.iyr.ian.ui.signup.phone_contacts.AddContactsFromPhoneActivity
import com.iyr.ian.dao.models.User
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.setup.SetupActivity
import com.iyr.ian.ui.setup.location.LocationRequiredActivity
import com.iyr.ian.ui.setup.pin_setup.PinSetupActivity
import com.iyr.ian.ui.setup.press_or_tap_setup.PressOrTapSetupActivity
import com.iyr.ian.ui.setup.speed_dial_setup.SpeedDialSetupActivity
import com.iyr.ian.utils.areLocationPermissionsGranted

interface IAuthentication {


    fun Activity.onUserAuthenticated(user: User) {

        val isUserCompleted =
            !user.display_name.isNullOrEmpty() && user.image.file_name != null && user.allow_speed_dial != null && user.sos_invocation_count != null && user.sos_invocation_count >= 3 && user.sos_invocation_method != null

        if (isUserCompleted) {
            goToMainScreen(user)
        } else {
            goToCompleteProfile(user)
        }
    }

    private fun Activity.goToCompleteProfile(user: User) {
        val bundle = Bundle()
        bundle.putString("data_object", Gson().toJson(user))
        bundle.putBoolean(
            "first_setup", true
        )
        var intent: Intent? = null
        intent =
            if (user.display_name.isNullOrBlank() || (user.telephone_number.isNullOrEmpty() && user.email_address.isEmpty())) {
                Intent(this, SetupActivity::class.java)
            } else if (user.sos_invocation_count == 0 || user.sos_invocation_method.isNullOrEmpty()) {
                Intent(this, PressOrTapSetupActivity::class.java)
            } else if (user.security_code.isBlank()) {
                Intent(this, PinSetupActivity::class.java)
            } else if (user.allow_speed_dial == null) {
                Intent(this, SpeedDialSetupActivity::class.java)
            } else if (user.sos_invocation_count == null || user.sos_invocation_count < 3) {
                Intent(this, PressOrTapSetupActivity::class.java)
            } else if (!areLocationPermissionsGranted()) {
                Intent(this, LocationRequiredActivity::class.java)
            } else {
                Intent(this, AddContactsFromPhoneActivity::class.java)
            }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun Activity.goToMainScreen(user: User) {
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putString("user", Gson().toJson(user))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

}