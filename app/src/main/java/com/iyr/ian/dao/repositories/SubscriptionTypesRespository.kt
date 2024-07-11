package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.enums.AccessLevelsEnum
import com.iyr.ian.utils.coroutines.Resource

interface SubscriptionTypesRepositoryInterface {
    suspend fun getSubscriptionType(subscriptionTypeKey: String): Resource<SubscriptionTypes?>
    suspend fun listSubscriptionType(): Resource<List<SubscriptionTypes>?>
    suspend fun getSubscriptionTypeByAccessLevel(free: AccessLevelsEnum): Resource<SubscriptionTypes?>
}

abstract class SubscriptionTypesRepository : SubscriptionTypesRepositoryInterface {
    private var authManager: Any? = null
    protected var tableName: String = "subscription_types"

    private val tableReference: Any? = null
}