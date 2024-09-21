package com.iyr.ian.utils

import android.os.Bundle
import androidx.navigation.NavController
import com.iyr.ian.R
import com.iyr.ian.ui.base.PulseRequestTarget

class NavControllerExtensions {
}

fun NavController.navToValidatorDialog(action: PulseRequestTarget, userKey: String, eventKey: String) {
    val bundle = Bundle()
    bundle.putSerializable("action", PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT)
    bundle.putSerializable("user_key", userKey)
    bundle.putSerializable("event_key", eventKey)
    this.navigate(R.id.validatorDialog, bundle)
}
