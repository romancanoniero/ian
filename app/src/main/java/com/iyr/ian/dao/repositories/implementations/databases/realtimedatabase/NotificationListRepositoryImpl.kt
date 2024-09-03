package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.dao.repositories.NotificationListRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationListRepositoryImpl : NotificationListRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)


    override suspend fun contactsGroupsByUserListFlow(userKey: String): Flow<ArrayList<ContactGroup>> =
        callbackFlow {

            val eventLister = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = ArrayList<ContactGroup>()
                    snapshot.children.forEach { childSnapshot ->
                        val data = childSnapshot.getValue(ContactGroup::class.java)
                        list.add(data!!)
                    }

                    trySend(list)

                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            databaseReference.child(userKey).addValueEventListener(eventLister)

            awaitClose() {
                databaseReference.child(userKey).removeEventListener(eventLister)
            }
        }


    override suspend fun contactsGroupsByUserFlow(userKey: String): Flow<NotificationListRepository.DataEvent> =
        callbackFlow {
            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    val data = dataSnapshot.getValue(ContactGroup::class.java)
                    data?.let {
                        data.list_key = dataSnapshot.key.toString()
                        trySend(
                            NotificationListRepository.DataEvent.ChildAdded(
                                data, previousChildName
                            )
                        )

                    }
                }

                override fun onChildChanged(
                    dataSnapshot: DataSnapshot, previousChildName: String?
                ) {
                    val data = dataSnapshot.getValue(ContactGroup::class.java)
                    data?.let {
                        data.list_key = dataSnapshot.key.toString()
                        trySend(
                            NotificationListRepository.DataEvent.ChildChanged(
                                data, previousChildName
                            )
                        )
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    val data = dataSnapshot.getValue(ContactGroup::class.java)
                    data?.let {
                        data.list_key = dataSnapshot.key.toString()
                        trySend(NotificationListRepository.DataEvent.ChildRemoved(data))
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    val data = dataSnapshot.getValue(ContactGroup::class.java)
                    data?.let {
                        data.list_key = dataSnapshot.key.toString()
                        trySend(
                            NotificationListRepository.DataEvent.onChildMoved(
                                data, previousChildName
                            )
                        )
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


    override suspend fun listNotificationList(userKey: String): Resource<ArrayList<ContactGroup>?> {

        return try {

            val result = FirebaseDatabase.getInstance().getReference(tableName)
                .child(FirebaseAuth.getInstance().uid.toString()).get().await()

            val toReturn = ArrayList<ContactGroup>()
            result.children.forEach { childSnapshot ->
                var newItem: ContactGroup = childSnapshot.getValue(ContactGroup::class.java)!!
                newItem.list_key = childSnapshot.key.toString()
                toReturn.add(newItem)
            }
            Resource.Success<ArrayList<ContactGroup>?>(toReturn)
        } catch (exception: Exception) {
            Resource.Error<ArrayList<ContactGroup>?>(exception.message.toString())
        }

    }

    suspend fun searchGroupByName(groupName: String): Resource<ContactGroup?> {
        return try {
            val dataSnapshot =
                databaseReference.child(firebaseAuth.uid.toString()).orderByChild("list_name")
                    .equalTo(groupName).get().await()


            if (dataSnapshot.childrenCount.toInt() == 1) {
                var contactGroup: ContactGroup? = null
                dataSnapshot.children.forEach { childSnapShot ->
                    contactGroup = childSnapShot.getValue(ContactGroup::class.java)!!
                    contactGroup?.list_key = childSnapShot.key.toString()
                }
                return Resource.Success<ContactGroup?>(contactGroup)

            } else {
                Resource.Success<ContactGroup?>(null)
            }
        } catch (exception: Exception) {
            Resource.Error<ContactGroup?>(exception.message.toString())
        }
    }

    override suspend fun insertGroup(groupName: String): Resource<ContactGroup> {
        return try {
            if (searchGroupByName(groupName).data != null) {
                return Resource.Error<ContactGroup>("group_already_exists")
            } else {
                val key = databaseReference.push().key
                val group = ContactGroup()
                group.list_key = key.toString()
                group.list_name = groupName
                Resource.Loading<ContactGroup>(group)
                databaseReference.child(firebaseAuth.uid.toString()).child(key.toString())
                    .setValue(group).await()
                Resource.Success<ContactGroup>(group)
            }
        } catch (e: Exception) {
            Resource.Error<ContactGroup>(e.message.toString())
        }

    }

    override suspend fun deleteGroup(ownerKey: String, listKey: String): Resource<Boolean?> {
        return try {
            try {
                val call = databaseReference.child(ownerKey).child(listKey).removeValue().await()
                Resource.Success<Boolean?>(true)
            } catch (e: Exception) {
                Resource.Error<Boolean?>(e.message.toString())
            }
        } catch (e: Exception) {
            Resource.Error<Boolean?>(e.message.toString())
        }

    }


    /***
     * Agrega un contacto al grupo
     */
    override suspend fun addContactToGroup(
        userKey: String, contact: Contact, group: ContactGroup
    ): Resource<Boolean?> {
        return try {
            try {
                val call = databaseReference.child(userKey).child(group.list_key.toString())
                    .child("members").child(contact.user_key.toString()).setValue(contact).await()
                Resource.Success<Boolean?>(true)
            } catch (e: Exception) {
                Resource.Error<Boolean?>(e.message.toString())
            }
        } catch (e: Exception) {
            Resource.Error<Boolean?>(e.message.toString())
        }
    }


    /***
     * Remueve un contacto del grupo
     */
    override suspend fun removeContactFromGroup(
        userKey: String, contactKey: String, groupKey: String
    ): Resource<Boolean?> {
        return try {
            try {
                val call = databaseReference.child(userKey).child(groupKey).child("members")
                    .child(contactKey).removeValue().await()
                Resource.Success<Boolean?>(true)
            } catch (e: Exception) {
                Resource.Error<Boolean?>(e.message.toString())
            }
        } catch (e: Exception) {
            Resource.Error<Boolean?>(e.message.toString())
        }


    }


}