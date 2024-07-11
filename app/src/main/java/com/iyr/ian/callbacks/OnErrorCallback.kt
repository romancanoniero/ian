package com.iyr.ian.callbacks

interface OnErrorCallback {
    fun onError(exception: Exception) {}
    fun onError(exception: String) {}

}