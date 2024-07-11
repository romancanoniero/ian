package com.iyr.ian.repository.implementations.databases.realtimedatabase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iyr.ian.dao.models.SubscriptionPlans
import com.iyr.ian.dao.repositories.SubscriptionPlansRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

class SubscriptionPlansRepositoryImpl : SubscriptionPlansRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)


    override suspend fun getSubscriptionPlan(subscriptionPlanKey: String): Resource<SubscriptionPlans?> {
        return try {
            val queryResult = databaseReference.child(subscriptionPlanKey).get().await()

            val subscriptionPlanRemote: SubscriptionPlans? =
                queryResult.getValue(SubscriptionPlans::class.java)
            subscriptionPlanRemote?.subscription_type_key = queryResult.key
            Resource.Success<SubscriptionPlans?>(subscriptionPlanRemote)
        } catch (ex: Exception) {
            Resource.Error(ex.message.toString())
        }
    }

    override suspend fun listSubscriptionPlans(subscriptionTypeKey : String): Resource<List<SubscriptionPlans>?> {
        return try {
            val queryResult = databaseReference.get().await()

            Log.d("SubscriptionPlansRepositoryImpl", "listSubscriptionPlans: ${queryResult.children}")

            val subscriptionPlansRemote: ArrayList<SubscriptionPlans> = ArrayList<SubscriptionPlans>()

            queryResult.children.forEach {
                val subscriptionPlan = it.getValue(SubscriptionPlans::class.java)

                if ( subscriptionPlan?.subscription_type_key == subscriptionTypeKey)
                {

                    subscriptionPlansRemote.add(subscriptionPlan)
                }
            }


            Resource.Success<List<SubscriptionPlans>?>(subscriptionPlansRemote)
        } catch (ex: Exception) {
            Resource.Error(ex.message.toString())
        }
    }

}

