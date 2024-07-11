package com.iyr.ian.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson

class BroadcastingExtensions


fun Context.broadcastMessage(data: Any?, action: String) {
    val intent = Intent(action)
    if (data is Bundle) {
        intent.putExtras(data)
    } else {
        data?.let {
            var dataJson = Gson().toJson(it)
            intent.putExtra("data", dataJson)
        }
    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(
        intent
    )

}
