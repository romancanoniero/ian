package com.iyr.ian.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.models.NotificationList
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationListRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class EventsFragmentViewModel : ViewModel() {

    private val notificationListRepository: NotificationListRepositoryImpl =
        NotificationListRepositoryImpl()
    private val eventsRepository: EventsRepositoryImpl = EventsRepositoryImpl()


    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    private val _eventType = MutableLiveData<String?>()
    val eventType: LiveData<String?> = _eventType

    private val _eventLocationType = MutableLiveData<String?>()
    val eventLocationType: LiveData<String?> = _eventLocationType

    private val _fixedLocation = MutableLiveData<EventLocation?>()
    val fixedLocation: LiveData<EventLocation?> = _fixedLocation

    private val _mediaContents = ArrayList<MediaFile>()
    private val _eventMediaFlow = MutableLiveData<ArrayList<MediaFile>>()
    val eventMediaFlow: LiveData<ArrayList<MediaFile>> = _eventMediaFlow

    private val _clearMediaAdapterContent = MutableLiveData<Boolean>()
    val clearMediaAdapterContent: LiveData<Boolean> = _clearMediaAdapterContent

    private val _formattedAddress = MutableLiveData<String?>()
    val formattedAddress: LiveData<String?> = _formattedAddress

    private val _floorAndApt = MutableLiveData<String?>()
    val floorAndApt: LiveData<String?> = _floorAndApt

    private val _showNotificationListSelector =
        MutableLiveData<Resource<ArrayList<NotificationList>?>>()
    val showNotificationListSelector: LiveData<Resource<ArrayList<NotificationList>?>> =
        _showNotificationListSelector

    private val _postingEventStatus = MutableLiveData<Resource<Event?>>()
    val postingEventStatus: LiveData<Resource<Event?>> = _postingEventStatus

    fun initializeEvent(userKey: String) {
        var newEvent = Event()
        newEvent.author_key = userKey
        _event.postValue(newEvent)
    }

    fun setEvent(createEvent: Event) {

    }

    fun setEventType(eventType: String) {
        _event.value?.event_type = eventType
        _eventType.value = eventType
    }


    fun onFixedLocation(location: EventLocation?) {
        _event.value?.location = location
        _fixedLocation.postValue(location)
        _formattedAddress.postValue(location?.formated_address)
    }

    fun onClearLocationRequest() {
        _event.value?.location = null
        _fixedLocation.postValue(null)
        _formattedAddress.postValue("")
    }


    fun setFloorAndApt(additionalText: String) {
        _event.value?.location?.floor_apt = additionalText
        _floorAndApt.postValue(additionalText)

    }

    fun setFormatedAddress(address: String) {
        _event.value?.location?.formated_address = address
        _formattedAddress.postValue(address)
    }

    fun setEventLocationMode(type: EventLocationType) {
        _event.value?.event_location_type = type.toString()
        _eventLocationType.value = type.toString()
    }

    fun onImageAdded(filePath: String) {
        val mediaFile = MediaFile()
        mediaFile.media_type = MediaTypesEnum.IMAGE
        mediaFile.file_name = filePath
        mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
        mediaFile.time = Date().time
        mediaFile.duration = 0
        if (!_mediaContents.contains(mediaFile)) {
            _mediaContents.add(mediaFile)
            _eventMediaFlow.postValue(_mediaContents)
            event.value?.media = _mediaContents
        }
    }

    fun onVideoAdded(filePath: String, duration: Int) {
        val mediaFile = MediaFile()
        mediaFile.media_type = MediaTypesEnum.VIDEO
        mediaFile.file_name = filePath
        mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
        mediaFile.time = Date().time
        if (!_mediaContents.contains(mediaFile)) {
            _mediaContents.add(mediaFile)
            _eventMediaFlow.postValue(_mediaContents)
            event.value?.media = _mediaContents
        }
    }

    fun onTextAdded(text: String) {
        val mediaFile = MediaFile()
        mediaFile.media_type = MediaTypesEnum.TEXT
        mediaFile.text = text
        mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
        mediaFile.time = Date().time
        mediaFile.duration = 0
        if (!_mediaContents.contains(mediaFile)) {
            _mediaContents.add(mediaFile)
            _eventMediaFlow.postValue(_mediaContents)
            event.value?.media = _mediaContents
        }
    }

    fun onAudioAdded(recordingFilename: String, duration: Int) {
        val mediaFile = MediaFile()
        mediaFile.media_type = MediaTypesEnum.AUDIO
        mediaFile.file_name = recordingFilename
        mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
        mediaFile.time = Date().time
        mediaFile.duration = duration
        if (!_mediaContents.contains(mediaFile)) {
            _mediaContents.add(mediaFile)
            _eventMediaFlow.postValue(_mediaContents)
            event.value?.media = _mediaContents
        }
    }

    fun removeMedia(mediaFile: MediaFile) {
        _mediaContents.remove(mediaFile)
        _eventMediaFlow.postValue(_mediaContents)
    }

    fun prepareToPublish(userKey: String) {
       viewModelScope.launch(Dispatchers.IO)
        {
            var notificationList = notificationListRepository.listNotificationList(userKey)
            _showNotificationListSelector.postValue(notificationList)
        }
    }


    suspend fun getNotificationList(userKey: String) : Resource<ArrayList<NotificationList>?> {
        return notificationListRepository.listNotificationList(userKey)
    }
/*
    fun publishEvent(event: Event) {
        _postingEventStatus.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO)
        {

            event.media?.forEach { media ->
                if (media.media_type == MediaTypesEnum.VIDEO ||
                    media.media_type == MediaTypesEnum.AUDIO ||
                    media.media_type == MediaTypesEnum.IMAGE
                ) {
                    val fileExtension = media.file_name.getFileExtension(con)
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
                            MultimediaUtils(AppClass.instance).convertFileToBase64(Uri.parse(fileUri))
                                .toString()
                    }
                    media.bytesB64 = mediaFileEncoded
                }
            }


            var call = eventsRepository.postEvent(event)
            if (call.data != null) {
                _postingEventStatus.postValue(Resource.Success<Event?>(call.data))

            } else
                _postingEventStatus.postValue(Resource.Error<Event?>(call.message.toString()))


            /*
                        EventsWSClient.instance.postEvent(event, object : OnCompleteCallback {
                            override fun onComplete(success: Boolean, result: Any?) {
                                if (success) {
                                    if (result is Event) {
                                        callback.onPublishEventDone(result)
                                        iCallback?.onComplete(true, result)
                                    }
                                }
                            }

                            override fun onError(exception: java.lang.Exception) {
                                callback.onError(exception)
                                iCallback?.onError(exception)
                            }
                        })
              */
        }

    }
*/
    fun clearMedia() {
        _mediaContents.clear()
        _eventMediaFlow.postValue(_mediaContents)
        event.value?.media = _mediaContents
    }


}