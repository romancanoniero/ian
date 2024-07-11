package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.repositories.ContactsRepository
import com.iyr.ian.ui.friends.enums.FriendshipStatusEnums
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContactsRepositoryImpl : ContactsRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference =
        FirebaseDatabase.getInstance().getReference(tableName)


    override suspend fun putOnPendingInvitationList(
        userWhoInviteKey: String,
        phoneNumber: String
    ): Boolean {
        try {
            FirebaseDatabase.getInstance()
                .getReference(tableName)
                .child(phoneNumber)
                .setValue(userWhoInviteKey)
                .await()

            return true
        } catch (exception: Exception) {
            return false
        }

    }

    override suspend fun postContactInvitation(
        userWhoInvitesKey: String,
        userToInviteKey: String
    ): Resource<Boolean?> {

        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_from_key"] = userWhoInvitesKey
                data["user_to_key"] = userToInviteKey
                try {
                    FirebaseFunctions.getInstance()
                        .getHttpsCallable("contactInvite")
                        .call(data).await()

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
        myKey: String,
        searchString: String
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
                        .getHttpsCallable("searchUserByMultipleMethods")
                        .call(data).await()
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
        myKey: String,
        searchString: String
    ): Resource<ArrayList<Contact>?> {
        return   try {
                val contactsFound = ArrayList<Contact>()
                val databaseReference = FirebaseDatabase.getInstance().getReference("users")
                val displayNameQuery =
                    databaseReference.orderByChild("display_name").startAt(searchString).endAt(searchString + "\uf8ff").get()
                        .await()
                val emailAddressQuery =
                    databaseReference.orderByChild("email_address").startAt(searchString).endAt(searchString + "\uf8ff").get()
                        .await()
                val phoneNumberQuery =
                    databaseReference.orderByChild("phone_number").startAt(searchString).endAt(searchString + "\uf8ff").get().await()

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


    override suspend fun contactAcceptInvitation(userWhoAccept: String,userToAcceptKey: String): Resource<Boolean?> {
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"]    = token.token.toString()
                data["user_key"]      = userWhoAccept
                data["user_from_key"] = userToAcceptKey

                try {
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("contactAccept")
                        .call(data).await()
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

        /*
        FirebaseAuth.getInstance().currentUser!!.getIdToken(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result!!.token.toString()

                    val data: MutableMap<String, Any> = HashMap()
                    data["auth_token"] = idToken
                    data["user_key"] = FirebaseAuth.getInstance().uid.toString()
                    data["user_from_key"] = userKey


                    FirebaseFunctions.getInstance()
                        .getHttpsCallable("contactAccept")
                        .call(data)
                        .continueWith(Continuation<HttpsCallableResult, String?> { task -> // This continuation runs on either success or failure, but if the task
                            val result: HashMap<String, Any?> =
                                task.result!!.data as HashMap<String, Any?>

                            callback.onComplete(true, null).toString()

                        }).addOnFailureListener(OnFailureListener { e ->
                            callback.onError(e)
                        })

                } else {
                    callback.onError(task.exception!!)
                }
            }
*/


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

}