package com.iyr.ian.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateExtensions {
}

fun Long.formatDateTime(): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(date)
}