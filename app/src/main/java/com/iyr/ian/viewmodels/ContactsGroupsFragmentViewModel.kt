package com.iyr.ian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ContactGroupsRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsGroupsFragmentViewModel(val userKey: String) : ViewModel() {

    private var contactGroupRepository: ContactGroupsRepositoryImpl = ContactGroupsRepositoryImpl()


    fun onCreateContactGroupClicked(listName: String) {
        viewModelScope.launch(Dispatchers.IO)
        {
           val result =  contactGroupRepository.postContactGroup(userKey, listName)

        }
    }

}