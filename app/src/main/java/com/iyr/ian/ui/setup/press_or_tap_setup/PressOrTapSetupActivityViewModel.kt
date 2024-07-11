package com.iyr.ian.ui.setup.press_or_tap_setup

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.iyr.ian.AppConstants
import com.iyr.ian.dao.models.User
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PressOrTapSetupActivityViewModel : ViewModel() {

    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()

    private var _originalData: User? = null
    private lateinit var _currentData: User

    private val _viewStatus = MutableLiveData<Resource<User?>>()
    val viewStatus: LiveData<Resource<User?>> get() = _viewStatus

    private val _isFirstSetup = MutableLiveData<Boolean>()
    val isFirstSetup: LiveData<Boolean> get() = _isFirstSetup

    private val _sosInvocationCount = MutableLiveData<Int>()
    val sosInvocationCount: LiveData<Int> get() = _sosInvocationCount

    private val _sosInvocationMethod = MutableLiveData<SOSActivationMethods?>()
    val sosInvocationMethod: LiveData<SOSActivationMethods?> get() = _sosInvocationMethod

    private val _saveButtonEnabled = MutableLiveData<Boolean>()
    val saveButtonEnabled: LiveData<Boolean> get() = _saveButtonEnabled

    private val _resetButtonEnabled = MutableLiveData<Boolean>()
    val resetButtonEnabled: LiveData<Boolean> get() = _resetButtonEnabled




    fun setExtraData(bundle: Bundle) {
        if (bundle.containsKey("data_object")) {
            val gson : Gson =  Gson()

            _currentData = gson.fromJson(bundle.getString("data_object"), User::class.java)
            _originalData = gson.fromJson(bundle.getString("data_object"), User::class.java)
            _sosInvocationCount.value = _currentData.sos_invocation_count
            if (_currentData.sos_invocation_method.isNullOrEmpty())
                _currentData.sos_invocation_method = SOSActivationMethods.TAP.name

            _sosInvocationMethod.value =
                SOSActivationMethods.valueOf(
                    _currentData.sos_invocation_method
                )
            validateSave()
            validateReset()
        }
        // TODO : Cambiar  por isFirstActivity
        _isFirstSetup.value = if (bundle.containsKey("first_setup")) {
            bundle.getBoolean("first_setup", false)
        } else {
            true
        }
    }

    private fun validateReset() {
        _resetButtonEnabled.value = _currentData.sos_invocation_count > 0
    }

    private fun validateSave() {
        _saveButtonEnabled.value = if (_currentData.sos_invocation_method != null)
            if (SOSActivationMethods.valueOf(
                    _currentData.sos_invocation_method
                ) == SOSActivationMethods.TAP
            )
                _currentData.sos_invocation_count >= AppConstants.MINIMUM_TAPS
            else
                _currentData.sos_invocation_count >= AppConstants.MINIMUM_TOUCH_TIME
        else
            false
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


            /*
                        UsersWSClient.instance.updateUserProfile(
                            currentObject,
                            originalObject,
                            object : OnCompleteCallback {
                                override fun onComplete(success: Boolean, result: Any?) {
                                    if (success) {
                                        val userReturned: User = result as User
                                        mCallback.onSaveDone(userReturned)
                                    }
                                }

                                override fun onError(exception: java.lang.Exception) {
                                    mCallback.onError(exception)
                                }
                            })
            */

        }
    }


    fun setProgress(count: Int) {
        _currentData.sos_invocation_count = count
        validateSave()
        validateReset()
    }

    fun setMethod(method: SOSActivationMethods) {
        if (method != _sosInvocationMethod.value)
            _sosInvocationMethod.value = method
    }
}