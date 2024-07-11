package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.SubscriptionPlans
import com.iyr.ian.utils.coroutines.Resource

interface SubscriptionPlansRepositoryInterface {
    suspend fun getSubscriptionPlan(subscriptionPlanKey: String): Resource<SubscriptionPlans?>
    suspend fun listSubscriptionPlans(subscriptionTypeKey : String): Resource<List<SubscriptionPlans>?>
}

abstract class SubscriptionPlansRepository : SubscriptionPlansRepositoryInterface {
    private var authManager: Any? = null
    protected var tableName: String = "subscription_plans"
    private val tableReference: Any? = null
}