package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.iyr.ian.dao.repositories.SubscriptionsRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

class SubscriptionsRepositoryImpl : SubscriptionsRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)


    override suspend fun buyPlan(planKey: String, forceDowngrade: Boolean?): Resource<Int?> {
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = FirebaseAuth.getInstance().uid.toString()
                data["plan_key"] = planKey
                data["downgrade"] =forceDowngrade?: false
                try {
                    var response =   FirebaseFunctions.getInstance()
                        .getHttpsCallable("buySubscriptionPlan")
                        .call(data).await()
                    Resource.Success<Int?>(1)
                } catch (exception: Exception) {
                    Resource.Error<Int?>(exception.message.toString())
                }
            } else {
                Resource.Error<Int?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<Int?>(exception.message.toString())
        }

    }




}

