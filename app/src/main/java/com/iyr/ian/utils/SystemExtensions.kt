package com.iyr.ian.utils

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.iyr.ian.BuildConfig

class SystemExtensions

fun Context.versionCode(): Int {
    return BuildConfig.VERSION_CODE
}

fun Context.versionPrefix(): Int {
    return BuildConfig.VERSION_NAME.substringBefore(".").toInt()
}


enum class VersionsEnum {
    DISCONNECTED, CONNECTED, FULL_VERSION
}


fun Activity.showSnackBar(root: View, message: String) {
    val snack = Snackbar.make(root, message, Snackbar.LENGTH_LONG)
    snack.show()
}