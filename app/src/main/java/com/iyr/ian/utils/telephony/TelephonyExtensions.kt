package com.iyr.ian.utils.telephony

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.iyr.ian.Constants.Companion.PHONE_CALL_REQUEST_CODE

class TelephonyExtensions {
}


fun Activity.makePhoneCall(phoneNumber: String) {
    // Verificar si ya se tiene el permiso
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // Solicitar el permiso
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CALL_PHONE),
            PHONE_CALL_REQUEST_CODE
        )
    } else {
        // Realizar la llamada
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
    }
}


