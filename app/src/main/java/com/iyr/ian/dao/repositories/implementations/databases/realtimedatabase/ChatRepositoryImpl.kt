package com.iyr.ian.repository.implementations.databases.realtimedatabase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.iyr.ian.AppConstants.Companion.CHAT_FILES_STORAGE_PATH
import com.iyr.ian.dao.models.UnreadMessages
import com.iyr.ian.dao.repositories.ChatRepository
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await

class ChatRepositoryImpl : ChatRepository() {

    private var chatRoomListener: ChildEventListener? = null
    private var chatRoomReference: DatabaseReference? = null

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference =
        FirebaseDatabase.getInstance().getReference(tableName)

    override fun getChatFlow(eventKey: String): Flow<Resource<ChatDataEvent>> =
        callbackFlow {
            var chatFilestPath= CHAT_FILES_STORAGE_PATH + eventKey +"/"


            chatRoomReference = databaseReference.child(eventKey)
            chatRoomListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = Message(snapshot.value as java.util.HashMap<String, Any>)
                    message.id = snapshot.key!!

                    // actualizo la ruta de los archivos multimedia.
                    when
                    {
                        message.image != null -> {
                            if (!message.image.url.startsWith(chatFilestPath)) {
                                message.image!!.url = chatFilestPath + message.image!!.url
                            }
                        }
                        message.video != null -> {
                            if (!message.video.url.startsWith(chatFilestPath)) {
                                message.video!!.url = chatFilestPath + message.video!!.url
                            }
                        }
                        message.voice != null -> {
                            if (!message.voice.url.startsWith(chatFilestPath)) {
                                message.voice!!.url = chatFilestPath + message.voice!!.url
                            }
                        }

                    }

                    var toReturn = Resource.Success<ChatDataEvent>(
                        ChatDataEvent.OnChildAdded(
                            message, null
                        )
                    )

                    Log.d(
                        "FLOW_REPOSITORY",
                        "emito el registo = ${(toReturn.data!! as ChatDataEvent.OnChildAdded).data.text}"
                    )

                    trySend(toReturn)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    //   TODO("Not yet implemented")
                    var pp = 33
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    //            TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    //          TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    //        TODO("Not yet implemented")
                }
            }

            chatRoomReference?.addChildEventListener(chatRoomListener!!)

            awaitClose {
                chatRoomReference?.removeEventListener(chatRoomListener!!)
            }
        }

    private fun snapshotToMessage(snapshot: DataSnapshot): Message {
        val hashMap: HashMap<String, Any> = snapshot.value as HashMap<String, Any>

        return Message(hashMap)

        /*

            val _messageId: String = hashMap.get("id").toString()
            val _createdAt: Date =  Date((hashMap.get("createdAt") as Long))

            val _authorHashmap = hashMap.get("author") as HashMap<String, Any>
            val _authorId: String = _authorHashmap.get("id").toString()
            val _authorName: String = _authorHashmap.get("name").toString()
            val _authorAvatar: String = _authorHashmap.get("avatar").toString()
            val _authorOnline: Boolean = (_authorHashmap.get("online") ?: false) as Boolean

            val _text: String = hashMap.get("text").toString()

            val author = Author(_authorId, _authorName, _authorAvatar, _authorOnline)


            val message: Message = Message(_messageId, author, _text, _createdAt)

            if ( hashMap.containsKey("image"))
            {
                val image :
            }
          */
        //   return message

    }


    override suspend fun generateMessageKey(eventKey: String): Resource<String> {
        return try {
            var call = FirebaseDatabase.getInstance().getReference(tableName)
                .child(eventKey.toString()).push().key

            Resource.Success<String>(call.toString())

        } catch (exception: Exception) {
            Resource.Error<String>(exception.message.toString())
        }
    }

    override suspend fun sendTextMessage(
        eventKey: String, senderKey: String, messageKey: String, textMessage: String
    ): Resource<Boolean?> {
        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = tokenResult.token.toString()
                data["event_key"] = eventKey
                data["sender_key"] = senderKey
                data["message_key"] = messageKey
                data["message"] = textMessage
                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("messageSend").call(data)
                            .await()

                    Resource.Success<Boolean?>(true)

                } catch (exception: Exception) {
                    Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }


    override suspend fun sendMediaMessage(
        eventKey: String, senderKey: String, messageKey: String, mediaFile: MediaFile
    ): Resource<String?> {
        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = tokenResult.token.toString()
                data["event_key"] = eventKey
                data["user_key"] = senderKey
                data["message_key"] = messageKey
                data["media_file"] = Gson().toJson(mediaFile)
                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("messageFileSend")
                            .call(data)
                            .await()
                    Log.d("RESULT", Gson().toJson(call.data))

                    var result = (call.data as HashMap<String, Any>)
                    when (result["status"]) {
                        200 -> {
                            return Resource.Success<String?>(messageKey.toString())
                        }

                        else -> {
                            return Resource.Error<String?>(
                                result["message"].toString(),
                                messageKey.toString()
                            )
                        }

                    }


                } catch (exception: Exception) {
                    return Resource.Error<String?>(
                        exception.message.toString(),
                        messageKey.toString()
                    )
                }
            } else {
                return Resource.Error<String?>("error_getting_token", messageKey.toString())
            }
        } catch (exception: Exception) {
            return Resource.Error<String?>(exception.message.toString(), messageKey.toString())
        }
    }

    override suspend fun sendSpeedMessage(
        eventKey: String,
        userKey: String,
        messageKey: String,
        message: Message
    ): Resource<Boolean?> {

        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = tokenResult.token.toString()
                data["event_key"] = eventKey
                data["user_key"] = userKey
                data["message_key"] = messageKey
                data["message"] = Gson().toJson(message) //message
                try {

                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("messageSpeedMessageSend")
                            .call(data)
                            .await()

                    return Resource.Success<Boolean?>(true)

                } catch (exception: Exception) {
                    return Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                return Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error<Boolean?>(exception.message.toString())
        }


    }


    override suspend fun resetUnreadMessages(
        userKey: String,
        eventKey: String
    ): Resource<Boolean?> {
        return try {
            var tokenResult = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (tokenResult != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = tokenResult.token.toString()
                data["event_key"] = eventKey
                data["user_key"] = userKey
                try {
                    var call =
                        FirebaseFunctions.getInstance()
                            .getHttpsCallable("resetUnReadMessagesCounter")
                            .call(data)
                            .await()
                    Log.d("RESULT", Gson().toJson(call.data))

                    var result = (call.data as HashMap<String, Any>)
                    when (result["status"]) {
                        0 -> {
                            return Resource.Success<Boolean?>(true)
                        }

                        else -> {
                            return Resource.Error<Boolean?>(
                                result["message"].toString(),
                                false
                            )
                        }

                    }
                } catch (exception: Exception) {
                    return Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                return Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error<Boolean?>(exception.message.toString())
        }
    }


    override fun unreadMessagesFlow(userKey: String): Flow<List<UnreadMessages>> = callbackFlow {
        val unreadMessagesReference =
            FirebaseDatabase.getInstance().getReference("chats_unreads_by_user").child(userKey)

        val chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var unreads = ArrayList<UnreadMessages>()

                if (snapshot.exists()) {
                    snapshot.children.forEach { snapshot ->
                        var record: UnreadMessages = snapshot.getValue(UnreadMessages::class.java)!!
                        record.chat_room_key = snapshot.key.toString()
                        unreads.add(record)
                    }
                    trySend(unreads)
                }
                trySend(unreads)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        unreadMessagesReference.addValueEventListener(chatListener)

        awaitClose {
            unreadMessagesReference.removeEventListener(chatListener)
        }
    }.conflate()



    override fun unreadMessagesFlow(userKey: String, eventKey: String): Flow<Long> = callbackFlow {
        val unreadMessagesReference =
            FirebaseDatabase.getInstance().getReference("chats_unreads_by_user").child(userKey).child(eventKey)

        val chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var unreads : Long = 0

                if (snapshot.exists()) {

                    val result = snapshot.getValue(UnreadMessages::class.java)
                    unreads = (result?.qty as Long).toInt().toLong()
                    trySend(unreads)
                }
                trySend(unreads)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        unreadMessagesReference.addValueEventListener(chatListener)

        awaitClose {
            unreadMessagesReference.removeEventListener(chatListener)
        }
    }.conflate()
}