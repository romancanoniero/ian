package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iyr.ian.dao.models.SpeedDialContact
import com.iyr.ian.dao.repositories.SpeedDialRepository
import com.iyr.ian.utils.PhoneContact
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SpeedDialRepositoryImpl : SpeedDialRepository() {

    override suspend fun addSpeedDialContacts(contacts: List<PhoneContact>): Resource<List<SpeedDialContact>> {
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = FirebaseAuth.getInstance().uid.toString()
                data["contacts"] = Gson().toJson(contacts)
                try {
                  var response =   FirebaseFunctions.getInstance()
                        .getHttpsCallable("speedDialAddContacts")
                        .call(data).await()

                    val listType = object : TypeToken<List<SpeedDialContact>>() {}.type
                    val theList: List<SpeedDialContact> = Gson().fromJson((response.data as HashMap<String, Any>)["data"].toString(), listType)
                   Resource.Success<List<SpeedDialContact>>(theList)
                } catch (exception: Exception) {
                    Resource.Error<List<SpeedDialContact>>(exception.message.toString())
                }
            } else {
                Resource.Error<List<SpeedDialContact>>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<List<SpeedDialContact>>(exception.message.toString())
        }
    }

    override suspend fun getSpeedDialContactsFlow(userKey: String): Flow<Resource<ArrayList<SpeedDialContact>?>> =
        callbackFlow {

            val databaseReference = FirebaseDatabase.getInstance().getReference("users_speed_dial")

            val speedDialContactsListener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    var contactsList = ArrayList<SpeedDialContact>()
                    for (child in snapshot.children) {
                        val contact = child.getValue(SpeedDialContact::class.java)
                        contact?.let {
                            contact.user_key = child.key.toString()
                            contactsList.add(contact)
                        }
                    }
                    trySend(Resource.Success<ArrayList<SpeedDialContact>?>(contactsList))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.Error<ArrayList<SpeedDialContact>?>(error.toException().localizedMessage))

                }
            }

            /*
                    val speedDialContactsListener = object : ChildEventListener {
                        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                            val data = snapshot.getValue(SpeedDialContact::class.java)
                            data?.let {
                                data.user_key = snapshot.key.toString()
                                trySend(SpeedDialRepository.DataEvent.OnChildAdded(data, previousChildName))
                            }
                        }

                        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                            val data = snapshot.getValue(SpeedDialContact::class.java)
                            data?.let {
                                data.user_key = snapshot.key.toString()
                                trySend(SpeedDialRepository.DataEvent.OnChildAdded(data, previousChildName))
                            }
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            val data = snapshot.getValue(SpeedDialContact::class.java)
                            data?.let {
                                data.user_key = snapshot.key.toString()
                                trySend(SpeedDialRepository.DataEvent.OnChildRemoved(data))
                            }
                        }

                        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                            //TODO("Not yet implemented")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            trySend(SpeedDialRepository.DataEvent.OnError(error.toException()))
                        }
                    }
            */


//            databaseReference.child(userKey).addChildEventListener(speedDialContactsListener)
            databaseReference.child(userKey).addValueEventListener(speedDialContactsListener)

            awaitClose {
                databaseReference.removeEventListener(speedDialContactsListener)
            }
        }


}


