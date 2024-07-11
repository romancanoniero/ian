package com.iyr.ian.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.AppConstants.Companion.NOTIFICATION_TYPE_NEW_MESSAGE
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.dao.models.UnreadMessages
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.NotificationsRepository
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.enums.DialogsEnum
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.IANModulesEnum
import com.iyr.ian.enums.RecordingStatusEnum
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ChatRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ContactsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SubscriptionTypeRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.ui.base.PulseValidationRequest
import com.iyr.ian.ui.settings.SettingsFragmentsEnum
import com.iyr.ian.utils.NotificationsUtils
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getFileExtension
import com.iyr.ian.utils.models.DialogToShowModel
import com.iyr.ian.utils.models.ViewAttributes
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModel(val context: Context, val userKey: String) : ViewModel() {

    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()
    private var eventsRepository: EventsRepositoryImpl = EventsRepositoryImpl()
    private var eventRepository: EventRepositoryImpl = EventRepositoryImpl()
    private var notificationsRepository: NotificationsRepositoryImpl = NotificationsRepositoryImpl()
    private var chatRepository: ChatRepositoryImpl = ChatRepositoryImpl()
    private var contactsRepository: ContactsRepositoryImpl = ContactsRepositoryImpl()

    private val storageRepositoryImpl: StorageRepositoryImpl = StorageRepositoryImpl()
    private val subscriptionsRepository: SubscriptionTypeRepositoryImpl =
        SubscriptionTypeRepositoryImpl()

    private lateinit var userFlowJob: Job


    private val _bluetoothStatus = MutableLiveData<Boolean>()
    val bluetoothStatus: LiveData<Boolean> = _bluetoothStatus

    private val _isLocationAvailable = MutableLiveData<Boolean>()
    val isLocationAvailable: LiveData<Boolean> = _isLocationAvailable

    var _recordFile: String? = null

    private val _newMedia = MutableLiveData<MediaFile?>()
    val newMedia: LiveData<MediaFile?> = _newMedia


    private val _bottomBarVisibilityStatus = MutableLiveData<Boolean?>()
    val bottomBarVisibilityStatus: LiveData<Boolean?> = _bottomBarVisibilityStatus

    private val _isKeyboardOpen = MutableLiveData<Boolean?>(null)
    val isKeyboardOpen: LiveData<Boolean?> = _isKeyboardOpen


    private val _recordingStatus = MutableLiveData<RecordingStatusEnum>()
    val recordingStatus: LiveData<RecordingStatusEnum> = _recordingStatus

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _titleBarCardViewVisible = MutableLiveData<Boolean>(true)
    val titleBarCardViewVisible: LiveData<Boolean> = _titleBarCardViewVisible


    private val _userSubscription = MutableLiveData<SubscriptionTypes?>()
    val userSubscription: LiveData<SubscriptionTypes?> = _userSubscription

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _initializationReady = MutableLiveData<Boolean>()
    val initializationReady: LiveData<Boolean> = _initializationReady

    private val _screenAttrs = MutableLiveData<ViewAttributes?>()
    val screenAttrs: LiveData<ViewAttributes?> = _screenAttrs


    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /*
        private val _notificationsFlow = MutableStateFlow<NotificationsRepository.DataEvent?>(null)
        val notificationsFlow: StateFlow<NotificationsRepository.DataEvent?> get() = _notificationsFlow
    */
    private val _notificationsStatus = MutableLiveData<Resource<Boolean?>>()
    val notificationsStatus: LiveData<Resource<Boolean?>> = _notificationsStatus


    private val _unreadMessagesFlow = MutableLiveData<List<UnreadMessages>>(emptyList())
    val unreadMessagesFlow: LiveData<List<UnreadMessages>> get() = _unreadMessagesFlow


    private val _subscribingToEventStatus = MutableLiveData<Resource<String?>>()
    val subscribingToEventStatus: LiveData<Resource<String?>> = _subscribingToEventStatus

    private val _currentModule = MutableLiveData<Int>()
    val currentModule: LiveData<Int> = _currentModule

    private val _dialogToShow = MutableLiveData<DialogToShowModel>()
    val dialogToShow: LiveData<DialogToShowModel> = _dialogToShow

    private val _validationRequestDialog = MutableLiveData<PulseValidationRequest>()
    val validationRequestDialog: LiveData<PulseValidationRequest> = _validationRequestDialog

    private val _goBack = MutableLiveData<Boolean>()
    val goBack: LiveData<Boolean> = _goBack

    private val _loaderCompass = MutableLiveData<Boolean?>()
    val loaderCompass: LiveData<Boolean?> = _loaderCompass

    private val _loader = MutableLiveData<Boolean?>()
    val loader: LiveData<Boolean?> = _loader


    private val _isInPanic = MutableLiveData<Boolean>()
    val isInPanic: LiveData<Boolean> = _isInPanic

    private val _eventsCount = MutableLiveData<Int>(0)
    val eventsCount: LiveData<Int> = _eventsCount

    /*
        private val _eventsFlow = MutableLiveData<EventsRepository.DataEvent>()
        val eventsFlow: LiveData<EventsRepository.DataEvent> get() = _eventsFlow
    */
    private val _refreshEventsShortcut = MutableLiveData<Boolean>()
    val refreshEventsShortcut: LiveData<Boolean> = _refreshEventsShortcut

    private val _panicButtonStatus = MutableLiveData<Resource<Event?>>()
    val panicButtonStatus: LiveData<Resource<Event?>> = _panicButtonStatus

    private val _closingEventStatus = MutableLiveData<Resource<Boolean?>>()
    val closingEventStatus: LiveData<Resource<Boolean?>> = _closingEventStatus


    private val _showEventsCounter = MutableLiveData<Boolean>()
    val showEventsCounter: LiveData<Boolean> = _showEventsCounter

    private val _enableMapIcon = MutableLiveData<Boolean>()
    val enableMapIcon: LiveData<Boolean> = _enableMapIcon

    private val _eventsFollowed = MutableLiveData<ArrayList<EventFollowed>>()
    val eventsFollowed: LiveData<ArrayList<EventFollowed>> = _eventsFollowed

    private val _onNotificationClicked = MutableLiveData<Resource<HashMap<String, Any?>>>()
    val onNotificationClicked: LiveData<Resource<HashMap<String, Any?>>> =
        _onNotificationClicked


    private val _newEventPopupToShow = MutableLiveData<Resource<HashMap<String, Any?>>?>()
    val newEventPopupToShow: LiveData<Resource<HashMap<String, Any?>>?> = _newEventPopupToShow
    /*
        fun listenNotifications() = liveData<NotificationsRepository.DataEvent>(Dispatchers.IO) {
            notificationsRepository.getDataFlow(userKey).collect { notifications ->
                _notificationsFlow.value = notifications
                emit(notifications)
            }

        }
    */


    init {
        _bluetoothStatus.postValue(SessionApp.getInstance(context).isBTPanicButtonEnabled)
        _recordingStatus.postValue(RecordingStatusEnum.NONE)
        _eventsFollowed.value = ArrayList<EventFollowed>()
        _isInPanic.postValue(false)


    }

    /**
     * Forma correcta de escuchar los cambios en la base de datos
     */
    val notificationsFlow = liveData<NotificationsRepository.DataEvent>(Dispatchers.IO)
    {
        notificationsRepository.getDataFlow(userKey).collect { notifications ->
            emit(notifications)
        }
    }


    private val _resetUnreadsForRoom = MutableLiveData<String?>()
    val resetUnreadsForRoom: LiveData<String?> = _resetUnreadsForRoom
    fun resetUnreadMessages(roomKey: String?) {
        if (roomKey != null) {
            _resetUnreadsForRoom.postValue(roomKey)

        }
    }


    /**
     * Analiza de acuerdo a los cambios en el listado de eventos, como deben comportarse los distintos controles
     */
    private fun processEventsRelatedActions() {


        if (getEventsCount() > 0) {
            _enableMapIcon.postValue(true)
            _showEventsCounter.postValue(true)

            var newPanicStatus = IsInPanic()
            if (_isInPanic.value != newPanicStatus) {
                _isInPanic.postValue(newPanicStatus)
            }
        } else {
            _enableMapIcon.postValue(false)
            _showEventsCounter.postValue(false)
            _isInPanic.postValue(false)

        }
    }

    private fun IsInPanic(): Boolean {
        var toReturn = false
        _eventsFollowed.value?.forEach { event ->
            if (event.author.author_key == FirebaseAuth.getInstance().uid.toString() && event.event_type == EventTypesEnum.PANIC_BUTTON.toString()) toReturn =
                true
            return@forEach
        }
        return toReturn

    }


    fun eventExists(eventKey: String): Boolean {
        var eventExists = false
        _eventsFollowed.value?.forEach { event ->
            if (event.event_key == eventKey) {
                eventExists = true
                return@forEach
            }

        }
        return eventExists
    }

    fun getFirstEvent(): EventFollowed? {
        var toReturn: EventFollowed? = null
        AppClass.instance.eventsFollowed.forEach { key, value ->
            toReturn = value
            return@forEach
        }
        return toReturn
    }

    fun getEvents(): ArrayList<EventFollowed> {
        var newArray = ArrayList<EventFollowed>()
        AppClass.instance.eventsFollowed.forEach { key, value ->
            newArray.add(value)
        }
        return newArray
    }


    /**
     * Retorna la cantidad de eventos activos del usuario
     */
    fun getEventsCount() = AppClass.instance.getEventsCount() ?: 0

    fun getEventFollowedByKey(eventKey: String): EventFollowed? {

        _eventsFollowed.value?.forEach { event ->
            if (event.event_key == eventKey) {
                return event
            }
        }
        return null
    }

    fun setButtonToPanic() {
        _isInPanic.postValue(true)
    }

    fun resetRefreshCommand() {
        _refreshEventsShortcut.value = false
    }


    fun postPanicEvent(event: Event) {
        _panicButtonStatus.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {

            Log.d("ITags", "Termine de publicar el evento")
            event.media?.forEach { event ->
                if (event.media_type == MediaTypesEnum.VIDEO || event.media_type == MediaTypesEnum.AUDIO || event.media_type == MediaTypesEnum.IMAGE) {
                    val fileExtension = event.file_name.getFileExtension(context)
                    var fileUri = event.file_name
                    if (fileExtension?.lowercase(Locale.getDefault()) == "jpg") {
                        fileUri = "file:" + event.file_name
                    }
                    var mediaFileEncoded: String? = null
                    if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                            Locale.getDefault()
                        ) == "mp4" || fileExtension?.lowercase(Locale.getDefault()) == "3gp"
                    ) {

                        mediaFileEncoded =
                            MultimediaUtils(context).convertFileToBase64(Uri.parse(fileUri))
                                .toString()
                    }
                    event.bytesB64 = mediaFileEncoded
                }


            }


            var call = eventsRepository.postEvent(event)

            if (call.message == null) {
                //              iCallback?.onComplete(true, call.data)
                _panicButtonStatus.postValue(Resource.Success<Event?>(call.data))

            } else {
//                iCallback?.onError(java.lang.Exception(call.message))
                _panicButtonStatus.postValue(Resource.Error<Event?>(call.message ?: "".toString()))

            }
        }

    }

    fun goBack() {
        _goBack.postValue(true)
    }

    fun showLoader(lottieFileID: Int) {
        _loader.postValue(true)
    }

    fun showLoader() {
        _loader.postValue(true)
    }

    fun hideLoader() {
        _loader.postValue(false)
    }

    fun switchToModule(moduleIndex: Int, tag: String) {
        _currentModule.postValue(moduleIndex)
    }

    fun extendEvent(eventKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = eventRepository.extendEvent(userKey, eventKey)
            if (result is Resource.Success) {
                _dialogToShow.postValue(
                    DialogToShowModel(
                        DialogsEnum.EVENTEXTENDEDTIMEDIALOG, null
                    )
                )
            }
        }
    }

    fun onCloseEventRequest(eventKey: String, code: String) {
        _closingEventStatus.postValue(Resource.Loading<Boolean?>())
        viewModelScope.launch(Dispatchers.IO) {
            val result = eventRepository.closeEvent(userKey, eventKey, code)
            if (result is Resource.Success) {
                _closingEventStatus.postValue(Resource.Success<Boolean?>(true))

                _dialogToShow.postValue(
                    DialogToShowModel(
                        DialogsEnum.EVENT_CLOSE_SUCCESSFULLY, eventKey
                    )
                )

            } else {
                _closingEventStatus.postValue(Resource.Error<Boolean?>(result.message.toString()))
            }
        }
    }

    fun securityPINIntroduced(pin: String, location: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = usersRepository.updateUserStatus(userKey, pin, location)
            if (result is Resource.Success) {
                _dialogToShow.postValue(
                    DialogToShowModel(
                        DialogsEnum.STATUS_VALIDATION_DIALOG, result.data
                    )
                )
            }
        }
    }

    fun postEventClicked(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = eventRepository.postEvent(event)
        }

    }


    private val _urlImageToPreLoad = MutableLiveData<String>()
    val urlImageToPreLoad: LiveData<String> = _urlImageToPreLoad

    fun preDownloadImage(path: String, subFolder: String, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = storageRepositoryImpl.getFileUrl(path, subFolder, fileName)
            if (call is Resource.Success) {
                _urlImageToPreLoad.postValue(call.data as String)
            } else {
                _error.postValue(call.message!!)
            }
        }
    }

    suspend fun getResourceUrl(
        path: String, subFolder: String, fileName: String
    ): Resource<String?> = storageRepositoryImpl.getFileUrl(path, subFolder, fileName)

    fun setUser(user: User) {
        //_user.postValue(user)
        _user.value = user
    }

    fun setLocationIsAvailable() {
        _isLocationAvailable.postValue(true)
    }

    fun setLocationNotAvailable() {
        _isLocationAvailable.postValue(false)
    }

    fun setTitle(title: String) {
        _title.postValue(title)
    }

    fun showError(text: String) {
        _error.postValue(text)
    }

    fun onLocationSearchStart() {
        _loaderCompass.postValue(true)
    }

    fun onLocationSearchEnd() {
        _loaderCompass.postValue(false)
    }

    fun onMediaPicked(mediaFile: MediaFile) {
        _newMedia.postValue(mediaFile)
    }

    fun onStartRecordingPressed() {
        _recordingStatus.postValue(RecordingStatusEnum.RECORDING)
    }

    fun onStopRecording() {
        _recordingStatus.postValue(RecordingStatusEnum.STOPING)

    }


    fun setAudioRecordingFileName(recordingFilename: String) {
        _recordFile = recordingFilename
    }

    fun getAudioRecordingFileName(): String? {
        return _recordFile
    }

    fun resetAudioRecordingFileName() {
        _recordFile = null
    }

    fun clearMedia() {
        TODO("Not yet implemented")
    }

    fun resetNewMedia() {
        _newMedia.value = null
    }


    //--------- BOTTOM BAR ---------
    fun onBottomBarFriendsClicked() {

        var subscriptionLevel: Int = _userSubscription.value?.access_level ?: 0
        if (subscriptionLevel >= AccessLevelsEnum.SOLIDARY.ordinal) {
            switchToModule(3, "friends")
        } else {
            var bundle = Bundle()
            bundle.putString("go_to", SettingsFragmentsEnum.PLAN_SETTINGS.name)
            switchToModule(IANModulesEnum.SETTINGS.ordinal, "settings")

        }

    }

    fun onProfileSettingsClick() {
        switchToModule(SettingsFragmentsEnum.PROFILE_SETTINGS.ordinal, "profile_settings")
    }

    fun onSOSSettingsClick() {
        switchToModule(SettingsFragmentsEnum.SOS_SETTINGS.ordinal, "sos_settings")
    }

    fun onSubscriptionSettingsClick() {
        switchToModule(SettingsFragmentsEnum.PLAN_SETTINGS.ordinal, "plan_settings")
    }

    fun onNotificationGroupsSettingsClick() {
        switchToModule(
            SettingsFragmentsEnum.NOTIFICATION_GROUPS.ordinal, "notification_group_settings"
        )
    }

    fun onPushButtonSettingsClick() {

        switchToModule(SettingsFragmentsEnum.PUSH_BUTTONS_SETTINGS.ordinal, "push_button_settings")

    }

    fun setBluetoothState(enabled: Boolean) {
        _bluetoothStatus.postValue(enabled)
    }

    fun onPanicStateChanged(inPanic: Boolean?) {
        _isInPanic.postValue(inPanic!!)
    }

    fun requestValidationDialog(validatorRequestType: PulseValidationRequest) {
        _validationRequestDialog.postValue(validatorRequestType)
    }


    // Contactos
    fun onAcceptContactRequest(userKey: String) {
        _notificationsStatus.postValue(Resource.Loading<Boolean?>())
        viewModelScope.launch(Dispatchers.IO) {
            val call = contactsRepository.contactAcceptInvitation(FirebaseAuth.getInstance().uid.toString(),userKey)
            if (call.message == null) {
                _notificationsStatus.postValue(Resource.Success<Boolean?>(true))
            } else {
                _notificationsStatus.postValue(Resource.Error<Boolean?>(call.message.toString()))
            }
        }
    }

    fun onAgreeToAssist(notificationKey: String, eventKey: String) {
        _subscribingToEventStatus.postValue(Resource.Loading<String?>())
        viewModelScope.launch(Dispatchers.IO) {
            val userKey = _user.value?.user_key.toString()
            val call = eventRepository.subscribeToEvent(userKey, notificationKey)
            if (call.message == null) {
                _subscribingToEventStatus.postValue(Resource.Success<String?>(eventKey))
            } else {
                _subscribingToEventStatus.postValue(Resource.Error<String?>(call.message.toString()))
            }
        }
    }

    fun updateUserSubscriptionType(subscriptionType: SubscriptionTypes) {
        _userSubscription.postValue(subscriptionType)
    }

    fun updateFollowedEvents(events: ArrayList<EventFollowed>) {
        _eventsCount.postValue(events.size)
        //    _eventsFollowed.postValue(events)
    }

    fun onBottomBarVisibilityOffRequired() {
        _bottomBarVisibilityStatus.postValue(false)
    }

    fun onBottomBarVisibilityOnRequired() {
        _bottomBarVisibilityStatus.postValue(true)
    }

    fun onKeyboardOpened() {
        _isKeyboardOpen.postValue(true)
    }

    fun onKeyboardClosed() {
        if (_isKeyboardOpen.value != null)
            _isKeyboardOpen.postValue(false)
    }

    /*
        fun onUserStatusUpdated(pin: String, latLng: LatLng) {
            viewModelScope.launch(Dispatchers.IO)
            {
                val result = usersRepository.updateUserStatus(userKey, pin, latLng)
            }
        }
    */

    fun isKeyboardOpen() = _isKeyboardOpen.value ?: false

    fun setScreenSize(width: Int, height: Int) {
        val attrs = ViewAttributes()
        attrs.width = width
        attrs.height = height
        _screenAttrs.postValue(attrs)
    }

    fun resetBottomBarVisibilityStatus() {
        _bottomBarVisibilityStatus.value = null
    }

    fun onTitleBarVisibilityOffRequired() {
        _titleBarCardViewVisible.postValue(false)
    }

    fun onTitleBarVisibilityOnRequired() {
        _titleBarCardViewVisible.postValue(true)
    }

    var pendingNotificationsArray = ArrayList<NotificationsUtils.PushNotification>()
    private val _pendingNotifications =
        MutableLiveData<ArrayList<NotificationsUtils.PushNotification>>()
    val pendingNotifications: LiveData<ArrayList<NotificationsUtils.PushNotification>> =
        _pendingNotifications

    fun onNewPendingNotification(notification: NotificationsUtils.PushNotification) {
        pendingNotificationsArray.add(notification)
        _pendingNotifications.postValue(pendingNotificationsArray)
    }

    fun onNewMessageNotification(notificationInfo: NotificationsUtils.PushNotification) {

        viewModelScope.launch(Dispatchers.IO) {
            var exists = eventRepository.checkIfEventExists(notificationInfo.linkKey.toString())
            if (exists is Resource.Success) {
                var myMap = HashMap<String, Any?>()
                myMap.put("notification", notificationInfo)
                _onNotificationClicked.postValue(Resource.Success(myMap))
            } else {
                _onNotificationClicked.postValue(Resource.Error(exists.message.toString()))
            }
        }
    }

    fun onPanicButtonNotification(notificationInfo: NotificationsUtils.PushNotification) {
        viewModelScope.launch(Dispatchers.IO) {
            var exists = eventRepository.getEvent(notificationInfo.linkKey.toString())
            if (exists is Resource.Success) {
                var myMap = HashMap<String, Any?>()
                myMap.put("notification", notificationInfo)
                myMap.put("object", exists.data)

                _onNotificationClicked.postValue(Resource.Success(myMap))
            } else {
                _onNotificationClicked.postValue(Resource.Error(exists.message.toString()))
            }
        }
    }

    fun onNewEventNotification(notification: EventNotificationModel) {
        var pp = 3
        viewModelScope.launch(Dispatchers.IO) {
            var eventKey = notification.event_key.toString()
            var exists = eventRepository.getEvent(eventKey)
            if (exists is Resource.Success) {

                var isFollowing = false
                eventsFollowed.value?.forEach { eventFollowed ->
                    if (eventFollowed.event_key == eventKey) {
                        isFollowing = true
                    }
                }
                if (!isFollowing) {
                    var myMap = HashMap<String, Any?>()
                    myMap.put("notification", notification)
                    myMap.put("object", exists.data)

                    _newEventPopupToShow.postValue(Resource.Success(myMap))
                }
            } else {
                _newEventPopupToShow.postValue(Resource.Error(exists.message.toString()))
            }
        }

    }

    fun onGoToChatPressed(channelKey: String, messageKey: String) {

        viewModelScope.launch(Dispatchers.IO) {

            var exists = eventRepository.checkIfEventExists(channelKey)
            if (exists is Resource.Success) {
                var myMap = HashMap<String, Any?>()


                var notification =
                    NotificationsUtils.PushNotification(NOTIFICATION_TYPE_NEW_MESSAGE)

                myMap.put("notification", notification)
                myMap.put("channel_key", channelKey)
                myMap.put("message_key", messageKey)
                _onNotificationClicked.postValue(Resource.Success(myMap))
            } else {
                //  _newEventPopupToShow.postValue(Resource.Error(exists.message.toString()))
            }
        }


    }

    /**
     * Actualiza la seccion de notificacion cuando el usuario lee un mensaje para que lo borre de las notificaciones
     * @param messageKey
     */
    fun onMessageRead(eventKey: String, messageKey: String) {

        // borro el mensaje de la lista de mensajes sin notificar.

        viewModelScope.launch(Dispatchers.IO) {
            var result = notificationsRepository.onMessageRead(userKey, eventKey, messageKey)
            var pp = 33
        }
    }
}