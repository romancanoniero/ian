package com.iyr.ian.ui.map.models

import com.iyr.ian.ui.map.enums.CameraModesEnum

class CameraMode {
    var mode: CameraModesEnum? = null
    var additionalKey: String? = null

    constructor()
    constructor(mode: CameraModesEnum?) {
        this.mode = mode
    }

    constructor(mode: CameraModesEnum?, additionalKey: String?) {
        this.mode = mode
        this.additionalKey = additionalKey
    }
}