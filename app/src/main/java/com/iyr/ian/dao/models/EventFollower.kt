package com.iyr.ian.dao.models

class EventFollower() : UserLocation() {

    constructor(userKey: String) : this()
    {
        this.user_key = userKey
    }

    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    var is_author: Boolean = false
    var time: Long? = null
    var battery_level: Double = 0.0

    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    var is_visible = true
    var going_time: Long? = null
    var call_time: Long? = null
    var user_type: String? = null
    var previous_location: HashMap<String, EventLocation>? = null
    val status: String = "USER_OK"

}