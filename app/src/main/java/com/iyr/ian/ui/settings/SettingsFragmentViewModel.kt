package com.iyr.ian.ui.settings

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iyr.ian.dao.models.User

class SettingsFragmentViewModel : ViewModel() {
    private val _fragmentVisible =
        MutableLiveData<SettingsFragmentsEnum>().apply { SettingsFragmentsEnum.LANDING }
    val fragmentVisible: LiveData<SettingsFragmentsEnum> = _fragmentVisible

    companion object{
        private lateinit var instance: SettingsFragmentViewModel

        @MainThread
        fun getInstance(): SettingsFragmentViewModel {
            instance = if(::instance.isInitialized) instance else SettingsFragmentViewModel()
            return instance
        }
    }

    fun onProfileSettingsClick() {
        _fragmentVisible.postValue(SettingsFragmentsEnum.PROFILE_SETTINGS)
    }

    fun onSOSSettingsClick() {
        _fragmentVisible.postValue(SettingsFragmentsEnum.SOS_SETTINGS)

    }

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun onUserChanged(user: User?) {
        _user.postValue(user)
    }

    fun goToFragment(index: Int) {
        _fragmentVisible.postValue(SettingsFragmentsEnum.values()[index])
    }
}