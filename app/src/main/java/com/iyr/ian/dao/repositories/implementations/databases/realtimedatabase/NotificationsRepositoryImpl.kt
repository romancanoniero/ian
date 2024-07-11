package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iyr.ian.AppConstants
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.dao.repositories.NotificationsRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationsRepositoryImpl : NotificationsRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference =
        FirebaseDatabase.getInstance().getReference(tableName)


    /**
     * Forma correcta de emitir datos desde Firebase Realtime Database
     */
    @ExperimentalCoroutinesApi
    override suspend fun getDataFlow(userKey: String) : Flow<DataEvent> = callbackFlow {

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val data = dataSnapshot.getValue(EventNotificationModel::class.java)
                data?.let {
                    data.notification_key = dataSnapshot.key.toString()
                    trySend(DataEvent.ChildAdded(data, previousChildName))

                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val data = dataSnapshot.getValue(EventNotificationModel::class.java)
                data?.let {
                    data.notification_key = dataSnapshot.key.toString()
                    trySend(DataEvent.ChildChanged(data, previousChildName))
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(EventNotificationModel::class.java)
                data?.let {
                    data.notification_key = dataSnapshot.key.toString()
                    trySend(DataEvent.ChildRemoved(data))
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

                val data = dataSnapshot.getValue(EventNotificationModel::class.java)
                data?.let {
                    data.notification_key = dataSnapshot.key.toString()
                    trySend(DataEvent.onChildMoved(data, previousChildName))
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }

        databaseReference.child(userKey).addChildEventListener(childEventListener)

        awaitClose {
            databaseReference.child(userKey).removeEventListener(childEventListener)
        }
    }

    override suspend fun onMessageRead(
        userKey: String,
        eventKey: String,
        messageKey: String
    ): Resource<Boolean?> {
        var notificationRef = databaseReference.child(userKey)
        var myNotifications = notificationRef.get().await()
        if (myNotifications.exists()) {
            myNotifications.children.forEach { record ->
                var notification = record.getValue(EventNotificationModel::class.java)!!
                notification.notification_key = record.key.toString();
                if (notification.notification_type == AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE && notification.event_key == eventKey) {
                    var messagesArray =
                        notification.event_data?.get("messages") as ArrayList<HashMap<String, Any>>




                    if (messagesArray.size > 1) {

                        var messagesArrayIndex = -1
                        messagesArray.forEach { message ->
                            messagesArrayIndex++
                            try {
                                if (message["message_key"] == messageKey) {

                                    var removeMessageTask = notificationRef
                                        .child(record.key.toString())
                                        .child("event_data")
                                        .child("messages")
                                        .child(messagesArrayIndex.toString())
                                        .removeValue().await()

                                    var updateQtyTask = null


                                    if (removeMessageTask != null && updateQtyTask != null) {
                                        Resource.Success<Boolean?>(true)
                                    } else {
                                        Resource.Error<Boolean?>("error_updating_qty")
                                    }
                                    return@forEach
                                }
                                return@forEach

                            } catch (exception: Exception) {
                                Resource.Error<Boolean?>(exception.message.toString())
                            }
                        }



                        notificationRef.child(record.key.toString())
                            .child("qty")
                            .setValue(messagesArray.size - 1).await()
                    } else {
                        notificationRef.child(record.key.toString()).removeValue()
                            .await()
                    }




                }
            }
        }
        return Resource.Success(false)
    }


    override suspend fun onAllMessagesRead(
        userKey: String,
        eventKey: String
    ): Resource<Boolean?> {
        var notificationRef = databaseReference.child(userKey)
        var myNotifications = notificationRef.get().await()
        if (myNotifications.exists()) {
            myNotifications.children.forEach { record ->
                var notification = record.getValue(EventNotificationModel::class.java)!!
                if (notification.notification_type == AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE && notification.event_key == eventKey) {
                    var removeMessageTask = notificationRef.child(notification.notification_key)
                        .removeValue().await()

                    if (removeMessageTask != null) {
                        Resource.Success<Boolean?>(true)
                    } else {
                        Resource.Error<Boolean?>("error_updating_qty")
                    }
                }
            }
        }
        return Resource.Success(false)
    }


    override fun updateNotificationStatusByUserKey(
        userKey: String,
        status: String
    ): Resource<Boolean?> {
        return try {
            var notificationRef = databaseReference.child(userKey)
            var myNotifications = notificationRef.addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.children.forEach { record ->
                            var notification =
                                record.getValue(EventNotificationModel::class.java)!!
                            if (notification.notification_type == AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE) {
                                var updateStatusTask = notificationRef
                                    .child(record.key.toString())
                                    .child("status")
                                    .setValue(status)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Resource.Error<Boolean?>(error.message)
                }
            })
            Resource.Success(false)
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }



    override suspend fun registerNotificationsToken(token: String): Resource<Boolean?> {
        return try {

            var call = FirebaseDatabase.getInstance().getReference("users_notification_tokens")
                .child(FirebaseAuth.getInstance().uid.toString())
                .setValue(token).await()

            if (call != null)
                Resource.Success<Boolean?>(true)
            else
                Resource.Error<Boolean?>("error")

        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }


}