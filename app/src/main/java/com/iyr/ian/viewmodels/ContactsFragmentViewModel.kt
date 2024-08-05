package com.iyr.ian.viewmodels

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.dao.repositories.ContactsRepository
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ContactsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationListRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsFragmentViewModel : ViewModel() {

    lateinit var userKey: String

    val contactsGroupsRepository = NotificationListRepositoryImpl()
    val contactsRepository = ContactsRepositoryImpl()

    companion object {
        private lateinit var instance: ContactsFragmentViewModel

        @MainThread
        fun getInstance(userKey: String? = null): ContactsFragmentViewModel {
            instance = if (Companion::instance.isInitialized) instance else {
                ContactsFragmentViewModel().apply {
                    this.userKey = userKey!!
                }
            }
            return instance
        }
    }


    private val groups = ArrayList<ContactGroup>(ArrayList<ContactGroup>())
    private val _groupsList = MutableLiveData<ArrayList<ContactGroup>?>()
    val groupsList: LiveData<ArrayList<ContactGroup>?> = _groupsList

    private val contacts = ArrayList<Contact>(ArrayList<Contact>())
    private val _contactsList = MutableLiveData<ArrayList<Contact>?>()
    val contactsList: LiveData<ArrayList<Contact>?> = _contactsList


    val contactsListFlow = liveData<ArrayList<Contact>>(Dispatchers.IO) {
        contactsRepository.contactsByUserListFlow(userKey).collect { contacts ->
            _contactsList.postValue(contacts)
        }
    }


    val contactsGroupsListFlow = liveData<ArrayList<ContactGroup>>(Dispatchers.IO) {
        contactsGroupsRepository.contactsGroupsByUserListFlow(userKey).collect { groups ->
            _groupsList.postValue(groups)
        }
    }


    val contactsFlow = liveData<ContactsRepository.DataEvent>(Dispatchers.IO)
    {
        contactsRepository.contactsByUserFlow(userKey).collect { contacts ->
            emit(contacts)
        }
    }

    private val _insertContactAction = MutableLiveData<Resource<Contact?>>()
    val insertContactAction: LiveData<Resource<Contact?>> = _insertContactAction


    private val _insertContactGroupAction = MutableLiveData<Resource<ContactGroup>?>()
    val insertContactGroupAction: LiveData<Resource<ContactGroup>?> = _insertContactGroupAction

    /**
     * Agrega un nuevo grupo de contactos
     */
    fun insertGroup(groupName: String) = viewModelScope.launch(Dispatchers.IO) {
        var result = contactsGroupsRepository.insertGroup(groupName)
        _insertContactGroupAction.postValue(result)
    }


    private val _deleteContactGroupAction = MutableLiveData<Resource<Boolean?>?>()
    val deleteContactGroupAction: LiveData<Resource<Boolean?>?> = _deleteContactGroupAction
    fun deleteGroup(listKey: String) {
        _deleteContactGroupAction.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            val ownerKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
            var result = contactsGroupsRepository.deleteGroup(ownerKey, listKey)
            _deleteContactGroupAction.postValue(result)
        }
    }

    fun updateVisibleContacts(items: ArrayList<Contact>) {
        _contactsList.postValue(items)
    }

    private fun modifyContactGroup(index: Int, group: ContactGroup) {
        synchronized(groups) {
            groups.set(index, group)
            _groupsList.postValue(groups)
        }
    }

    private fun insertContactGroup(group: ContactGroup) {
        synchronized(groups) {
            groups.add(group)
            _groupsList.postValue(groups)
        }
    }

    private fun removeContactGroup(index: Int) {
        synchronized(groups) {
            groups.removeAt(index)
            _groupsList.postValue(groups)
        }
    }

    fun addContactByEmail(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = UserViewModel.getInstance().getUser() as UserMinimum
            contactsRepository.inviteByEmail(user, email)
        }
    }

    fun addContactByPhone(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = UserViewModel.getInstance().getUser() as UserMinimum

            contactsRepository.inviteByTelephone(user, phoneNumber)
        }
    }


    private val _showMediaShare = MutableLiveData<Boolean?>()
    val showMediaShare: LiveData<Boolean?> = _showMediaShare
    fun inviteNonUser(data: String) {

        /*
                if (data.isValidPhoneNumber() )  {
                    insertContactByPhone(data)
                } else if (data.isValidMail()) {
                    addContactByEmail(data)
                }

         */
    }


    fun insertContactByPhone(telephoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = UserViewModel.getInstance().getUser() as UserMinimum

            val call = contactsRepository.inviteByTelephone(user, telephoneNumber)
            when (call) {
                is Resource.Error -> {
                    _insertContactAction.postValue(Resource.Error(call.message.toString()))
                }

                is Resource.Success -> {
                    _insertContactAction.postValue(Resource.Success<Contact?>(call.data!!))
                }

                else -> {
                }
            }
        }

    }

    private val _contactCancelationAction = MutableLiveData<Resource<Boolean?>?>()
    val contactCancelationAction: LiveData<Resource<Boolean?>?> = _contactCancelationAction
    fun cancelFriendship(userFriendshipToRevokeKey: String) {
        _contactCancelationAction.postValue(Resource.Loading())

        viewModelScope.launch(Dispatchers.IO) {
            val call =
                contactsRepository.contactCancelFriendship(userKey, userFriendshipToRevokeKey)
            when (call) {
                is Resource.Error -> {
                    _contactCancelationAction.postValue(Resource.Error(call.message.toString()))
                }

                is Resource.Success -> {
                    _contactCancelationAction.postValue(Resource.Success<Boolean?>(call.data!!))
                }

                else -> {
                }
            }
        }

    }

    fun resetOperations() {
        _contactCancelationAction.value = null
        _insertContactGroupAction.value = null
        _deleteContactGroupAction.value = null
    }

    fun selectUnselectContactGroup(contact: Contact, group: ContactGroup, included: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            contactsRepository.selectUnselectContactGroup(
                userKey,
                contact,
                group,
                included)
        }
    }


}