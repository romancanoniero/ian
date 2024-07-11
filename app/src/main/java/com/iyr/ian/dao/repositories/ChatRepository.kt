package com.iyr.ian.dao.repositories


import com.iyr.ian.dao.models.UnreadMessages
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.flow.Flow

interface ChatRepositoryInterface {
    fun getChatFlow(eventKey: String): Flow<Resource<ChatRepository.ChatDataEvent>>?
    suspend fun sendTextMessage(
        eventKey: String,
        senderKey: String,
        messageKey: String,
        textMessage: String
    ): Resource<Boolean?>

    suspend fun sendMediaMessage(
        eventKey: String,
        senderKey: String,
        messageKey: String,
        mediaFile: MediaFile
    ): Resource<String?>


    suspend fun sendSpeedMessage(
        eventKey: String,
        userKey: String,
        messageKey: String,
        message: Message
    ): Resource<Boolean?>


    fun unreadMessagesFlow(userKey: String): Flow<List<UnreadMessages>>

    suspend fun resetUnreadMessages(userKey: String, eventKey: String): Resource<Boolean?>

    suspend fun generateMessageKey(eventKey: String): Resource<String>
}


abstract class ChatRepository : ChatRepositoryInterface {
    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = "chats"

    sealed class ChatDataEvent {
        data class OnChildAdded(val data: Message, val previousChildName: String?) : ChatDataEvent()
        data class OnChildChanged(val data: Message, val previousChildName: String?) :
            ChatDataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class OnChildRemoved(val data: Message) : ChatDataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class OnChildMoved(val data: Message, val previousChildName: String?) : ChatDataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved

        data class OnError(val exception: Exception) : ChatDataEvent()
    }


}