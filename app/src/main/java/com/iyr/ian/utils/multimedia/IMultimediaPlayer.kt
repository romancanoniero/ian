package com.iyr.ian.utils.multimedia

interface  IMultimediaPlayer {
    fun onBeforePlay() {}
     fun onStart() : Void? = null

    fun onAfterPlay() {

    }

    fun onStop() : Void ? = null

    fun onError(exception: Exception) : Void? = null
}
