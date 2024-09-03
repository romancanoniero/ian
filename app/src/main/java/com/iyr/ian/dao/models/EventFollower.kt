package com.iyr.ian.dao.models

open class EventFollower() : UserLocation() {

    constructor(userKey: String) : this()
    {
        this.user_key = userKey
    }

    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    var is_author: Boolean = false
    var time: Long? = null
    var battery_percentage: Double = 0.0

    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    var is_visible = true
    var going_time: Long? = null
    var call_time: Long? = null
    var arrival_time: Long? = null
    var user_type: String? = null
    var previous_location: HashMap<String, EventLocation>? = null
    val status: String = "USER_OK"
    var following : Boolean = false
    var on_line : Boolean = false
         var following_start_time: Long? = null
    var following_stop_time: Long? = null

}