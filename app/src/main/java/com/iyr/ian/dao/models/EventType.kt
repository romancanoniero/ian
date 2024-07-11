package com.iyr.ian.dao.models

import com.google.firebase.database.annotations.NotNull


class EventType() {

    constructor(
        eventTypeName: String,
        imageResId: Int,
        firsLineResId: Int,
        secondLineResId: Int
    ) : this() {
        this.event_type_key = eventTypeName
        this.imageResId = imageResId
        this.firsLineResId = firsLineResId
        this.secondLineResId = secondLineResId
    }

    @NotNull
    var event_type_key: String = ""

    @NotNull
    var imageResId: Int = -1
    var secondLineResId: Int = -1
    var firsLineResId: Int = -1

}