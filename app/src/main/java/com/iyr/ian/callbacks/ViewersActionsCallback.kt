package com.iyr.ian.callbacks

interface ViewersActionsCallback {

    fun onGoingStateChanged(eventKey: String, viewerKey: String, callback: OnCompleteCallback)
    fun onCallAuthorityStateChanged(
        eventKey: String,
        viewerKey: String,
        callback: OnCompleteCallback
    )

    fun selectUserToFollow(userKey: String)

}