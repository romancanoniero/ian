package com.iyr.ian.dao.models

import java.io.Serializable

enum class EventNotificationType {
    NOTIFICATION_TYPE_USER_IN_TROUBLE,
    NOTIFICATION_TYPE_USER_STATUS_OK,
    CONTACT_REQUEST,
    NOTIFICATION_TYPE_EVENT_NOTIFICATION,
    SCORTING_REQUEST,
    EVENT_CLOSED_OK,
    EVENT_LEAVED,
    PULSE_VERIFICATION_FAILED,
    PANIC_BUTTON,
    NOTIFICATION_TYPE_EVENT_STATUS_CLOSED_OK,
    NOTIFICATION_TYPE_SEND_POLICE,
    NOTIFICATION_TYPE_SEND_FIREMAN,
    NOTIFICATION_TYPE_ROBBER_ALERT,
    NOTIFICATION_TYPE_PERSECUTION,
    NOTIFICATION_TYPE_SCORT_ME,
    NOTIFICATION_TYPE_SEND_AMBULANCE,
    NOTIFICATION_TYPE_KID_LOST,
    NOTIFICATION_TYPE_PET_LOST,
    NOTIFICATION_TYPE_PANIC_BUTTON,
    NOTIFICATION_TYPE_FALLING_USER,
    NOTIFICATION_TYPE_NOT_RESPONSE,
    NOTIFICATION_TYPE_NOTICE_CLOSE_TO_EXPIRE,
    NOTIFICATION_TYPE_ADDED_TO_SPEED_DIAL,
    NOTIFICATION_TYPE_MESSAGE
}


enum class EventStatusEnum {
    DELIVERED,
    RECEIVED,
    READ
}


class EventInfo : Serializable {
    var event_key = null
    var event_type = null
    var user_key = null
    var user_name = null
    var image_file_name = null

}

class EventNotificationModel {

    var notification_key: String = ""
    var event_key: String = ""
    var sub_key: String = ""
    var event_type: String = ""
    var notification_type: String = ""
    var time: Long = 0
    var event_data: HashMap<String, Any>? = null
    var event_info: HashMap<String, Any>? = null
    var qty: Int? = null
    var status: String = ""
    var read: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventNotificationModel

        if (notification_key != other.notification_key) return false

        return true
    }

    override fun hashCode(): Int {
        return event_key.hashCode()
    }


}