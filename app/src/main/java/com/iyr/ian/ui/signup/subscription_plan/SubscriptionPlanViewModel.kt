package com.iyr.ian.ui.signup.subscription_plan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.dao.models.SubscriptionPlans
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SubscriptionPlansRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SubscriptionTypeRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SubscriptionsRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubscriptionPlanViewModel : ViewModel() {

    private val subscriptionTypesRepository: SubscriptionTypeRepositoryImpl =
        SubscriptionTypeRepositoryImpl()

    private val subscriptionPlansRepository: SubscriptionPlansRepositoryImpl =
        SubscriptionPlansRepositoryImpl()

    private val subscriptionsRepository: SubscriptionsRepositoryImpl =
        SubscriptionsRepositoryImpl()

    private val _buyingStatus = MutableLiveData<Resource<Int?>?>()
    val buyingStatus: LiveData<Resource<Int?>?> = _buyingStatus

    private val _subscriptionTypeslist = MutableLiveData<Resource<List<SubscriptionTypes>>>()
    val subscriptionTypeslist: LiveData<Resource<List<SubscriptionTypes>>> = _subscriptionTypeslist


    private val _subscriptionPlansList = MutableLiveData<Resource<List<SubscriptionPlans>>>()
    val subscriptionPlansList: LiveData<Resource<List<SubscriptionPlans>>> = _subscriptionPlansList


    fun fetchSubscriptionTypesList() {
        _subscriptionTypeslist.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            var call = subscriptionTypesRepository.listSubscriptionType()
            if (call is Resource.Success) {

                var response = call.data as List<SubscriptionTypes>
                _subscriptionTypeslist.postValue(Resource.Success(response))
            } else {
                _subscriptionTypeslist.postValue(Resource.Error(call.message.toString()))
            }
        }
    }

    fun onSubscriptionTypeSelected(subscriptionKey: String) {
        _subscriptionPlansList.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            var call = subscriptionPlansRepository.listSubscriptionPlans(subscriptionKey)
            if (call is Resource.Success) {
                var response = call.data as List<SubscriptionPlans>
                _subscriptionPlansList.postValue(Resource.Success(response))
            } else {
                _subscriptionPlansList.postValue(Resource.Error(call.message.toString()))
            }
        }




    }

    fun onBuy(plan: SubscriptionPlans) {
        _buyingStatus.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            var call = subscriptionsRepository.buyPlan(plan.plan_key)
            if (call is Resource.Success) {
       //         var response = call.data as List<SubscriptionPlans>
                _buyingStatus.postValue(Resource.Success(1))
            } else {
                _buyingStatus.postValue(Resource.Error(call.message.toString()))
            }
        }

    }

    fun resetBuyingStatus() {
        _buyingStatus.postValue(null)
    }
}

