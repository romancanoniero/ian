package com.iyr.ian.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationsRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsFragmentViewModel : ViewModel() {

    private val notificationsRepository = NotificationsRepositoryImpl()
    private val eventsRepository = EventsRepositoryImpl()


    fun updateNotificationsAsRead(keysMap: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationsRepository.setNotificationsAsRead(
                UserViewModel.getInstance().getUser()?.user_key ?: "",
                *keysMap.toTypedArray()
            )
        }

    }

    fun agreeToAssist(notificationKey: String, eventKey: String) {
        MainActivityViewModel.getInstance().showLoader()
        viewModelScope.launch(Dispatchers.IO) {
            val call = eventsRepository.subscribeToEvent(notificationKey, eventKey)
            when (call) {
                is Resource.Success -> {
MainActivityViewModel.getInstance().showGoToEventDialog(notificationKey, eventKey)
                    MainActivityViewModel.getInstance().hideLoader()
                }

                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        MainActivityViewModel.getInstance().hideLoader()
                        MainActivityViewModel.getInstance().showError(call.message.toString())
                    }
                }

                else -> {}
            }
        }
    }

    fun onDeleteNotificationByKeyRequested(notificationKey: String) {
        MainActivityViewModel.getInstance().showLoader()
        viewModelScope.launch(Dispatchers.IO) {
            val userKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
            val call = notificationsRepository.removeNotificationByKey(userKey, notificationKey)
            when (call) {
                is Resource.Success -> {
                    MainActivityViewModel.getInstance().hideLoader()
                }

                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        MainActivityViewModel.getInstance().hideLoader()
                        MainActivityViewModel.getInstance().showError(call.message.toString())
                    }
                }

                else -> {}
            }
        }
    }


    private val _deletionLivaData = MutableLiveData<Resource<Boolean?>>()
    val deletionLivaData: LiveData<Resource<Boolean?>> = _deletionLivaData
    fun onDeleteAllNotificationsRequested() {
        _deletionLivaData.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val call = notificationsRepository.removeAllNotifications(
                    UserViewModel.getInstance().getUser()?.user_key ?: ""
                )
                _deletionLivaData.postValue(call)
            } catch (ex: Exception) {
                _deletionLivaData.postValue(Resource.Error(ex.message.toString()))
            }
        }
    }




}