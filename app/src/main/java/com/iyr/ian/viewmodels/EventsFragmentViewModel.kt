package com.iyr.ian.viewmodels

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventLocation
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationListRepositoryImpl
import com.iyr.ian.services.eventservice.EventService
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class EventsFragmentViewModel private constructor(val context: Context) : ViewModel() {

    private val notificationListRepository: NotificationListRepositoryImpl =
        NotificationListRepositoryImpl()
    private val eventsRepository: EventsRepositoryImpl = EventsRepositoryImpl()
    val contactsGroupsRepository = NotificationListRepositoryImpl()

    private val _event = MutableLiveData<Event>(Event())
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

    private val _showContactGroupSelector =
        MutableLiveData<Boolean?>()
    val showContactGroupSelector: LiveData<Boolean?> =
        _showContactGroupSelector

    private val _postingEventStatus = MutableLiveData<Resource<Event?>>()
    val postingEventStatus: LiveData<Resource<Event?>> = _postingEventStatus


    companion object {
        private lateinit var instance: EventsFragmentViewModel

        @MainThread
        fun getInstance(context: Context? = null): EventsFragmentViewModel {
            instance =
                if (Companion::instance.isInitialized)
                    instance else
                {
                    EventsFragmentViewModel(context!!)
                }
            return instance
        }
    }

    fun initializeEvent(userKey: String) {
        var newEvent = Event()
        newEvent.author_key = userKey
        _event.postValue(newEvent)
    }


    fun getEvent(): Event {
        if (_event.value == null)
            _event.value = Event()

        return _event.value!!
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
//            var notificationList = notificationListRepository.listNotificationList(userKey)
            if ((groupsList.value?: emptyList<ContactGroup>()).size > 0) {
                _showContactGroupSelector.postValue(true)
            }
        }
    }


    suspend fun getNotificationList(userKey: String): Resource<ArrayList<ContactGroup>?> {
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

    fun getViewModel(): EventsFragmentViewModel {
        return this
    }

    fun onNotificationGroupSelected(groupKey: String) {
        event.value?.group_key = groupKey

    }

    fun onPostEventClicked() {
        val notificationsGroupsLists = groupsList.value ?: emptyList()
        if (notificationsGroupsLists.size > 0) {
            _showContactGroupSelector.postValue(true)
        } else {
            // Register the service
            val eventService = EventService.getInstance(context)
            //  event.group_key = "_default"
            onNotificationGroupSelected("_default")
            val event = event.value!!
            eventService.fireEvent(event)
        }
    }

    var groups = ArrayList<ContactGroup>(ArrayList<ContactGroup>())
    private val _groupsList = MutableLiveData<ArrayList<ContactGroup>?>()
    val groupsList: LiveData<ArrayList<ContactGroup>?> = _groupsList
    val contactsGroupsListFlow = liveData<ArrayList<ContactGroup>>(Dispatchers.IO) {
        val userKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
        contactsGroupsRepository.contactsGroupsByUserListFlow(userKey).collect { groups ->

            _groupsList.postValue(groups)
        }
    }


}