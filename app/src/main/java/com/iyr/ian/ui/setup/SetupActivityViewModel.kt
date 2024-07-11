package com.iyr.fewtouchs.ui.views.setup

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.iyr.ian.AppConstants
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.implementations.auth.firebase.AuthenticationRepositoryImpl
import com.iyr.ian.enums.ScreensEnum
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetupActivityViewModel : ViewModel() {

    private var _originalData: User? = null
    private lateinit var _currentData: User

    private val _viewStatus = MutableLiveData<Resource<User?>>()
    val viewStatus: LiveData<Resource<User?>> get() = _viewStatus

    private val _displayName = MutableLiveData<String>()
    val diaplayName: LiveData<String> get() = _displayName

    private val _displayNameMinCharsRequired = MutableLiveData<Boolean>()
    val displayNameMinCharsRequired: LiveData<Boolean> get() = _displayNameMinCharsRequired


    private val _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> get() = _phoneNumber

    private val _image = MutableLiveData<Resource<MediaFile?>>()
    val image: LiveData<Resource<MediaFile?>> get() = _image

    private val _authToken = MutableLiveData<String>()
    val authToken: LiveData<String> get() = _authToken

    private val _isFirstSetup = MutableLiveData<Boolean>()
    val isFirstSetup: LiveData<Boolean> get() = _isFirstSetup

    private val _saveButtonEnabled = MutableLiveData<Boolean>()
    val saveButtonEnabled: LiveData<Boolean> get() = _saveButtonEnabled

    private val _screenToShow = MutableLiveData<ScreensEnum>()
    val screenToShow: LiveData<ScreensEnum> get() = _screenToShow

    private val authRepository: AuthenticationRepositoryImpl = AuthenticationRepositoryImpl()
    private var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()

    fun setExtraData(bundle: Bundle) {
        if (bundle.containsKey("data_object")) {
            _currentData = Gson().fromJson(bundle.getString("data_object"), User::class.java)
            _originalData = Gson().fromJson(bundle.getString("data_object"), User::class.java)
            _displayName.value = _currentData.display_name ?: ""
            _phoneNumber.value = _currentData.telephone_number ?: ""
            _image.value = Resource.Success(_currentData.image)
            _displayNameMinCharsRequired.value =
                _displayName.value!!.length < AppConstants.USERNAME_MINIMUM_LENGTH

        }
        _isFirstSetup.value = if (bundle.containsKey("first_setup")) {
            bundle.getBoolean("first_setup", false)
        } else {
            true
        }
    }

    fun getCurrentData(): User {
        return _currentData
    }

    fun onDisplayNameChanged(text: String?) {
        _currentData.display_name = text ?: ""
        //    _diaplayName.value = _currentData?.display_name  ?: ""

        validateData(true)
    }

    fun onPhoneNumberChanged(text: String?) {
        _currentData.telephone_number = text ?: ""

        validateData(true)
    }


    fun onImageChanged(image: MediaFile) {
    }

    fun onImageChanged(userKey: String, image: MediaFile) {
        _image.postValue(Resource.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {

            usersRepository.onUpdateUserImage(userKey, image)
            _currentData.image = image
            _image.postValue(Resource.Success(_currentData.image))

            validateData(true)

        }
        //  _image.postValue(image)
    }


    /**
     * Valida que la informacion requerida este completa
     *
     * @param silent
     */
    private fun validateData(silent: Boolean): Boolean {
        val displayName: String = _currentData.display_name ?: ""
        val telephoneNumber: String = _currentData.telephone_number.toString()
        val image: MediaFile? = _currentData.image
        _displayNameMinCharsRequired.postValue(
            displayName.length >= AppConstants.USERNAME_MINIMUM_LENGTH
        )
        //           _showSpeedDialSwitch.value =
        //               (telephoneNumber.isEmpty() == false && Validators.isValidPhoneNumber(telephoneNumber))
        val isOkToProceed =
            image?.file_name != null &&
                    displayName.length >= AppConstants.USERNAME_MINIMUM_LENGTH &&
                    Validators.isValidPhoneNumber(telephoneNumber)

        _saveButtonEnabled.postValue(isOkToProceed)
        return isOkToProceed
    }

    fun onSaveButtonClicked() {
        _viewStatus.value = Resource.Loading<User?>()
        viewModelScope.launch(Dispatchers.IO) {
//lo saco porque ya viene cargado el base 64
            /*
                      if (_currentData.image?.file_name != _originalData?.image?.file_name) {


                          val fileUri = _currentData.image?.localFullPath


                          val mediaFileEncoded =
                              MultimediaUtils(AppClass.instance).convertFileToBase64(Uri.parse(fileUri)).toString()
                          _currentData.image?.bytesB64 = mediaFileEncoded


                      }
          */
            try {
                var updateCall = usersRepository.saveUser(_currentData, _originalData)

                try {
                    var authToken = authRepository.getAuthToken()
                    Log.d("TOKEN_AUTHENTICATION =", authToken.toString())
                    _authToken.postValue(authToken.data.toString())
                    val user = _currentData
                    _viewStatus.postValue(Resource.Success<User?>(user))
                } catch (exception: Exception) {
                    _viewStatus.postValue(Resource.Error<User?>(exception.localizedMessage.toString()))

                }

            } catch (exception: Exception) {
                _viewStatus.postValue(Resource.Error<User?>(exception.localizedMessage.toString()))
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

    fun onFirstNameChanged(text: String?) {
        _currentData.first_name = text ?: ""
    }

    fun onLastNameChanged(text: String?) {
        _currentData.last_name = text ?: ""
    }


}