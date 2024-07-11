package com.iyr.ian.ui.signup.phone_contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.dao.models.SpeedDialContact
import com.iyr.ian.repository.implementations.databases.realtimedatabase.SpeedDialRepositoryImpl
import com.iyr.ian.utils.PhoneContact
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AddContactsFromPhoneViewModel : ViewModel() {


    private val speedDialRepositoryImpl: SpeedDialRepositoryImpl = SpeedDialRepositoryImpl()

    private val _inviting = MutableLiveData<Resource<List<SpeedDialContact>>?>()
    val inviting: LiveData<Resource<List<SpeedDialContact>>?> = _inviting

    /**
     * Add contacts to the speed dial list
     */
    fun addAsSpeedDialContact(contacts: List<PhoneContact>) {
        _inviting.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            var call = speedDialRepositoryImpl.addSpeedDialContacts(contacts)
            if (call is Resource.Success) {
                _inviting.postValue(Resource.Success(call.data))
            } else {
                _inviting.postValue(Resource.Error(call.message.toString()))
            }
        }
    }

}