package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.SpeedDialContact
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.flow.Flow

interface SubscriptionsRepositoryInterface {
    suspend fun  buyPlan(planKey: String , forceDowngrade : Boolean? = false): Resource<Int?>
}

abstract class SubscriptionsRepository : SubscriptionsRepositoryInterface {
    private var authManager: Any? = null
    protected var tableName: String = "users_subscriptions"
    private val tableReference: Any? = null
}