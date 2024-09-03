package com.iyr.ian.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.dao.models.SpeedMessage
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.ChatRepository
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ChatRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationsRepositoryImpl
import com.iyr.ian.ui.KeyboardStatusEnum
import com.iyr.ian.ui.chat.ChatWindowStatus
import com.iyr.ian.utils.chat.enums.MessagesStatus
import com.iyr.ian.utils.chat.models.Author
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessagesInEventFragmentViewModel : ViewModel() {

    private var chatRepository: ChatRepositoryImpl = ChatRepositoryImpl()
    private var eventRepository: EventRepositoryImpl = EventRepositoryImpl()
    private var notificationsRepository: NotificationsRepositoryImpl = NotificationsRepositoryImpl()


    private val _chatWindowStatus = MutableLiveData<ChatWindowStatus>(ChatWindowStatus.NORMAL)
    val chatWindowStatus: LiveData<ChatWindowStatus> = _chatWindowStatus

    private var _eventKey: String =
        MapSituationFragmentViewModel.getInstance().eventFlow.value?.data?.event_key?.toString()
            ?: ""

    private val messages = ArrayList<Message>()

    private val _storingMessage = MutableLiveData<Resource<String?>>()
    val storingMessage: LiveData<Resource<String?>> = _storingMessage

    private val _messageModified = MutableLiveData<HashMap<String, Any>>()
    val messageModified: LiveData<HashMap<String, Any>> = _messageModified

    private val _isMaximizeButtonVisible = MutableLiveData<Boolean?>()
    val isMaximizeButtonVisible: LiveData<Boolean?> = _isMaximizeButtonVisible

    private val _isCloseButtonVisible = MutableLiveData<Boolean?>()
    val isCloseButtonVisible: LiveData<Boolean?> = _isCloseButtonVisible

    //-------------
    private var chatEventsArray: ArrayList<ChatRepository.ChatDataEvent> =
        ArrayList<ChatRepository.ChatDataEvent>()
    private val _chatEvents = MutableLiveData<ArrayList<ChatRepository.ChatDataEvent>>()
    val chatEvents: LiveData<ArrayList<ChatRepository.ChatDataEvent>> = _chatEvents
    //-------------

    private val _chatRoomFlow = MutableLiveData<ChatRepository.ChatDataEvent>()
    val chatRoomFlow: LiveData<ChatRepository.ChatDataEvent> get() = _chatRoomFlow


    private val _chatMessages = MutableLiveData<ArrayList<Message>>()
    val chatMessages: LiveData<ArrayList<Message>> = _chatMessages

    private val _sendingMessage = MutableLiveData<Resource<Boolean?>>()
    val sendingMessage: LiveData<Resource<Boolean?>> = _sendingMessage


    init {
        _chatMessages.value = ArrayList<Message>()
        _chatEvents.value = ArrayList<ChatRepository.ChatDataEvent>()
    }


    private suspend fun generateMessageKey(eventKey: String): Resource<String> {
        // Genero la clave del mensaje localmente para que no haga falta pedirle al servidor una clave
        var messageKey = "${eventKey}-${
            UserViewModel.getInstance().getUser()?.user_key
        }-${System.currentTimeMillis()}"
        return Resource.Success(messageKey)
        //return chatRepository.generateMessageKey(eventKey)
    }

    fun onNewMessage(sender: User, message: String) {
        viewModelScope.launch(Dispatchers.IO) {

            var call = generateMessageKey(_eventKey.toString())
            if (call.data != null) {
                var messageKey = call.data.toString()
                var previewMessage =
                    createLocalMessage(_eventKey.toString(), messageKey, sender, message)
                val _messageEvent = ChatRepository.ChatDataEvent.OnChildAdded(previewMessage, null)

//             insertPreviewMessage(_messageEvent)
                _messagePreview.postValue(_messageEvent) // agrego el preview


                chatEventsArray.add(_messageEvent)
                messages.add(previewMessage)
                _chatEvents.postValue(chatEventsArray)
                chatRepository.sendTextMessage(
                    _eventKey.toString(), sender.user_key, messageKey, message
                )
            } else {
                var implementar = 3
            }
        }
    }

    private val _messagePreview = MutableLiveData<ChatRepository.ChatDataEvent?>()
    val messagePreview: LiveData<ChatRepository.ChatDataEvent?> = _messagePreview
    private fun insertPreviewMessage(message: ChatRepository.ChatDataEvent.OnChildAdded) {

    }


    fun onNewSpeedMessage(message: Message) {

        _sendingMessage.postValue(Resource.Loading(true))

        viewModelScope.launch(Dispatchers.IO) {
            var call = generateMessageKey(_eventKey.toString())
            if (call.data != null) {
                var messageKey = call.data.toString()
                message.id = messageKey
                val _messageEvent = ChatRepository.ChatDataEvent.OnChildAdded(message, null)

                _messagePreview.postValue(_messageEvent) // agrego el preview

                chatEventsArray.add(_messageEvent)
                messages.add(message)
                _chatEvents.postValue(chatEventsArray)
                var result = chatRepository.sendSpeedMessage(
                    _eventKey.toString(),
                    FirebaseAuth.getInstance().currentUser?.uid.toString(),
                    messageKey,
                    message
                )

                _sendingMessage.postValue(Resource.Success(true))


                /*
                                when (SpeedMessageActions.valueOf(message.action.actionType.toString())) {
                                    SpeedMessageActions.GOING -> {
                                        eventRepository.togleGoingStatus(
                                            _eventKey.toString(),
                                            FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                            true
                                        )
                                    }

                                    SpeedMessageActions.NOT_GOING -> {
                                        eventRepository.togleGoingStatus(
                                            _eventKey.toString(),
                                            FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                            false

                                        )
                                    }
                                    SpeedMessageActions.CALLED -> {
                                        eventRepository.toggleCallAuthority(
                                            _eventKey.toString(),
                                            FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                            true
                                        )
                                    }
                                    SpeedMessageActions.NOT_CALLED -> {
                                        eventRepository.toggleCallAuthority(
                                            _eventKey.toString(),
                                            FirebaseAuth.getInstance().currentUser?.uid.toString(),
                                            false
                                        )
                                    }
                                    SpeedMessageActions.IM_THERE -> {


                                    }

                                    else ->{}
                                }
                */

            } else {
                var implementar = 3
            }
        }

    }

    /*
        suspend fun onSendMediaMessage(sender: User, mediaFile: MediaFile) {

            Log.d("VIDEO_FILE", "Llegue a onSendMediaMessage")


            //    viewModelScope.launch(Dispatchers.IO) {
            var call = generateMessageKey(_eventKey.toString())
            if (call.data != null) {
                var messageKey = call.data.toString()
                var previewMessage =
                    createLocalMessage(_eventKey.toString(), messageKey, sender, mediaFile)

                if (previewMessage.image != null) {
                    val _messageEvent = ChatRepository.ChatDataEvent.OnChildAdded(previewMessage, null)
                    var filePath = previewMessage.image.url
                    if (filePath.startsWith("/")) {
                        filePath = "file:$filePath"
                        previewMessage.image.url = filePath
                    }
                } else if (previewMessage.video != null) {
                    val _messageEvent = ChatRepository.ChatDataEvent.OnChildAdded(previewMessage, null)
                    var filePath = previewMessage.video.url
                    if (filePath.startsWith("/")) {
                        filePath = "file:$filePath"
                        previewMessage.video.url = filePath.getJustFileName()
                    }
                }


                Log.d("VIDEO_FILE", "postValue en _messagePreview ")

                _messagePreview.postValue(
                    ChatRepository.ChatDataEvent.OnChildAdded(
                        previewMessage, null
                    )
                )// agrego el preview

                //      _storingMessage.postValue(Resource.Loading(previewMessage))

            } else {
                var implementar = 3
            }
            //  }

        }

        fun onMediaMessageReadyToSend(message: Message, compressedBase64: String?) {

            viewModelScope.launch(Dispatchers.IO) {
                var messageKey = message.id
                var sender = message.user
                var mediaFile: MediaFile = MediaFile()
                mediaFile.user = User(sender.id, sender.name, sender.avatar)
                mediaFile.status = MediaFile.MEDIA_FILE_STATUS_NEW
                mediaFile.time = Date().time
                mediaFile.width = 0
                mediaFile.height = 0
                mediaFile.duration = 0


                compressedBase64?.let { compressedString ->
                    mediaFile.bytesB64 = compressedString
                }
                if (message.image != null) {
                    mediaFile.media_type = MediaTypesEnum.IMAGE
                    mediaFile.file_name = message.image.url
                } else if (message.video != null) {
                    mediaFile.media_type = MediaTypesEnum.VIDEO
                    mediaFile.file_name = message.video.url
                } else if (message.voice != null) {
                    mediaFile.media_type = MediaTypesEnum.AUDIO
                    mediaFile.file_name = message.voice.url
                }


                _storingMessage.postValue(Resource.Loading(message.id.toString()))

                val result = chatRepository.sendMediaMessage(
                    _eventKey.toString(), sender.id, messageKey, mediaFile
                )

                if (result != null) {
                    _storingMessage.postValue(Resource.Success(message.id.toString()))
                } else {
                    _storingMessage.postValue(Resource.Error("error_uploading"))
                }

            }


        }
    */
    private fun User(userKey: String, userName: String, imagePath: String): User {
        var newUser = User()
        newUser.user_key = userKey
        newUser.display_name = userName
        newUser.image = MediaFile(MediaTypesEnum.IMAGE, imagePath)
        return newUser
    }


    suspend fun onNewMediaMessage(sender: User, mediaFile: MediaFile) {

        var call = generateMessageKey(_eventKey.toString())
        if (call.data != null) {
            var messageKey = call.data.toString()
            var previewMessage =
                createLocalMessage(_eventKey.toString(), messageKey, sender, mediaFile)

            if (previewMessage.image != null) {
                //    val _messageEvent = ChatRepository.ChatDataEvent.OnChildAdded(previewMessage, null)
                var filePath = previewMessage.image.url
                if (filePath.startsWith("/")) {
                    filePath = "file:$filePath"
                    previewMessage.image.url = filePath
                }
            } else if (previewMessage.video != null) {
                //      val _messageEvent = ChatRepository.ChatDataEvent.OnChildAdded(previewMessage, null)
                var filePath = previewMessage.video.url
                if (filePath.startsWith("/")) {
                    filePath = "file:$filePath"
                    previewMessage.video.url = filePath
                }
            } else if (previewMessage.voice != null) {
                //        val _messageEvent = ChatRepository.ChatDataEvent.OnChildAdded(previewMessage, null)
                var filePath = previewMessage.voice.url
                if (filePath.startsWith("/")) {
                    filePath = "file:$filePath"
                    previewMessage.voice.url = filePath
                }
            }
            withContext(Dispatchers.Main) {
                _chatRoomFlow.postValue(
                    ChatRepository.ChatDataEvent.OnChildAdded(
                        previewMessage, null
                    )
                )
            }

            // Envio el mensaje al servidor.
            withContext(Dispatchers.IO) {

                val result = chatRepository.sendMediaMessage(
                    _eventKey.toString(), sender.user_key, messageKey, mediaFile
                )
                if (result is Resource.Error<*> == false) {

                    previewMessage.status = MessagesStatus.READY
                    _messagePreview.postValue(
                        ChatRepository.ChatDataEvent.OnChildChanged(
                            previewMessage, null
                        )
                    )// agrego el preview

                    _storingMessage.postValue(Resource.Success(previewMessage.id))
                } else {

                    previewMessage.status = MessagesStatus.ERROR
                    _messagePreview.postValue(
                        ChatRepository.ChatDataEvent.OnChildChanged(
                            previewMessage, null
                        )
                    )// agrego el preview


                    _storingMessage.postValue(
                        Resource.Error<String?>(
                            result.message.toString(), result.data.toString()
                        )
                    )
                }
            }
        } else {
            var implementar = 3
        }
        //      }
    }


    private fun createLocalMessage(
        eventKey: String, messageKey: String, sender: User, message: String
    ): Message {
        val author = Author(
            sender.user_key,
            sender.display_name,
            sender.user_key + "/" + sender.image.file_name,
            false
        )
        return Message(messageKey, author, message)
    }

    private fun createLocalMessage(
        eventKey: String, messageKey: String, sender: User, mediaFile: MediaFile
    ): Message {
        val author = Author(
            sender.user_key,
            sender.display_name,
            sender.user_key + "/" + sender.image.file_name,
            false
        )

        var newMessage = Message(messageKey, author)
        when (mediaFile.media_type) {
            MediaTypesEnum.IMAGE -> {
                val image = Message.Image(mediaFile.file_name)
                newMessage.image = image
            }

            MediaTypesEnum.VIDEO -> {
                val video = Message.Video(
                    mediaFile.file_name, mediaFile.duration, mediaFile.width, mediaFile.height
                )
                newMessage.video = video
                newMessage.status = MessagesStatus.SENDING
            }

            MediaTypesEnum.AUDIO -> {
                val voice = Message.Voice(
                    mediaFile.file_name, mediaFile.duration
                )
                newMessage.voice = voice
                newMessage.status = MessagesStatus.SENDING
            }

            MediaTypesEnum.TEXT -> TODO()
        }
        return newMessage
    }

    private fun createLocalMessage(
        eventKey: String, messageKey: String, sender: User, message: SpeedMessage
    ): Message {
        val author = Author(
            sender.user_key,
            sender.display_name,
            sender.user_key + "/" + sender.image.file_name,
            false
        )
        return Message(messageKey, author, message)
    }


    fun clearPendingEvents() {
        chatEventsArray.clear()
    }

    fun removePending(_event: ChatRepository.ChatDataEvent.OnChildAdded) {
        chatEventsArray.remove(_event)
    }

    fun onMediaPicked(media: MediaFile) {
        viewModelScope.launch(Dispatchers.IO) {
            var call = generateMessageKey(_eventKey.toString())
            if (call.data != null) {
                var messageKey = call.data.toString()
            }
        }
    }


    fun onConnectToEvent(eventKey: String) {
        _eventKey = eventKey
    }

    /**
     * Establece la Key del evento actual
     */
    fun setEventKey(value: String) {
        _eventKey = value
    }

    fun onKeyboardStateChange(isKeyboardOpen: Boolean?, status: ChatWindowStatus?) {
        when (isKeyboardOpen) {
            null -> {}
            false -> {

                _isMaximizeButtonVisible.postValue(true)
                _isCloseButtonVisible.postValue(true)

            }

            true -> {
                _isMaximizeButtonVisible.postValue(false)
                _isCloseButtonVisible.postValue(false)
            }
        }
    }

    fun onChatWindowStatusChange(status: ChatWindowStatus) {
        _chatWindowStatus.value = status
        //    MapSituationFragmentViewModel.getInstance().setMessageFragmentMode(status)
    }

    private val _keyboardStatus = MutableLiveData<KeyboardStatusEnum>()
    val keyboardStatus: LiveData<KeyboardStatusEnum> = _keyboardStatus
    fun onKeyboardStatusChange(status: KeyboardStatusEnum) {
        _keyboardStatus.value = status
    }


    fun removeChatNotifications(eventKey: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val userKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
            try {
                chatRepository.resetUnreadMessages(userKey, eventKey)
                val call =
                    notificationsRepository.removeNotificationsByChatroomKey(userKey, eventKey)
            } catch (ex: Exception) {
                val eee = 3
            }
        }
    }


}