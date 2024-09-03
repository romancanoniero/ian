package com.iyr.ian.viewmodels

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.dao.models.User
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventFollowersRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventsFollowedRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val usersRepository = UsersRepositoryImpl()
    private val eventFollowedRepository = EventsFollowedRepositoryImpl()
    private val eventFollowersRepository = EventFollowersRepositoryImpl()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    companion object {
        private lateinit var instance: UserViewModel

        @MainThread
        fun getInstance(): UserViewModel {
            instance = if (Companion::instance.isInitialized) instance else UserViewModel()
            return instance
        }
    }

    fun setUser(user: User) {
        _user.value = user
    }

    fun getUser(): User? {
        return _user.value
    }

    fun offLine() {
        val userKey = user.value?.user_key!!

        viewModelScope.launch(Dispatchers.IO) {
            usersRepository.onOffLine(userKey)
            val call = eventFollowedRepository.getEventsFollowedAll(user.value?.user_key!!)
            if (call is Resource.Success) {
                call.data?.forEach {
                    eventFollowersRepository.setOffLine(it.event_key,userKey)
                }
            }

        }
    }

    fun onLine() {
        val userKey = user.value?.user_key!!
        viewModelScope.launch(Dispatchers.IO) {
            usersRepository.onOnLine(userKey)
            val call = eventFollowedRepository.getEventsFollowedAll(user.value?.user_key!!)
            if (call is Resource.Success) {
                call.data?.forEach {
                    eventFollowersRepository.setOnLine(it.event_key,userKey)
                }
            }
        }
    }

}