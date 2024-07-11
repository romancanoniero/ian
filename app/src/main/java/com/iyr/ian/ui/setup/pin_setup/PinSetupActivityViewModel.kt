package com.iyr.fewtouchs.ui.views.setup.pin_setup

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

class PinSetupActivityViewModel : ViewModel() {

    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()

    private var _originalData: User? = null
    private lateinit var _currentData: User

    private val _viewStatus = MutableLiveData<Resource<User?>>()
    val viewStatus: LiveData<Resource<User?>> get() = _viewStatus

    private val _isFirstSetup = MutableLiveData<Boolean>()
    val isFirstSetup: LiveData<Boolean> get() = _isFirstSetup

    private val _pinCode = MutableLiveData<String?>()
    val pinCode: LiveData<String?> get() = _pinCode


    private val _saveButtonEnabled = MutableLiveData<Boolean>()
    val saveButtonEnabled: LiveData<Boolean> get() = _saveButtonEnabled


    fun setExtraData(bundle: Bundle) {
        if (bundle.containsKey("data_object")) {
            val gson: Gson = Gson()

            _currentData = gson.fromJson(bundle.getString("data_object"), User::class.java)
            _originalData = gson.fromJson(bundle.getString("data_object"), User::class.java)



            validateSave()

            /*
                  _sosInvocationCount.value = _currentData.sos_invocation_count
                  if (_currentData.sos_invocation_method.isNullOrEmpty())
                      _currentData.sos_invocation_method = SOSActivationMethods.TAP.name

                  _sosInvocationMethod.value =
                      SOSActivationMethods.valueOf(
                          _currentData.sos_invocation_method
                      )
                  validateSave()
                  validateReset()

             */
        }
        // TODO : Cambiar  por isFirstActivity
        _isFirstSetup.value = if (bundle.containsKey("first_setup")) {
            bundle.getBoolean("first_setup", false)
        } else {
            true
        }
    }


    private fun validateSave() {

        _saveButtonEnabled.value = _currentData.security_code.isBlank() == false &&
                _currentData.security_code.length == 4 &&
                _currentData.security_code != _pinCode.value?.toString()?:"".reversed()
    }

    fun setPinCode(otp: String?) {
        //_pinCode.value = otp
        _currentData.security_code = otp ?: ""
        validateSave()
    }


    fun onSaveButtonClicked() {
        _viewStatus.value = Resource.Loading<User?>()
        viewModelScope.launch(Dispatchers.IO) {

            try {
                var updateCall = usersRepository.saveUser(_currentData, _originalData)

                val user = _currentData
                _viewStatus.postValue(Resource.Success<User?>(user))

            } catch (exception: Exception) {
                _viewStatus.value = Resource.Error<User?>(exception.localizedMessage?.toString() ?: "Unknown Error")
            }
        }
    }


}