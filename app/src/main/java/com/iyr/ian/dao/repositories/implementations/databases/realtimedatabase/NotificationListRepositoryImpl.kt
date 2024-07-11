package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iyr.ian.dao.models.NotificationList
import com.iyr.ian.dao.repositories.NotificationListRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

class NotificationListRepositoryImpl : NotificationListRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference =
        FirebaseDatabase.getInstance().getReference(tableName)


    /*

suspend fun DatabaseReference.singleValueEvent(): SingleEventResponse = suspendCoroutine { continuation ->
    val valueEventListener = object: ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            continuation.resume(EventResponse.Cancelled(error))
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            continuation.resume(EventResponse.Changed(snapshot))
        }
    }
    addListenerForSingleValueEvent(valueEventListener) // Subscribe to the event
}
 */
    override suspend fun listNotificationList(userKey: String): Resource<ArrayList<NotificationList>?> {

        return try {

            val result = FirebaseDatabase.getInstance()
                .getReference(tableName)
                .child(FirebaseAuth.getInstance().uid.toString()).get().await()

            val toReturn = ArrayList<NotificationList>()
            result.children.forEach { childSnapshot ->
                var newItem : NotificationList = childSnapshot.getValue(NotificationList::class.java)!!
                newItem.list_key = childSnapshot.key.toString()
                toReturn.add(newItem)
            }
            Resource.Success<ArrayList<NotificationList>?>(toReturn)
        } catch (exception: Exception) {
            Resource.Error<ArrayList<NotificationList>?>(exception.message.toString())
        }

   }
}