package com.iyr.ian.callbacks

import com.iyr.ian.dao.models.EventFollowed


interface MainActivityCommonDataInterface {

    fun getEventsFollowed(): ArrayList<EventFollowed> {
        return ArrayList<EventFollowed>()
    }


}