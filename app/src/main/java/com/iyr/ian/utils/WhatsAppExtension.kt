package com.iyr.ian.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

class WhatsAppExtension {
}


fun Context.sendWhatsAppMessage( mobileNumber: String, message: String) {

    val encodedMessage = URLEncoder.encode(message, "UTF-8") //"ISO-8859-1"

    val url =
        "https://api.whatsapp.com/send?phone=${mobileNumber}&text=${encodedMessage}"

    val intent = Intent(Intent.ACTION_VIEW).apply {
        this.data = Uri.parse(url)
        this.`package` = "com.whatsapp"
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        this.startActivity(intent)
    } catch (ex: ActivityNotFoundException) {
        Toast.makeText(
            this,
            "Whatsapp not installed",
            Toast.LENGTH_LONG
        ).show()
    }
}
