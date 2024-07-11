package com.iyr.ian.ui.callback

import com.google.firebase.database.DataSnapshot
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventFollower


interface MainActivityCallback  {
    fun onError(exception: Exception)
/*
    fun onNotificationAdded(snapshot: DataSnapshot, previousChildName: String?)
    fun onNotificationChanged(snapshot: DataSnapshot, previousChildName: String?)
    fun onNotificationRemoved(snapshot: DataSnapshot)

 */
    fun goToEvent(eventKey: String)
    fun onNoEventsToShow()
    fun onViewerAdded(eventFollower: EventFollower)
    fun onViewerChanged(eventFollower: EventFollower)
    fun onViewerRemoved(eventFollower: EventFollower)
    fun onViwerMoved(eventFollower: EventFollower)
    fun onPublishEventDone(event: Event?)
    fun onEventCloseDone(eventKey: String)
    fun onLeaveEventRequestDone(eventKey: String)
    fun onNotGoingToHelpRequestDone()
    fun updateUI()
    fun isFollingEvent(eventKey: String): Boolean
    fun isUserInHidenPanic(): Boolean
    fun onEventFollowedAdded(eventFull: Event, eventFollowed: EventFollowed)
    fun onEventFollowedChanged(eventFull: Event, eventFollowed: EventFollowed)
    fun onEventFollowedRemoved(eventKey: String)
    fun onContactListAdded(snapshot: DataSnapshot, previousChildName: String?)
    fun onContactListChanged(snapshot: DataSnapshot, previousChildName: String?)
    fun onContactListRemoved(snapshot: DataSnapshot)
    fun onAgreeToAssistDone(eventKey: String)

}
