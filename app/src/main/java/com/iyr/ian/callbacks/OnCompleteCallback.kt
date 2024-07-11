package com.iyr.ian.callbacks

interface OnCompleteCallback
{
    fun onComplete(success: Boolean, result: Any?) {}
    fun onError(exception: Exception) {}
}