package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.dao.repositories.ContactsRepository
import com.iyr.ian.ui.friends.enums.FriendshipStatusEnums
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.dao.ErrorResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContactsRepositoryImpl : ContactsRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)

    private val nonUserInvitedRepository = NonUsersInvitedRepositoryImpl()

    override suspend fun putOnPendingInvitationList(
        userWhoInviteKey: String, phoneNumber: String
    ): Boolean {
        try {
            FirebaseDatabase.getInstance().getReference(tableName).child(phoneNumber)
                .setValue(userWhoInviteKey).await()

            return true
        } catch (exception: Exception) {
            return false
        }

    }

    override suspend fun postContactInvitation(
        userWhoInvitesKey: String, userToInviteKey: String
    ): Resource<Boolean?> {

        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_from_key"] = userWhoInvitesKey
                data["user_to_key"] = userToInviteKey
                try {
                    FirebaseFunctions.getInstance().getHttpsCallable("contactInvite").call(data)
                        .await()

                    Resource.Success(true)
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


    override suspend fun searchContacts(
        myKey: String, searchString: String
    ): Resource<ArrayList<Contact>?> {
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = FirebaseAuth.getInstance().uid.toString()
                data["search_text"] = searchString
                try {
                    var contactsFound = ArrayList<Contact>()
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("searchUserByMultipleMethods").call(data).await()
                    if (call.data != null) {
                        var responseAsString: String =
                            (call.data as HashMap<String, Any>).get("data").toString()
                        var type = object : TypeToken<ArrayList<Contact>?>() {}.type
                        contactsFound =
                            Gson().fromJson<ArrayList<Contact>?>(responseAsString!!, type)
                        Resource.Success<ArrayList<Contact>?>(contactsFound)
                    } else {
                        Resource.Error<ArrayList<Contact>?>("Error: Error looking for contacts")
                    }
                } catch (exception: Exception) {
                    Resource.Error<ArrayList<Contact>?>(exception.message.toString())
                }
            } else {
                Resource.Error<ArrayList<Contact>?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<ArrayList<Contact>?>(exception.message.toString())
        }

    }


    override suspend fun searchContactsFromPhone(
        myKey: String, searchString: String
    ): Resource<ArrayList<Contact>?> {
        return try {
            val contactsFound = ArrayList<Contact>()
            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            val displayNameQuery =
                databaseReference.orderByChild("display_name").startAt(searchString)
                    .endAt(searchString + "\uf8ff").get().await()
            val emailAddressQuery =
                databaseReference.orderByChild("email_address").startAt(searchString)
                    .endAt(searchString + "\uf8ff").get().await()
            val phoneNumberQuery =
                databaseReference.orderByChild("phone_number").startAt(searchString)
                    .endAt(searchString + "\uf8ff").get().await()

            displayNameQuery.children.forEach { snapshot ->
                val contact = snapshot.getValue(Contact::class.java)
                contact?.let { contactsFound.add(it) }
            }

            emailAddressQuery.children.forEach { snapshot ->
                val contact = snapshot.getValue(Contact::class.java)
                contact?.let { contactsFound.add(it) }
            }

            phoneNumberQuery.children.forEach { snapshot ->
                val contact = snapshot.getValue(Contact::class.java)
                contact?.let { contactsFound.add(it) }
            }

            contactsFound.forEach { it.status = FriendshipStatusEnums.NOT_A_FRIEND_BUT_EXISTS.name }

            // ahora debo recorrer los contactos y verificar si ya son contactos o si son usuarios
            var contactsReference = FirebaseDatabase.getInstance().getReference("users_contacts")
            contactsReference.child(myKey).get().await().children.forEach { snapshot ->
                val contact = snapshot.getValue(Contact::class.java)
                contact?.let {
                    var index = contactsFound.indexOfFirst { it.user_key == contact.user_key }
                    if (index != -1) {

                        contactsFound?.removeAt(index)
                    }
                }
            }

            Resource.Success<ArrayList<Contact>?>(contactsFound)
        } catch (exception: Exception) {
            Resource.Error<ArrayList<Contact>?>(exception.message.toString())
        }

    }


    override suspend fun alreadyFriends(userKey: String, otherUserKey: String): Resource<Boolean?> {
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            var call: HttpsCallableResult? = null
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = userKey
                data["user_from_key"] = otherUserKey
                try {
                    call =
                        FirebaseFunctions.getInstance().getHttpsCallable("alreadyContacted")
                            .call(data)
                            .await()
                    val hashResponse = call.data as HashMap<String, Any>
                    val status = hashResponse.get("status") as Int
                    if (status == 200) {
                        // crea un type token paraa interpretar el resultado
                        /*
                                                val typeToken = object : TypeToken<HashMap<String, Contact>?>() {}.type
                                                val data = Gson().fromJson<HashMap<String, Contact>>(
                                                    hashResponse.get("data").toString(),
                                                    typeToken
                                                )
                        */
                        val response = hashResponse.get("data") as Boolean
                        return Resource.Success<Boolean?>(response)
                    } else {
                        val hash_response = hashResponse.get("data").toString()
                        val error_response = Gson().fromJson<ErrorResponse>(
                            hash_response,
                            ErrorResponse::class.java
                        ).error
                        when (error_response) {
                            "error_accepting_contact" -> {
                                return Resource.Error<Boolean?>("error_accepting_contact")
                            }

                            "error_getting_token" -> {
                                return Resource.Error<Boolean?>("error_getting_token")
                            }

                            else -> {
                                return Resource.Error<Boolean?>(error_response)
                            }
                        }

                    }

                } catch (exception: Exception) {
                    call?.data?.toString()
                    return Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                return Resource.Error<Boolean?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error(exception.message.toString())
        }
    }

    override suspend fun contactAcceptInvitation(
        userWhoAccept: String, userToAcceptKey: String
    ): Resource<HashMap<String, Contact>?> {
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            var call: HttpsCallableResult? = null
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = userWhoAccept
                data["user_from_key"] = userToAcceptKey

                try {
                    call =
                        FirebaseFunctions.getInstance().getHttpsCallable("contactAccept").call(data)
                            .await()
                    val hashResponse = call.data as HashMap<String, Any>
                    val status = hashResponse.get("status") as Int
                    if (status == 200) {
                        // crea un type token paraa interpretar el resultado

                        val typeToken = object : TypeToken<HashMap<String, Contact>?>() {}.type
                        val data = Gson().fromJson<HashMap<String, Contact>>(
                            hashResponse.get("data").toString(),
                            typeToken
                        )

                        return Resource.Success<HashMap<String, Contact>?>(data)
                    } else {
                        val hash_response = hashResponse.get("data").toString()
                        val error_response = Gson().fromJson<ErrorResponse>(
                            hash_response,
                            ErrorResponse::class.java
                        ).error
                        when (error_response) {
                            "error_accepting_contact" -> {
                                return Resource.Error<HashMap<String, Contact>?>("error_accepting_contact")
                            }

                            "error_getting_token" -> {
                                return Resource.Error<HashMap<String, Contact>?>("error_getting_token")
                            }

                            else -> {
                                return Resource.Error<HashMap<String, Contact>?>(error_response)
                            }
                        }

                    }
                    /*
                    if (call.data != null) {
                        return Resource.Success<Boolean?>(true)
                    } else {
                        return Resource.Error<Boolean?>("error_accepting_contact")
                    }
                    */
                } catch (exception: Exception) {
                    call?.data?.toString()
                    return Resource.Error<HashMap<String, Contact>?>(exception.message.toString())
                }
            } else {
                return Resource.Error<HashMap<String, Contact>?>("error_getting_token")
            }
        } catch (exception: Exception) {
            return Resource.Error(exception.message.toString())
        }
    }


    /***
     * Cancela una invitacion de contacto
     * @param userKey   Usuario que cancela la invitacion
     * @param userInvitedKey Usuario al que se le cancela la invitacion
     * @return Resource<Boolean?>
     */
    override suspend fun contactCancelInvitation(
        userKey: String,
        userInvitedKey: String
    ): Resource<Boolean?> {
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = userKey
                data["user_from_key"] = userInvitedKey

                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("contactInvitationCancel")
                            .call(data)
                            .await()
                    if (call.data != null) {
                        return Resource.Success<Boolean?>(true)
                    } else {
                        return Resource.Error<Boolean?>("error_accepting_contact")
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


    /***
     * Cancela una amistad
     * @param userKey   Usuario que cancela la amistad
     * @param userFriendshipToRevokeKey Usuario al que se le cancela la amistad
     * @return Resource<Boolean?>
     */
    override suspend fun contactCancelFriendship(
        userKey: String,
        userFriendshipToRevokeKey: String
    ): Resource<Boolean?> {
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = userKey
                data["user_to_exclude_key"] = userFriendshipToRevokeKey

                try {
                    var call =
                        FirebaseFunctions.getInstance().getHttpsCallable("contactBreak").call(data)
                            .await()

                    nonUserInvitedRepository.cancelNonUserInvitation(
                        userKey,
                        userFriendshipToRevokeKey
                    )

                    if (call.data != null) {
                        return Resource.Success<Boolean?>(true)
                    } else {
                        return Resource.Error<Boolean?>("error_accepting_contact")
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


    override suspend fun contactsByUserFlow(userKey: String): Flow<DataEvent> = callbackFlow {

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val data = dataSnapshot.getValue(Contact::class.java)
                data?.let {
                    data.user_key = dataSnapshot.key.toString()
                    trySend(ContactsRepository.DataEvent.ChildAdded(data, previousChildName))

                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val data = dataSnapshot.getValue(Contact::class.java)
                data?.let {
                    data.user_key = dataSnapshot.key.toString()
                    trySend(ContactsRepository.DataEvent.ChildChanged(data, previousChildName))
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(Contact::class.java)
                data?.let {
                    data.user_key = dataSnapshot.key.toString()
                    trySend(ContactsRepository.DataEvent.ChildRemoved(data))
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

                val data = dataSnapshot.getValue(Contact::class.java)
                data?.let {
                    data.user_key = dataSnapshot.key.toString()
                    trySend(ContactsRepository.DataEvent.onChildMoved(data, previousChildName))
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

    override suspend fun contactsByUserListFlow(userKey: String): Flow<ArrayList<Contact>> =
        callbackFlow {

            val eventLister = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = ArrayList<Contact>()
                    snapshot.children.forEach { childSnapshot ->
                        val data = childSnapshot.getValue(Contact::class.java)
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

    override suspend fun inviteByTelephone(
        user: UserMinimum, telephoneNumber: String
    ): Resource<Contact?> {
        return try {
            val newObject = Contact()
            newObject.user_key = telephoneNumber
            newObject.telephone_number = telephoneNumber
            newObject.status = FriendshipStatusEnums.USER_NOT_FOUND.name
            databaseReference.child(user.user_key).child(telephoneNumber).setValue(newObject)
                .await()

            nonUserInvitedRepository.addNonUserInvitation(telephoneNumber, user)
            Resource.Success<Contact?>(newObject)
        } catch (exception: Exception) {
            Resource.Error<Contact?>(exception.message.toString())
        }
    }


    override suspend fun inviteByEmail(user: UserMinimum, email: String): Resource<Contact?> {
        return try {

            val newObject = Contact()
            newObject.user_key = email
            newObject.email = email
            newObject.status = FriendshipStatusEnums.USER_NOT_FOUND.name
            databaseReference.child(user.user_key).child(email).setValue(newObject).await()

            nonUserInvitedRepository.addNonUserInvitation(email, user)

            Resource.Success<Contact?>(newObject)
        } catch (exception: Exception) {
            Resource.Error<Contact?>(exception.message.toString())
        }
    }


    override suspend fun selectUnselectContactGroup(
        userKey: String,
        contact: Contact,
        group: ContactGroup,
        included: Boolean
    ): Resource<Boolean?> {
        return try {

            if (included) {
                databaseReference.child(userKey).child(contact.user_key.toString()).child("groups")
                    .child(group.list_key)
                    .setValue(group).await()

                val call =
                    NotificationListRepositoryImpl().addContactToGroup(userKey, contact, group)
                if (call is Resource.Error) {
                    throw Exception(call.message.toString())
                }


            } else {
                databaseReference.child(userKey).child(contact.user_key.toString()).child("groups")
                    .child(group.list_key)
                    .removeValue().await()

                val call = NotificationListRepositoryImpl().removeContactFromGroup(
                    userKey,
                    contact.user_key.toString(),
                    group.list_key.toString()
                )
                if (call is Resource.Error) {
                    throw Exception(call.message.toString())
                }
            }

            Resource.Success<Boolean?>(true)
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }

    }

}