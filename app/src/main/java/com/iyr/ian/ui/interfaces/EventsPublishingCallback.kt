package com.iyr.ian.ui.interfaces

import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Event


interface EventsPublishingCallback {
    fun onPublishEvent(event: Event) {}
    fun onPublishEvent(event: Event, callback: OnCompleteCallback?) {}
    fun onPublishEventDone(event: Event?) {}

}
