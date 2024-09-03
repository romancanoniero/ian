package com.iyr.ian.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.NOTIFICATION_TYPE_NEW_MESSAGE
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.dao.models.Subscription
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.dao.models.UnreadMessages
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.dao.repositories.NotificationsRepository
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.enums.DialogsEnum
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.enums.IANModulesEnum
import com.iyr.ian.enums.RecordingStatusEnum
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ChatRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ContactsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationListRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SubscriptionTypeRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersSubscriptionsRepositoryImpl
import com.iyr.ian.services.eventservice.EventService
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.ui.base.PulseValidationRequest
import com.iyr.ian.ui.settings.SettingsFragmentsEnum
import com.iyr.ian.utils.NotificationsUtils
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getCacheLocation
import com.iyr.ian.utils.getFileExtension
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.loadImageFromCache
import com.iyr.ian.utils.models.DialogToShowModel
import com.iyr.ian.utils.models.ViewAttributes
import com.iyr.ian.utils.multimedia.MultimediaUtils
import com.iyr.ian.utils.saveImageToCache
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModel private constructor(
    val context: Context? = null, var userKey: String? = null
) : ViewModel() {

    enum class AppStatus {
        NOT_LOGGED, INITIALIZING, READY
    }


    private var serverTimeRef: Long? = 0
    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()
    private var usersSubscriptionsRespository: UsersSubscriptionsRepositoryImpl =
        UsersSubscriptionsRepositoryImpl()
    private var eventsRepository: EventsRepositoryImpl = EventsRepositoryImpl()
    private var eventRepository: EventRepositoryImpl = EventRepositoryImpl()
    private var notificationsRepository: NotificationsRepositoryImpl = NotificationsRepositoryImpl()
    private var chatRepository: ChatRepositoryImpl = ChatRepositoryImpl()
    private var contactsRepository: ContactsRepositoryImpl = ContactsRepositoryImpl()
    private val contactsGroupsRepository = NotificationListRepositoryImpl()


    private val storageRepositoryImpl: StorageRepositoryImpl = StorageRepositoryImpl()
    private val subscriptionsRepository: SubscriptionTypeRepositoryImpl =
        SubscriptionTypeRepositoryImpl()


    private val _homeIconVisible = MutableLiveData<Boolean>(false)
    val homeIconVisible: LiveData<Boolean> = _homeIconVisible

    private lateinit var userFlowJob: Job


    private val _appStatus = MutableLiveData<AppStatus?>()
    val appStatus: LiveData<AppStatus?> = _appStatus

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

    var currentFragment : Int? = null

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
    val onNotificationClicked: LiveData<Resource<HashMap<String, Any?>>> = _onNotificationClicked


    private val _newEventPopupToShow = MutableLiveData<Resource<HashMap<String, Any?>>?>()
    val newEventPopupToShow: LiveData<Resource<HashMap<String, Any?>>?> = _newEventPopupToShow/*
        fun listenNotifications() = liveData<NotificationsRepository.DataEvent>(Dispatchers.IO) {
            notificationsRepository.getDataFlow(userKey).collect { notifications ->
                _notificationsFlow.value = notifications
                emit(notifications)
            }

        }
    */

    companion object {
        private lateinit var instance: MainActivityViewModel

        @MainThread
        fun getInstance(context: Context? = null, userKey: String? = null): MainActivityViewModel {
            instance = if (::instance.isInitialized) instance else {
                if (context == null) throw Exception("Context and userKey must be provided")
                else MainActivityViewModel(context, userKey)
            }
            return instance
        }


    }


    init {
        _bluetoothStatus.postValue(SessionApp.getInstance(context).isBTPanicButtonEnabled)
        _recordingStatus.postValue(RecordingStatusEnum.NONE)
        _eventsFollowed.value = ArrayList<EventFollowed>()
        _isInPanic.postValue(false)
    }

    /*
    fun fetchServerTime() {
        viewModelScope.launch(Dispatchers.IO) {
            this@MainActivityViewModel.serverTimeRef = fetchServerTime()
        }
    }
*/
    //    private fun getServerTime() {


    var timeDifference: Long = 0

    private val _serverTime = MutableLiveData<Long?>()
    val serverTime: LiveData<Long?> = _serverTime
    suspend fun fetchServerTime(): Long? {
        viewModelScope.launch(Dispatchers.IO) {
            val rootRef = FirebaseDatabase.getInstance().reference
            rootRef.child("serverTime").setValue(ServerValue.TIMESTAMP).await()
            val serverTime = rootRef.child("serverTime").get().await().getValue(Long::class.java)
            val deviceTime = System.currentTimeMillis()
            // Calcula la diferencia entre la hora del servidor y la hora local del dispositivo
            timeDifference = (serverTime ?: 0) - deviceTime
            _serverTime.postValue(serverTime)
        }
        return null
    }


    // Función para obtener la hora del servidor usando la hora local del dispositivo y la diferencia de tiempo
    fun getServerTimeUsingDeviceTime(): Long {
        val currentDeviceTime = System.currentTimeMillis()
        return currentDeviceTime - timeDifference
    }

    // Función para verificar si un horario establecido en el servidor está dentro de un rango de tiempo
    fun isServerTimeInRange(startTime: Long, endTime: Long): Boolean {
        val currentServerTime = getServerTimeUsingDeviceTime()
        return currentServerTime in startTime..endTime
    }


//        return Resource.Loading<Long?>(0)

    //  }

    private val _notificationsStatus = MutableLiveData<Resource<Boolean?>>()
    val notificationsStatus: LiveData<Resource<Boolean?>> = _notificationsStatus

    val notificationsList = ArrayList<EventNotificationModel>()
    private val _notifications = MutableLiveData<ArrayList<EventNotificationModel>>()
    val notifications: LiveData<ArrayList<EventNotificationModel>> = _notifications

    /**
     * Forma correcta de escuchar los cambios en la base de datos
     */
    val notificationsFlow = liveData<NotificationsRepository.DataEvent>(Dispatchers.IO) {
        notificationsRepository.getDataFlow(userKey!!).collect { event ->
            emit(event)
            when (event) {
                is NotificationsRepository.DataEvent.ChildAdded -> {
                    if (currentFragment!= null && (currentFragment == R.id.messagesInEventFragment ||  MainActivityViewModel.getInstance().currentFragment == R.id.mapSituationFragment) &&
                        MapSituationFragmentViewModel.getInstance().currentEventKey == event.data.event_key)
                    {
                        return@collect
                    }

                    if (!notificationsList.contains(event.data)) {
                        notificationsList.add(event.data)
                    }
                }

                is NotificationsRepository.DataEvent.ChildChanged -> {
                    if (currentFragment!= null && ( currentFragment == R.id.messagesInEventFragment  ||  MainActivityViewModel.getInstance().currentFragment == R.id.mapSituationFragment)&&
                        MapSituationFragmentViewModel.getInstance().currentEventKey == event.data.event_key)
                    {
                        return@collect
                    }
                    val index = notificationsList.indexOf(event.data)
                    if (index == -1) {
                        notificationsList.add(event.data)
                    } else {
                        notificationsList[index] = event.data
                    }
                }

                is NotificationsRepository.DataEvent.ChildRemoved -> {
                    val index = notificationsList.indexOf(event.data)
                    if (index > -1) {
                        notificationsList.removeAt(index)
                    }
                }

                else -> {}
            }
            _notifications.postValue(notificationsList)
        }
    }


    val userSubscriptionsFlow = liveData<Resource<List<Subscription>?>>(Dispatchers.IO) {
        usersSubscriptionsRespository.getUserSubscriptionsAsFlow(userKey!!)
            .collect { subscriptions ->
                emit(subscriptions)
            }
    }


    private val _subscriptionType = MutableLiveData<SubscriptionTypes>()
    val subscriptionType: LiveData<SubscriptionTypes> = _subscriptionType
    val userSubscriptionTypeAsFlow = liveData<Resource<SubscriptionTypes?>>(Dispatchers.IO) {
        usersSubscriptionsRespository.getUserSubscriptionTypeAsFlow(userKey!!).collect { resource ->
            _subscriptionType.postValue(resource.data!!)

            emit(resource)
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
                    val fileExtension = event.file_name.getFileExtension(context!!)
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
            val result = eventRepository.extendEvent(userKey!!, eventKey)
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
            val result = eventRepository.closeEvent(userKey!!, eventKey, code)
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
            val result = usersRepository.updateUserStatus(userKey!!, pin, location)
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
        this.userKey = user.user_key
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

        var subscriptionLevel: Int = _subscriptionType.value?.access_level ?: 0
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
            val call = contactsRepository.contactAcceptInvitation(
                FirebaseAuth.getInstance().uid.toString(), userKey
            )
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
        _subscriptionType.postValue(subscriptionType)
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
        if (_isKeyboardOpen.value != null) _isKeyboardOpen.postValue(false)
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
            var result = notificationsRepository.onMessageRead(userKey!!, eventKey, messageKey)

        }
    }

    fun setAppStatus(status: MainActivityViewModel.AppStatus) {
        _appStatus.postValue(status)
        if (status == MainActivityViewModel.AppStatus.INITIALIZING) {
            viewModelScope.launch(Dispatchers.IO) {
                fetchServerTime()
            }
        }
    }


    private val _contactRequest = MutableLiveData<Resource<UserMinimum?>>()
    val contactRequest: LiveData<Resource<UserMinimum?>> = _contactRequest

    /***
     * Gestiona cuando llega una invitacion de contacto
     */
    fun onContactByUserKey(otherUserKey: String) {

        _contactRequest.postValue(Resource.Loading<UserMinimum?>())
        viewModelScope.launch(Dispatchers.IO) {

            val alreadyContactedCall = contactsRepository.alreadyFriends(
                UserViewModel.getInstance().getUser()?.user_key ?: "",
                otherUserKey
            )
            val alreadyContacted = alreadyContactedCall.data ?: false
            if (!alreadyContacted) {
                val userCall = usersRepository.getUserRemote(otherUserKey)
                when (userCall) {
                    is Resource.Success -> {
                        var user = userCall.data
                        if (user != null) {

                            val context = AppClass.instance.context!!
                            // me aseguro de conseguir la imagen del usuario

                            val userAvatar = user.image.file_name
                            var destinationPath =
                                context.getCacheLocation(AppConstants.PROFILE_IMAGES_STORAGE_PATH + userAvatar)

                            var localPath = userAvatar.toString()

                            var fileName = userAvatar.getJustFileName()

                            try {

                                var imageBitmap = context.loadImageFromCache(
                                    fileName,
                                    "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${user.user_key}"
                                )

                                if (imageBitmap == null) { // No esta en el cache, lo busco en el servidor local
                                    val storageReference =
                                        FirebaseStorage.getInstance().reference.child(
                                            "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${user.user_key}/${
                                                fileName.substringAfterLast("/")
                                                    .substringBefore(".")
                                            }.jpg"
                                        )
                                    try {
                                        // Comprueba si la imagen existe en Firebase Storage
                                        storageReference.metadata.await()

                                        // Descarga la imagen y la guarda en el caché
                                        val requestOptions = RequestOptions().diskCacheStrategy(
                                            DiskCacheStrategy.ALL
                                        )

                                        val result = withContext(Dispatchers.IO) {
                                            GlideApp.with(context).asBitmap().load(storageReference)
                                                .apply(requestOptions)
                                                .submit().get()
                                        }
                                        result.saveImageToCache(
                                            context,
                                            fileName,
                                            AppConstants.PROFILE_IMAGES_STORAGE_PATH
                                        )

                                    } catch (e: StorageException) {
                                        var pp = 3
                                    }
                                }
                                _contactRequest.postValue(Resource.Success<UserMinimum?>(user))
                            } catch (ex: Exception) {
                                _contactRequest.postValue(Resource.Error<UserMinimum?>(ex.message.toString()))
                            }

                        }
                    }

                    is Resource.Error -> {
                        _contactRequest.postValue(Resource.Error<UserMinimum?>(userCall.message.toString()))
                    }

                    is Resource.Loading -> TODO()

                }

            } else {
                _contactRequest.postValue(Resource.Error<UserMinimum?>("already_friends"))

            }
        }
    }

    private val _contactAcceptance = MutableLiveData<Resource<HashMap<String, Contact>?>>()
    val contactAcceptance: LiveData<Resource<HashMap<String, Contact>?>> = _contactAcceptance
    fun acceptContactInvitation(userToAcceptKey: String) {
        val meKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
        _contactAcceptance.postValue(Resource.Loading())
        viewModelScope.launch {
            val call = contactsRepository.contactAcceptInvitation(meKey, userToAcceptKey)
            _contactAcceptance.postValue(call)
        }
    }


    private val _postingEvent = MutableLiveData<Resource<Event?>>()
    val postingEvent: LiveData<Resource<Event?>> = _postingEvent

    /**
     * Cuando el evento esta completo, lo dispara
     */
    fun onEventReadyToFire(event: Event) {
        val eventService = EventService.getInstance(context!!)
        eventService.fireEvent(event)
    }

    private val _validationResult = MutableLiveData<Resource<Bundle?>>()
    val validationResult: LiveData<Resource<Bundle?>> = _validationResult
    fun onValidationDone(arguments: Bundle?) {
        arguments?.let { bundle ->
            viewModelScope.launch(Dispatchers.IO) {

                val userKey = UserViewModel.getInstance().getUser()?.user_key.toString()

                val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable("action", PulseRequestTarget::class.java)
                } else {
                    bundle.getSerializable("action")
                }
                val eventKey: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable("event_key", String::class.java).toString()
                } else {
                    bundle.getSerializable("event_key").toString()
                }
                val securityCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable("code", String::class.java).toString()
                } else {
                    bundle.getSerializable("code").toString()
                }
                _validationResult.postValue(Resource.Loading())
                when (action) {
                    PulseRequestTarget.PULSE_VALIDATION -> TODO()
                    PulseRequestTarget.VALIDATE_USER -> TODO()
                    PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT -> {
                        try {
                            val call = eventRepository.closeEvent(eventKey, userKey, securityCode)
                            _validationResult.postValue(Resource.Success<Bundle?>(Bundle()))
                        } catch (ex: Exception) {
                            _validationResult.postValue(Resource.Error<Bundle?>(ex.message.toString()))
                        }
                    }

                    PulseRequestTarget.ON_FALLING_VALIDATION -> TODO()
                    null -> {}
                }

            }

        }

    }

    fun onError(errorMessage: String) {
        _error.postValue(errorMessage)
    }
}