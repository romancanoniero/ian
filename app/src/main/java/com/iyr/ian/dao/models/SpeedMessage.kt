package com.iyr.ian.dao.models

import android.os.Parcel
import android.os.Parcelable


enum class SpeedMessageActions {
    GOING,
    NOT_GOING,
    CALLED,
    NOT_CALLED,
    IM_THERE,
    NOT_IN_THERE
}


class SpeedMessage() : Parcelable {
    var messageTag: String = ""
    var actionType: String? = null
    var actionTitleResId: Int = 0
    var actionMessageResId: Int = 0
    var revertActionType: String? = null
    var revertActionTitleResId: Int = 0
    var revertActionMessageResId: Int = 0

    constructor(parcel: Parcel) : this() {
        messageTag = parcel.readString().toString()

        actionType = parcel.readString()
        actionTitleResId = parcel.readInt()
        actionMessageResId = parcel.readInt()

        revertActionType = parcel.readString()
        revertActionTitleResId = parcel.readInt()
        revertActionMessageResId = parcel.readInt()

    }

    /*
        constructor(messageTag: String, messageResId: Int) : this() {
            this.messageTag = messageTag
            this.messageResId = messageResId
        }

        constructor(messageTag: String, messageResId: Int, actionType: SpeedMessageActions) : this() {
            this.messageTag = messageTag
            this.messageResId = messageResId
            this.actionType = actionType.toString()
        }
    */

    constructor(
        messageTag: String,
        actiontype: SpeedMessageActions,
        actionTitleResId: Int,
        actionMessageResId: Int,

    ) : this() {
        this.messageTag = messageTag
        this.actionType = actiontype.toString()
        this.actionTitleResId = actionTitleResId
        this.actionMessageResId = actionMessageResId

        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpeedMessage

        return messageTag == other.messageTag
    }

    override fun hashCode(): Int {
        return messageTag.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<SpeedMessage> {
        override fun createFromParcel(parcel: Parcel): SpeedMessage {
            return SpeedMessage(parcel)
        }

        override fun newArray(size: Int): Array<SpeedMessage?> {
            return arrayOfNulls(size)
        }
    }


}