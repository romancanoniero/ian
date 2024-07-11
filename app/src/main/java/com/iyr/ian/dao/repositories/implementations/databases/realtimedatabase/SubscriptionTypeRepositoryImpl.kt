package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.dao.repositories.SubscriptionTypesRepository
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

class SubscriptionTypeRepositoryImpl : SubscriptionTypesRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)


    override suspend fun getSubscriptionType(subscriptionTypeKey: String): Resource<SubscriptionTypes?> {
        return try {
            val queryResult = databaseReference
                .child(subscriptionTypeKey).get().await()

            val subscriptionTypeRemote: SubscriptionTypes? =
                queryResult.getValue(SubscriptionTypes::class.java)
            subscriptionTypeRemote?.subscription_type_key = queryResult.key
            Resource.Success<SubscriptionTypes?>(subscriptionTypeRemote)
        } catch (ex: Exception) {
            Resource.Error(ex.message.toString())
        }
    }

    override suspend fun listSubscriptionType(): Resource<List<SubscriptionTypes>?> {
        return try {
            val queryResult = databaseReference.get().await()

            val subscriptionTypesRemote: List<SubscriptionTypes>? =
                queryResult.children.mapNotNull {
                    val subscriptionType = it.getValue(SubscriptionTypes::class.java)
                    subscriptionType?.subscription_type_key = it.key

                    subscriptionType
                }

            subscriptionTypesRemote?.sortedBy { it.access_level }

            Resource.Success<List<SubscriptionTypes>?>(subscriptionTypesRemote)
        } catch (ex: Exception) {
            Resource.Error(ex.message.toString())
        }
    }


    override suspend fun getSubscriptionTypeByAccessLevel(level: AccessLevelsEnum): Resource<SubscriptionTypes?> {
        var listCall = listSubscriptionType()
        when (listCall) {
            is Resource.Error -> {
                return Resource.Error(listCall.message.toString())
            }
            is Resource.Success -> {
                val subscriptionTypes = listCall.data
                val subscriptionType = subscriptionTypes?.find { it.access_level == level.ordinal }
                return Resource.Success<SubscriptionTypes?>(subscriptionType)
            }
            else -> {
                return Resource.Error("Error")
            }
        }
    }
}

