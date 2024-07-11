package com.iyr.ian.services.eventservice

import com.iyr.ian.dao.models.Event

interface IEventService {
    fun fireEvent(event: Event, notificationListKey: String? = "_default")
}