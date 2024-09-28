package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iyr.ian.dao.models.Subscription
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class UsersSubscriptionsRepositoryImpl : UsersSubscriptionsRespository() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)


    override fun getUserSubscriptionsAsFlow(userKey: String): Flow<Resource<List<Subscription>?>> =
        callbackFlow {

            var subscriptionsReference = databaseReference.child(userKey)

            var subscriptionsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var subscriptions = snapshot.children.mapNotNull {
                            val subscription = it.getValue(Subscription::class.java)
                            subscription?.subscription_key = it.key
                            subscription
                        }
                        trySend(Resource.Success<List<Subscription>?>(subscriptions))

                    } else {
                        // cuando el evento dejo de existir

                        //                  Toast.makeText(context, "Evento Eliminado", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }

            subscriptionsReference.addValueEventListener(subscriptionsListener)
            awaitClose {
                subscriptionsReference.removeEventListener(subscriptionsListener)
            }
        }.conflate()

    override suspend fun getUserSubscriptionTypeAsFlow(userKey: String): Flow<Resource<SubscriptionTypes?>> =
        callbackFlow<Resource<SubscriptionTypes?>> {

            var subscriptionsTypesRespository = SubscriptionTypeRepositoryImpl()
            var subscriptionsReference = databaseReference.child(userKey)

            var subscriptionsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var subscriptions = snapshot.children.mapNotNull {
                            val subscription = it.getValue(Subscription::class.java)
                            subscription?.subscription_key = it.key
                            subscription
                        }

                        subscriptions?.let { list ->
                            list.forEach { subscription ->

                                // comparar si la fecha de hoy en milisegundos es mayor a la fecha de subscripcion (subscripted_on) y menor a la fecha de expiracion (expires_on)
                                // verifica si la hora actual se encuentra entre la fecha de subscripcion y la fecha de expiracion
//                                val timeInMillis = MainActivityViewModel.getInstance().getServerTimeUsingDeviceTime()

//                                if (subscription.subscripted_on <= timeInMillis  && subscription.expires_on >= timeInMillis) {

                                    GlobalScope.launch(Dispatchers.IO) {
                                        var subscriptionType =
                                            subscriptionsTypesRespository.getSubscriptionType(
                                                subscriptionTypeKey = subscription.subscription_type_key
                                            )
                                        trySend(
                                            Resource.Success<SubscriptionTypes?>(
                                                subscriptionType.data
                                            )

                                        )
                                        return@launch
                                    }
                                    return@forEach
                                    /*
                                } else {
                                    var ddd = 33
                                }*/

                            }
                        }


                    } else {

                        GlobalScope.launch(Dispatchers.IO) {
                            var subscriptionType =
                                subscriptionsTypesRespository.getSubscriptionTypeByAccessLevel(
                                    AccessLevelsEnum.FREE
                                )
                            trySend(
                                Resource.Success<SubscriptionTypes?>(
                                    subscriptionType.data
                                )
                            )
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }

            subscriptionsReference.addValueEventListener(subscriptionsListener)
            awaitClose {
                subscriptionsReference.removeEventListener(subscriptionsListener)
            }
        }.conflate()


}