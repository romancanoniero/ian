package com.iyr.ian.physical_button

import android.app.Application
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

const val BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED = "BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED"


fun Application.broadcastPanicButtonPressedMessage() {
    val intent: Intent = Intent(BROADCAST_MESSAGE_PANIC_BUTTON_PRESSED)
    intent.setPackage(applicationContext.packageName)
    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
}