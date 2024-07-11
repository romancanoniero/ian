package com.iyr.ian.utils

import android.content.Context
import com.iyr.ian.R
import java.util.Locale

class StringExtensions

fun Context.millisToTimeLocale(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60) % 60)
    val hours = (milliseconds / (1000 * 60 * 60) % 24)
    if (hours > 0) {
        return if (seconds > 0) {
            String.format(getString(R.string.time_with_hms), hours, minutes, seconds)
        } else {
            String.format(getString(R.string.time_with_hm), hours, minutes)
        }
    } else if (minutes > 0) {
        return if (seconds > 0) {
            String.format(getString(R.string.time_with_ms), minutes, seconds)
        } else {
            String.format(getString(R.string.time_with_m), minutes)
        }
    } else if (seconds > 0) {
        return if (seconds > 0) {
            String.format(getString(R.string.time_with_s), seconds)
        } else {
            ""
        }

    }
    return ""
}

fun String.capitalizeWords(): String {
    return this.trim().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }
}