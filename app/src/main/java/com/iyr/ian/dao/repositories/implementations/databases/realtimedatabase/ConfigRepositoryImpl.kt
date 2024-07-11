package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.iyr.ian.dao.models.SystemConfig
import com.iyr.ian.dao.repositories.ChatRepository
import com.iyr.ian.dao.repositories.ConfigRepository
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ConfigRepositoryImpl : ConfigRepository() {

    private var chatRoomListener: ChildEventListener? = null
    private var chatRoomReference: DatabaseReference? = null

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)

    /*
    private lateinit var appConfigsRef: DatabaseReference
    private var appConfigsListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            this@AppClass.appConfigs = snapshot.getValue(SystemConfig::class.java)
        }

        override fun onCancelled(error: DatabaseError) {
            AppClass.instance.currentActivity?.showErrorDialog(error.toException().localizedMessage)
        }
    }
    */

    override fun getConfigFlow(): Flow<Resource<ConfigDataEvent>> = callbackFlow {
        var appConfigsListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var systemConfig: SystemConfig = snapshot.getValue(SystemConfig::class.java)!!
                trySend(
                    Resource.Success<ConfigDataEvent>(
                        ConfigDataEvent.OnChildAdded(
                            systemConfig, null
                        )
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(
                    Resource.Success<ConfigDataEvent>(
                        ConfigDataEvent.OnError(
                            error.toException()
                        )
                    )
                )


            }
        }

        databaseReference.addValueEventListener(appConfigsListener)

        awaitClose {
            databaseReference.removeEventListener(appConfigsListener)
        }
    }


}