package com.iyr.ian.ui.settings.landing_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsLandingFragmentViewModel : ViewModel() {

    private var usersRepository = UsersRepositoryImpl()


    private val _deletingAccountStatus = MutableLiveData<Resource<Boolean>?>()
    val deletingAccountStatus: LiveData<Resource<Boolean>?> = _deletingAccountStatus
    fun onDeleteAccountClick() {
        _deletingAccountStatus.value = Resource.Loading<Boolean>()
        viewModelScope.launch(Dispatchers.IO)
        {
            var call = usersRepository.onDeleteAccount()
            if (call.message == null)
                _deletingAccountStatus.value = Resource.Success<Boolean>()
            else
                _deletingAccountStatus.value = Resource.Error<Boolean>(call.message.toString())
        }
    }

    private val _image = MutableLiveData<Resource<MediaFile?>>()
    val image: LiveData<Resource<MediaFile?>> get() = _image


    fun onImageChanged(userKey: String, image: MediaFile) {
        viewModelScope.launch(Dispatchers.IO) {
            usersRepository.onUpdateUserImage(userKey, image)
        }
      //  _image.postValue(image)
    }
}