package com.iyr.ian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.repository.implementations.databases.realtimedatabase.NotificationListRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationsFragmentViewModel : ViewModel() {

    private var notificationsRepository = NotificationListRepositoryImpl()
    fun updateNotificationsAsRead(keysMap: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationsRepository.setNotificationsAsRead(
                UserViewModel.getInstance().getUser()?.user_key ?: "",
                *keysMap.toTypedArray()
            )
        }

    }

}