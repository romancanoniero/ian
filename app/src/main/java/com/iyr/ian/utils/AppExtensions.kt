package com.iyr.ian.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.IANModulesEnum
import com.iyr.ian.ui.MainActivity


enum class ActivityResultsTarget {
    MAIN_FRAGMENT, POSTING_EVENT_FRAGMENT, MAP_FRAGMENT
}

fun AppClass.resultTarget(): ActivityResultsTarget? {

    if (this.getCurrentActivity() is MainActivity) {
        val currentActivity = this.getCurrentActivity() as MainActivity
        when (currentActivity.currentModuleIndex) {
            IANModulesEnum.MAIN.ordinal -> {
                return ActivityResultsTarget.MAIN_FRAGMENT
            }
            IANModulesEnum.POST_EVENTS.ordinal -> {
                return ActivityResultsTarget.POSTING_EVENT_FRAGMENT
            }
            IANModulesEnum.EVENTS_TRACKING.ordinal -> {
                return ActivityResultsTarget.MAP_FRAGMENT
            }
        }
    }
    return null
}

fun Context.getCurrentActivity(): Activity?
{
    return AppClass.instance.getCurrentActivity()
}

/*
fun Activity.showEventRedirectorDialog(eventKey: String) {
    val callbackDialog: OnEventPublishedDone =
        object : OnEventPublishedDone {
            override fun onBringMeToEvent() {
                (AppClass.instance.getCurrentActivity() as MainActivity).goToEvent(eventKey)
            }

            override fun onRefuse() {
            }
        }

    val doneDialog = EventPublishedDoneDialog(
        this,
        this,
        callbackDialog
    )

    doneDialog.show()

}
*/

fun Context.getEventTypeName(
    eventType: String
): String {
    when (eventType) {


        EventTypesEnum.SEND_POLICE.name -> {
            return getString(R.string.call_police_request)
        }

        EventTypesEnum.SEND_FIREMAN.name -> {
            return getString(R.string.call_fireman_request)
        }

        EventTypesEnum.ROBBER_ALERT.name -> {
            return getString(R.string.suspicius_activity_notification)
        }

        EventTypesEnum.PERSECUTION.name -> {
            return getString(R.string.persecution_notification)
        }

        EventTypesEnum.SCORT_ME.name -> {
            return getString(R.string.scorting_request)
        }

        EventTypesEnum.SEND_AMBULANCE.name -> {
            return getString(R.string.call_ambulance_request)
        }

        EventTypesEnum.PET_LOST.name -> {
            return getString(R.string.pet_lost_notification)
        }

        EventTypesEnum.KID_LOST.name -> {
            return getString(R.string.kid_lost_notification)
        }

        EventTypesEnum.PANIC_BUTTON.name -> {
            return getString(R.string.panic_button_pressed)
        }

        EventTypesEnum.FALLING_ALARM.name -> {
            return getString(R.string.falling_alarm_fired)
        }

    }
    return ""
}


fun Context.getEventTypeDrawable(
    eventType: String
): Drawable? {
    when (eventType) {
        EventTypesEnum.SEND_POLICE.name -> {
            return getDrawable(R.drawable.poli)
        }

        EventTypesEnum.SEND_FIREMAN.name -> {
            return getDrawable(R.drawable.fireman_big)
        }

        EventTypesEnum.ROBBER_ALERT.name -> {
            return getDrawable(R.drawable.suspicius_big)
        }

        EventTypesEnum.PERSECUTION.name -> {
            return getDrawable(R.drawable.persecution_big)
        }

        EventTypesEnum.SCORT_ME.name -> {
            return getDrawable(R.drawable.scortme_big)
        }

        EventTypesEnum.SEND_AMBULANCE.name -> {
            return getDrawable(R.drawable.ambulance_big)
        }

        EventTypesEnum.PET_LOST.name -> {
            return getDrawable(R.drawable.pet_lost_big)
        }

        EventTypesEnum.KID_LOST.name -> {
            return getDrawable(R.drawable.kid_lot_big)
        }

        EventTypesEnum.PANIC_BUTTON.name -> {
            return getDrawable(R.drawable.sos_big)
        }

        EventTypesEnum.FALLING_ALARM.name -> {
            return getDrawable(R.drawable.ic_falling)
        }

    }
    return null
}


