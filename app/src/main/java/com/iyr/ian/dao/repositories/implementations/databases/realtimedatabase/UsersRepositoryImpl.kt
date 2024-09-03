package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.UsersRespository
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.coroutines.safeCall
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UsersRepositoryImpl : UsersRespository() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)


    override suspend fun getUserLoginStatus(): Resource<FirebaseUser?> {

        return withContext(Dispatchers.IO) {
            safeCall {
                var response: FirebaseUser? = null

                if (FirebaseAuth.getInstance().currentUser != null) {
                    FirebaseAuth.getInstance().currentUser!!.reload().await()
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        //--------------
                        when ((currentUser.providerData[1] as UserInfo).providerId) {
                            "email" -> {
                                if (currentUser.isEmailVerified) {
                                    response = FirebaseAuth.getInstance().currentUser
                                }
                            }

                            "password" -> {
                                if (currentUser.isEmailVerified) {
                                    response = FirebaseAuth.getInstance().currentUser
                                }
                            }

                            "google.com" -> {
                                if (currentUser.isEmailVerified) {
                                    response = FirebaseAuth.getInstance().currentUser
                                }
                            }

                            "phone" -> {
                                val userInfo =
                                    (currentUser.providerData[1] as UserInfo)


                                response = FirebaseAuth.getInstance().currentUser
                            }
                        }
                    }
                }

                if (response != null) Resource.Success(response) else Resource.Error("Error")
            }
        }
    }

    override suspend fun getUserRemote(userKey: String): Resource<User?> {

        return try {
            val queryResult = FirebaseDatabase.getInstance()
                .getReference(tableName)
                .child(userKey)
                .get()
                .await()

            val userRemote: User? = queryResult.getValue(User::class.java)
            userRemote?.user_key = userKey
            Resource.Success<User?>(userRemote)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage.toString())
        }

    }


    override fun getUserDataAsFlow(userKey: String): Flow<Resource<User>> = callbackFlow {

        var userReference = databaseReference.child(userKey)

        var userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    val user = snapshot.getValue(User::class.java)!!
                    user.user_key = snapshot.key
                    trySend(Resource.Success<User>(user))
                } else {
                    // cuando el evento dejo de existir

                    //                  Toast.makeText(context, "Evento Eliminado", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        userReference.addValueEventListener(userListener)
        awaitClose {
            userReference.removeEventListener(userListener)
        }
    }.conflate()


    override suspend fun listUsersByPhoneNumber(phoneNumber: String): Resource<ArrayList<User?>> {
        val list: ArrayList<User?> = ArrayList<User?>()

        return try {
            val queryResult = FirebaseDatabase.getInstance()
                .getReference(tableName)
                .get()
                .await()

            val returnedData = queryResult.children
            returnedData.forEach { record ->
                var user = record.getValue(User::class.java)!!
                user.user_key = record.key
                if (user.telephone_number.toString().endsWith(phoneNumber)) {
                    list.add(user)
                }
            }
            Resource.Success<ArrayList<User?>>(list)
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage.toString())
        }


    }

    override suspend fun saveUser(currentObject: User, originalObject: User?): Resource<User?> {

        try {
            var tokenRequest = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            val token = tokenRequest.token.toString()

            val data: MutableMap<String, Any> = HashMap()
            data["auth_token"] = token
            data["user_key"] = currentObject.user_key
            data["original_record"] = Gson().toJson(originalObject)
            data["current_record"] = Gson().toJson(currentObject)

            try {


                var call = FirebaseFunctions.getInstance()
                    .getHttpsCallable("updateUserProfile")
                    .call(data).await()

                val result: HashMap<String, Any?> = call.data as HashMap<String, Any?>

                val user =
                    Gson().toJson(result["data"].toString(), User::class.java) as User
                Resource.Success<User>(user)
            } catch (exception: Exception) {
                Resource.Error<User?>(exception.localizedMessage.toString())
            }


        } catch (exception: Exception) {
            Resource.Error<User?>(exception.localizedMessage.toString())
        }
        return Resource.Error<User?>("Error saving")
    }


    override suspend fun onDeleteAccount(): Resource<Boolean?> {
        Resource.Loading<Boolean?>()
        return try {
            var token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {

                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid.toString()
                try {
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("removeUserAccount")
                        .call(data).await()

                    Resource.Success<Boolean?>(true)
                } catch (exception: Exception) {
                    Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                Resource.Error<Boolean?>("token_null")
            }
        } catch (exception: java.lang.Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }


    override suspend fun insertUser(user: User): Resource<Boolean?> {
        return try {
            val call = FirebaseDatabase.getInstance()
                .getReference(tableName)
                .child(user.user_key)
                .setValue(user).await()

            return Resource.Success<Boolean?>(true)

        } catch (exception: Exception) {
            return Resource.Error<Boolean?>(exception.message.toString())
        }
    }

    override suspend fun updateUserStatus(
        userKey: String,
        pin: String,
        latLng: LatLng
    ): Resource<Boolean?> {
        return try {
            var token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {

                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = userKey
                data["latitude"] = latLng.latitude
                data["longitude"] = latLng.longitude
                data["security_code"] = pin

                try {
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("updateUserStatus")
                        .call(data).await()

                    Resource.Success<Boolean?>(true)
                } catch (exception: Exception) {
                    Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                Resource.Error<Boolean?>("token_null")
            }
        } catch (exception: java.lang.Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }

    override suspend fun updateUserDataByMap(
        userKey: String,
        map: HashMap<String, Any>
    ): Resource<Boolean?> {
        return try {
            var token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {

                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = userKey
                data["data_map"] = Gson().toJson(map)
                try {
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("updateUserProfileByMap")
                        .call(data).await()

                    var mapResult = call.data as HashMap<String, Any?>

                    if (mapResult["status"] == 0)
                        Resource.Success<Boolean?>(true)
                    else
                        Resource.Error<Boolean?>(mapResult["message"].toString())

                } catch (exception: Exception) {
                    Resource.Error<Boolean?>(exception.message.toString())
                }
            } else {
                Resource.Error<Boolean?>("token_null")
            }
        } catch (exception: java.lang.Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }

    override suspend fun updateSubscriptionType(
        userKey: String,
        subscriptionTypeKey: String?
    ): Resource<Boolean?> {
        return try {
            val call = FirebaseDatabase.getInstance()
                .getReference(tableName)
                .child(userKey)
                .child("subscription_type_key")
                .setValue(subscriptionTypeKey).await()
            return Resource.Success<Boolean?>(true)
        } catch (exception: Exception) {
            return Resource.Error<Boolean?>(exception.message.toString())
        }

    }


    override suspend fun onUpdateUserImage(userKey: String, image: MediaFile): Resource<Boolean?> {
        var map = HashMap<String, Any>()
        map.put("image/file_name", image.file_name)
        return this.updateUserDataByMap(userKey, map)
    }

    /***
     * This method is used to set the user online
     */
    override suspend fun onOnLine(userKey: String): Resource<Boolean?> {
        return try {

            val call = FirebaseDatabase.getInstance()
                .getReference("users_online_status")
                .child(userKey)
                .setValue(true).await()

            Resource.Success<Boolean?>(true)
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }

    /***
     * This method is used to set the user offline
     */
    override suspend fun onOffLine(userKey: String): Resource<Boolean?> {
        return try {
            val call = FirebaseDatabase.getInstance()
                .getReference("users_online_status")
                .child(userKey)
                .removeValue().await()
            Resource.Success<Boolean?>(true)
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }

    override suspend fun onLineFlow(userKey: String): Flow<Resource<Boolean?>> = callbackFlow {
        var onLineReference =
            FirebaseDatabase.getInstance().getReference("users_online_status").child(userKey)

        var statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    trySend(Resource.Success<Boolean?>(true))
                } else {
                    trySend(Resource.Success<Boolean?>(false))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        onLineReference.addValueEventListener(statusListener)

        awaitClose {
            onLineReference.removeEventListener(statusListener)
        }
    }.conflate()

}