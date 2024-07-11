package com.iyr.ian.utils

import android.content.Context
import com.google.gson.Gson

class SharedPreferencesExtensions

fun Context.getSharePreferencesMessages(key :String): HashMap<String, NotificationsUtils.PushNotification>? {
    val sharedPref = getSharedPreferences("Messages", Context.MODE_PRIVATE)
    return sharedPref.all as HashMap<String, NotificationsUtils.PushNotification>?
}

fun Context.storeInSharedPreferencesMessages(key :String, value: NotificationsUtils.PushNotification) {
    val sharedPref = getSharedPreferences("Messages", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    editor.putString(key, Gson().toJson(value))
    editor.apply()
}

fun Context.clearPendingNotifications() {
    val sharedPref = getSharedPreferences("Messages", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    editor.clear()
    editor.apply()
}

