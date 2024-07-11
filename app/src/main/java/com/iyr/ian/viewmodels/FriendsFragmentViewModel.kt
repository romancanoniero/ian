package com.iyr.ian.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.repositories.ContactsRepository
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ContactsRepositoryImpl
import com.iyr.ian.utils.StringUtils
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendsFragmentViewModel(val userKey: String) : ViewModel() {

    private var contactsRepository = ContactsRepositoryImpl()

    var searchJob: Job? = null
    private val _autoCompleteSearch = MutableLiveData<Resource<ArrayList<Contact>?>>()
    val autoCompleteSearch: LiveData<Resource<ArrayList<Contact>?>> = _autoCompleteSearch


    private val _updateOperation = MutableLiveData<Resource<Boolean>?>()
    val updateOperation: LiveData<Resource<Boolean>?> = _updateOperation

    fun onSearchBoxChange(text: String) {

        var proceedWithSearch = false
        if (!StringUtils.areOnlyDigits(text) && !text.contains("@")) {
            proceedWithSearch = true
        } else if (StringUtils.areOnlyDigits(text) && text.length >= 9) {
            proceedWithSearch = true
        } else if (StringUtils.emailAtSymbolPresent(text) && Validators.isValidMail(text)) {
            proceedWithSearch = true
        }

        if (proceedWithSearch) {
            _autoCompleteSearch.postValue(Resource.Loading())
            if (searchJob?.isActive == true) {
                searchJob?.cancel()
            }

            searchJob = viewModelScope.launch(Dispatchers.IO)
            {
                var call = contactsRepository.searchContactsFromPhone(
                    FirebaseAuth.getInstance().uid.toString(), text
                )

                when (call) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main)
                        {
                            _autoCompleteSearch.postValue(Resource.Error(call.message.toString()))
                        }
                    }

                    is Resource.Success -> {
                        withContext(Dispatchers.Main)
                        {
                            _autoCompleteSearch.postValue(Resource.Success<ArrayList<Contact>?>(call.data))
                        }
                    }

                    else -> {
                    }
                }
            }
        }
    }


    /**
     * Forma correcta de escuchar los cambios en la base de datos
     */
    val contactsFlow = liveData<ContactsRepository.DataEvent>(Dispatchers.IO)
    {
        contactsRepository.contactsByUserFlow(userKey).collect { notifications ->
            emit(notifications)
        }
    }


    fun contactInvite(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            var call =
                contactsRepository.postContactInvitation(userKey, contact.user_key.toString())
            when (call) {
                is Resource.Error -> {
                    _updateOperation.postValue(Resource.Error<Boolean>(call.message.toString()))
                }

                is Resource.Loading -> {

                    _updateOperation.postValue(Resource.Loading<Boolean>())

                }

                is Resource.Success -> {
                    _updateOperation.postValue(Resource.Success<Boolean>(true))
                }

                else -> {}
            }

        }
    }

    fun acceptContactRequest(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            var call =
                contactsRepository.contactAcceptInvitation(userKey, contact.user_key.toString())
            when (call) {
                is Resource.Error -> {
                    _updateOperation.postValue(Resource.Error<Boolean>(call.message.toString()))
                }

                is Resource.Loading -> {

                    _updateOperation.postValue(Resource.Loading<Boolean>())

                }

                is Resource.Success -> {
                    _updateOperation.postValue(Resource.Success<Boolean>(true))
                }

                else -> {}
            }

        }

    }
}

