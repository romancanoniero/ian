package com.iyr.ian.utils

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Patterns
import com.iyr.ian.R
import java.util.Locale

class StringExtensions


fun Long.toFormattedDuration(): String {
    val minutes = (this / 1000) / 60
    val seconds = (this / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

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


fun String.capitalizeFirstAndLongWords(): String {
    return this.split(" ").mapIndexed { index, word ->
        if (index == 0 || word.length > 3) {
            word.capitalize()
        } else {
            word
        }
    }.joinToString(" ")
}



fun String.isValidMail(): Boolean {
    return !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword( minLenght: Int): Boolean {
    return !TextUtils.isEmpty(this) && this.length >= minLenght
}

fun String.isValidPhoneNumber(): Boolean {
    return PhoneNumberUtils.isGlobalPhoneNumber(this) && this.length >= 12
}
