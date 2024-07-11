package com.iyr.ian.ui.interfaces

import com.iyr.ian.ui.map.models.CameraMode


interface MainActivityInterface {

    fun getCameraMode(): CameraMode {
        // TODO
    return CameraMode()
    }
    fun setToolbarTitle(title: String)
    fun isPanicButtonActive(): Boolean {
        return false
    }
}