package com.iyr.ian.utils

import com.iyr.ian.utils.support_models.MediaFile


class DataAdaptersSupport {

    sealed class AdapterEventMediaFile {
        data class ItemAdded(val data: MediaFile) :
            AdapterEventMediaFile()
        {
           // val notification = data as EventNotificationModel
        }
        data class ItemChanged(val data: MediaFile):
            AdapterEventMediaFile()

        data class ItemRemoved(val data: MediaFile) :
            AdapterEventMediaFile()

        data class ItemMoved(val data: MediaFile) :
            AdapterEventMediaFile()
    }
}