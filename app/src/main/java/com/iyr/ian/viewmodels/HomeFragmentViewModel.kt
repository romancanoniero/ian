package com.iyr.ian.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.app.AppClass
import com.iyr.ian.dao.models.SpeedDialContact
import com.iyr.ian.dao.models.SpeedMessage
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.ChatRepository
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.dao.repositories.SpeedDialRepository
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ChatRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventFollowersRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SpeedDialRepositoryImpl
import com.iyr.ian.utils.avatarsviewgroup.AvatarGroupView
import com.iyr.ian.utils.chat.enums.MessagesStatus
import com.iyr.ian.utils.chat.models.Author
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragmentViewModel : ViewModel() {
    private var eventObservedKey: String? = null
    private var eventRepository: EventRepositoryImpl = EventRepositoryImpl()
    private var speedDialRepository: SpeedDialRepositoryImpl = SpeedDialRepositoryImpl()
    private var eventFollowersRepository: EventFollowersRepository = EventFollowersRepositoryImpl()
    private var chatRepository: ChatRepository = ChatRepositoryImpl()
    private val _viewers = MutableLiveData<ArrayList<AvatarGroupView.Avatar>>()
    val viewers: LiveData<ArrayList<AvatarGroupView.Avatar>> = _viewers

    private val _speedDialUpdates = MutableLiveData<SpeedDialRepository.DataEvent>()
    val speedDialUpdates: LiveData<SpeedDialRepository.DataEvent> = _speedDialUpdates

    private val _sendingContent = MutableLiveData<Resource<Boolean?>?>()
    val sendingContent: LiveData<Resource<Boolean?>?> = _sendingContent


    fun onObserveEventRequest(eventKey: String) {
        if (eventObservedKey == eventKey) return/*
                viewModelScope.launch(Dispatchers.IO) {
                    eventObservedKey = eventKey
                    eventRepository.getEventFlow(eventKey)?.collect { result ->
                        var viewersToShow = ArrayList<AvatarGroupView.Avatar>()
                        var viewers = result.data?.viewers?.values?.toList()
                        viewers?.forEach { viewer ->
                            var newAvatar = AvatarGroupView.Avatar()
                            newAvatar.setCaption(viewer.display_name.toString())
                            newAvatar.setImageSrc(viewer.profile_image_path.toString())

                            var extras = HashMap<String, String>()
                            extras["userKey"] = viewer.user_key.toString()
                            newAvatar.setExtras(extras)

                            viewersToShow.add(newAvatar)
                        }
                        _viewers.postValue(viewersToShow)
                    }
                }
        */
        viewModelScope.launch(Dispatchers.IO) {
            eventObservedKey = eventKey
            eventFollowersRepository.getEventFollowersFlow(eventKey)?.collect { result ->
                var viewersToShow = ArrayList<AvatarGroupView.Avatar>()/*
                                when (result) {
                                    is Resource.Loading -> {

                                    }

                                    is Resource.Success -> {
                                        var record = result.data!!
                                        when (record) {
                                            is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                                                var follower = (record.data!!)
                                                var newAvatar = AvatarGroupView.Avatar()

                                                newAvatar.setCaption(follower.display_name.toString())
                                                newAvatar.setImageSrc(follower.profile_image_path.toString())

                                                var extras = HashMap<String, String>()
                                                extras["userKey"] = follower.user_key.toString()
                                                newAvatar.setExtras(extras)

                                                viewersToShow.add(newAvatar)

                                                _viewers.postValue(viewersToShow)
                                            }

                                            is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {
                                                // Implementar
                                            }
                                            is EventFollowersRepository.EventFollowerDataEvent.OnChildMoved -> {
                                                // Implementar
                                            }
                                            is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> {
                                                // Implementar
                                            }

                                            is EventFollowersRepository.EventFollowerDataEvent.OnError -> {
                                            }

                                        }
                                        /*
                                                                var viewers = result.data?.viewers?.values?.toList()
                                                                viewers?.forEach { viewer ->
                                                                    var newAvatar = AvatarGroupView.Avatar()
                                                                    newAvatar.setCaption(viewer.display_name.toString())
                                                                    newAvatar.setImageSrc(viewer.profile_image_path.toString())
                                                                }
                                                                */
                                    }

                                    is Resource.Error -> {

                                    }

                                    is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> TODO()
                                    is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> TODO()
                                    is EventFollowersRepository.EventFollowerDataEvent.OnChildMoved -> TODO()
                                    is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> TODO()
                                    is EventFollowersRepository.EventFollowerDataEvent.OnError -> TODO()
                                    null -> TODO()
                                }
                                */

                /*
                              var viewers = result.data?.viewers?.values?.toList()
                              viewers?.forEach { viewer ->
                                  var newAvatar = AvatarGroupView.Avatar()
                                  newAvatar.setCaption(viewer.display_name.toString())
                                  newAvatar.setImageSrc(viewer.profile_image_path.toString())

                                  var extras = HashMap<String, String>()
                                  extras["userKey"] = viewer.user_key.toString()
                                  newAvatar.setExtras(extras)

                                  viewersToShow.add(newAvatar)
                              }
                              _viewers.postValue(viewersToShow)
              */


            }
        }


    }

    fun getEventObservedKey(): String? {
        return eventObservedKey
    }


    val speedDialFlow = liveData<Resource<ArrayList<SpeedDialContact>?>>(Dispatchers.IO) {
        speedDialRepository.getSpeedDialContactsFlow(FirebaseAuth.getInstance().uid.toString())
            ?.collect { dataEvent ->
                emit(dataEvent!!)
            }
    }

    /*
        fun listenSpeedDialContacts(userKey: String) {

            viewModelScope.launch(Dispatchers.IO) {
                speedDialRepository.getSpeedDialContactsFlow(userKey)?.collect { dataEvent ->
                    when (dataEvent) {
                        is SpeedDialRepository.DataEvent.OnChildAdded -> {
                            // Implementar

                        _speedDialUpdates.postValue(dataEvent!!)
                        }

                        is SpeedDialRepository.DataEvent.OnChildChanged -> {
                            // Implementar
                            _speedDialUpdates.postValue(dataEvent!!)

                        }

                        is SpeedDialRepository.DataEvent.OnChildRemoved -> {
                            // Implementar
                            _speedDialUpdates.postValue(dataEvent!!)
                        }

                        is SpeedDialRepository.DataEvent.OnError -> {
                            // Implementar
                            _speedDialUpdates.postValue(dataEvent!!)
                        }

                        is SpeedDialRepository.DataEvent.OnChildMoved -> {}
                        null -> {
                        }
                    }
                }
            }
        }
    */


    suspend fun onNewMediaMessage(eventKey: String, sender: User, mediaFile: MediaFile) {

        var call = chatRepository.generateMessageKey(eventKey)
        if (call.data != null) {
            var messageKey = call.data.toString()
            var previewMessage = createLocalMessage(eventKey, messageKey, sender, mediaFile)

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
                    previewMessage.video.url = filePath
                }
            } else if (previewMessage.voice != null) {
                val _messageEvent = ChatRepository.ChatDataEvent.OnChildAdded(previewMessage, null)
                var filePath = previewMessage.voice.url
                if (filePath.startsWith("/")) {
                    filePath = "file:$filePath"
                    previewMessage.voice.url = filePath
                }
            }

//            previewMessage.status = MessagesStatus.UPLOADING

            _sendingContent.postValue(Resource.Loading())

            var definitiveFileName =
                mediaFile.file_name.replace(AppClass.instance.cacheDir.toString() + "/", "")

            // Envio el mensaje al servidor.
            val result = chatRepository.sendMediaMessage(
                eventKey, sender.user_key, messageKey, mediaFile
            )
            if (result is Resource.Error<*> == false) {
                _sendingContent.postValue(Resource.Success<Boolean?>(true))
            } else {
                _sendingContent.postValue(Resource.Error<Boolean?>(result.message.toString()))

            }
        }
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


}

