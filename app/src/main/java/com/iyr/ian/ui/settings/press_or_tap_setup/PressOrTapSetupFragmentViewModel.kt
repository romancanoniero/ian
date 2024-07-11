package com.iyr.ian.ui.settings.press_or_tap_setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyr.ian.dao.models.User
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.ui.setup.press_or_tap_setup.SOSActivationMethods
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PressOrTapSetupFragmentViewModel : ViewModel() {

    private val usersRepository = UsersRepositoryImpl()


    private var _user: User? = null

    private val _counter = MutableLiveData<Int>()
    val counter: LiveData<Int> = _counter

    private val _buttonMode = MutableLiveData<SOSActivationMethods?>()
    val buttonMode: LiveData<SOSActivationMethods?> = _buttonMode

    private val _savingStatus = MutableLiveData<Resource<Boolean?>?>()
    val savingStatus: LiveData<Resource<Boolean?>?> = _savingStatus


    fun setCounter(count: Int) {
        _counter.postValue(count)
    }

    fun incCounter() {
        _counter.value = _counter.value?.plus(1)
    }

    fun resetCounter() {
        _counter.value = 0
    }


    fun setButtonMode(mode: SOSActivationMethods) {
        _buttonMode.postValue(mode)
    }

    fun onSaveClick() {
        viewModelScope.launch(Dispatchers.IO)
        {
            _savingStatus.postValue(Resource.Loading())
            val dataMap = HashMap<String, Any>()
            dataMap["sos_invocation_method"] =
                _buttonMode.value?.name ?: SOSActivationMethods.TAP.name
            dataMap["sos_invocation_count"] = _counter.value ?: 3
            val call = usersRepository.updateUserDataByMap(_user?.user_key.toString(), dataMap)
            if (call.message == null)
                _savingStatus.postValue(Resource.Success<Boolean?>(true))
            else
                _savingStatus.postValue(Resource.Error<Boolean?>(call.message.toString()))
        }
    }

    fun setUser(user: User?) {
        _user = user
    }

    fun getUser(): User? {
        return _user
    }

    fun resetSavingStatus() {
        _savingStatus.value = null
    }


}