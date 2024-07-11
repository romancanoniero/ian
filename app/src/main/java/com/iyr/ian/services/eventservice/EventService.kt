package com.iyr.ian.services.eventservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.models.GeoLocation
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getCurrentLocation
import com.iyr.ian.utils.getFileExtension
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class EventService : Service, IEventService {

    private var context: Context? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val eventsRepository: EventsRepositoryImpl = EventsRepositoryImpl()


    companion object {

        private var _instance: EventService? = null

        fun getInstance(applicationContext: Context): EventService {
            if (_instance == null) {
                _instance = EventService()
                _instance!!.context = applicationContext
            }
            return _instance!!
        }
    }

    constructor() {
        // Initialize the database
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun fireEvent(event: Event, notificationListKey: String?) {

        scope.launch {

            flow.postValue(Resource.Loading<Event?>())

            var currentLocationResource = context!!.getCurrentLocation()

            var currentLocation = currentLocationResource.data!!
            val geoLocationAtCreation = GeoLocation()
            geoLocationAtCreation.l = ArrayList<Double>()
            (geoLocationAtCreation.l as ArrayList<Double>).add(currentLocation.latitude)
            (geoLocationAtCreation.l as ArrayList<Double>).add(currentLocation.longitude)
            geoLocationAtCreation.event_time = Date().time
            event.location_at_creation =  geoLocationAtCreation

            if (event.event_location_type.compareTo(EventLocationType.REALTIME.name) == 0) {
                event.location = EventLocation().apply {
                    latitude = event.location?.latitude
                    longitude = event.location?.longitude
                }
            }

            event.media?.forEach { media ->
                if (media.media_type == MediaTypesEnum.VIDEO ||
                    media.media_type == MediaTypesEnum.AUDIO ||
                    media.media_type == MediaTypesEnum.IMAGE
                ) {
                    val fileExtension = media.file_name.getFileExtension(context!!)
                    var fileUri = media.file_name
                    if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" ||
                        fileExtension?.lowercase(Locale.getDefault()) == "png"
                    ) {
                        fileUri = "file:" + media.file_name
                    }
                    var mediaFileEncoded: String? = null
                    if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" ||
                        fileExtension?.lowercase(Locale.getDefault()) == "png" ||
                        fileExtension?.lowercase(Locale.getDefault()) == "mp4" ||
                        fileExtension?.lowercase(Locale.getDefault()) == "3gp"
                    ) {

                        mediaFileEncoded =
                            MultimediaUtils(_instance!!.context!!).convertFileToBase64(
                                Uri.parse(
                                    fileUri
                                )
                            )
                                .toString()
                    }
                    media.bytesB64 = mediaFileEncoded
                }
            }


            var call = eventsRepository.postEvent(event)
            if (call.data != null) {
//                        _postingEventStatus.postValue(Resource.Success<Event?>(call.data))
                flow.postValue(Resource.Success<Event?>(call.data))
            } else
//                        _postingEventStatus.postValue(Resource.Error<Event?>(call.message.toString()))
                flow.postValue(Resource.Error<Event?>(call.message.toString()))
        }


    }

    private val flow = MutableLiveData<Resource<Event?>>()


    fun getResult(): MutableLiveData<Resource<Event?>> = flow
}