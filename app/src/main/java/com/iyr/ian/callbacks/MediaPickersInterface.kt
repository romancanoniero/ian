package com.iyr.ian.callbacks

import android.content.Intent

interface MediaPickersInterface {
    fun takePicture() {}
    fun pickImage() {}
    fun recordVideo() {}
    fun onImageSelected(intent: Intent) {}
    fun onVideoRecorded(intent: Intent) {}
}