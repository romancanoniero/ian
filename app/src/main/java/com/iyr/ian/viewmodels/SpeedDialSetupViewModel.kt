package com.iyr.ian.viewmodels

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.iyr.ian.dao.models.User
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpeedDialSetupViewModel : ViewModel() {

    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()

    private var _originalData: User? = null
    private lateinit var _currentData: User

    private val _viewStatus = MutableLiveData<Resource<User?>>()
    val viewStatus: LiveData<Resource<User?>> get() = _viewStatus

    private val _isFirstSetup = MutableLiveData<Boolean>()
    val isFirstSetup: LiveData<Boolean> get() = _isFirstSetup

    private val _allowToAdd = MutableLiveData<Boolean?>()
    val allowToAdd: LiveData<Boolean?> get() = _allowToAdd

    private val _saveButtonEnabled = MutableLiveData<Boolean>()
    val saveButtonEnabled: LiveData<Boolean> get() = _saveButtonEnabled


    fun setExtraData(bundle: Bundle) {


        if (bundle.containsKey("data_object")) {
            val gson: Gson = Gson()
            _currentData = gson.fromJson(bundle.getString("data_object"), User::class.java)
            _originalData = gson.fromJson(bundle.getString("data_object"), User::class.java)

            _currentData.allow_speed_dial =  _currentData.allow_speed_dial ?: true
            _allowToAdd.value = (_currentData.allow_speed_dial ?: true) as Boolean?
        }
        // TODO : Cambiar  por isFirstActivity
        _isFirstSetup.value = if (bundle.containsKey("first_setup")) {
            bundle.getBoolean("first_setup", false)
        } else {
            true
        }

        _allowToAdd.value = if (bundle.containsKey("allow_add")) {
            bundle.getBoolean("allow_add", false)
        } else {
            true
        }
    }


    fun onSaveButtonClicked() {
        _viewStatus.value = Resource.Loading<User?>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var updateCall = usersRepository.saveUser(_currentData, _originalData)

                val user = _currentData
                _viewStatus.postValue(Resource.Success<User?>(user))

            } catch (exception: Exception) {
                _viewStatus.value =
                    Resource.Error<User?>(exception.localizedMessage?.toString() ?: "Unknown Error")
            }
        }
    }

    fun setAllowSpeedDial(checked: Boolean) {
        _currentData.allow_speed_dial = checked
    }
}