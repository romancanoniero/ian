package com.iyr.ian.viewmodels

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iyr.ian.dao.models.User

class UserViewModel : ViewModel() {



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

}