package com.iyr.ian.services.location

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context

fun Activity.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}


fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun Service.isServiceRunning(): Boolean {

    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (this.javaClass.name == service.service.className) {
            return true
        }
    }
    return false
}


fun Service.isServiceRunningInForeground() : Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (this.javaClass.name == service.service.className) {
            if (service.foreground) {
                return true
            }
        }
    }
    return false
}