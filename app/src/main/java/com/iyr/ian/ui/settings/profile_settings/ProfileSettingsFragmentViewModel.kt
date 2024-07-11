package com.iyr.ian.ui.settings.profile_settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.AppConstants
import com.iyr.ian.dao.models.User
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileSettingsFragmentViewModel : ViewModel() {

    private val usersRepository = UsersRepositoryImpl()


    private val _savingStatus = MutableLiveData<Resource<Boolean?>>()
    val savingStatus: LiveData<Resource<Boolean?>> = _savingStatus

    private val _buttonStatus = MutableLiveData<Boolean>()
    val buttonStatus: LiveData<Boolean> = _buttonStatus

    private val _displayName = MutableLiveData<String>()
    val displayName: LiveData<String> = _displayName

    private val _profileImageFileName = MutableLiveData<String?>()
    val profileImageFileName: LiveData<String?> = _profileImageFileName

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user


    fun onDisplayNameChanged(value: String) {
        _displayName.postValue(value)
        onValidateUIRequest()
    }

    fun setProfileImagePath(fileName: String?) {
        _profileImageFileName.value = fileName
        onValidateUIRequest()
    }

    private fun onValidateUIRequest() {
        val shouldEnable =
            !_displayName.value.isNullOrBlank() && _displayName.value?.length ?: 0 >= AppConstants.USERNAME_MINIMUM_LENGTH
                    && _displayName.value!=_user.value?.display_name


        _buttonStatus.postValue(shouldEnable)
    }

    fun onSaveButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            _savingStatus.postValue(Resource.Loading())
            var dataMap = HashMap<String, Any>()
            dataMap["display_name"] = _displayName.value.toString()
            val call = usersRepository.updateUserDataByMap(user.value?.user_key.toString(), dataMap)
            if (call.message == null)
                _savingStatus.postValue(Resource.Success<Boolean?>(true))
            else
                _savingStatus.postValue(Resource.Error<Boolean?>(call.message.toString()))
        }
    }

    fun setUser(user: User?) {
        _user.value = user
    }

}