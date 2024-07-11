package com.iyr.ian.utils

class TimeConversionExtensions

val Int.asSeconds: Long
    get() = ((this * 1000).toLong())

val Int.asMinutes: Long
    get() = ((this * 60 * 1000).toLong())
