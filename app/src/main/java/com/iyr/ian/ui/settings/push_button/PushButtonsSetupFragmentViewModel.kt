package com.iyr.ian.ui.settings.push_button

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iyr.ian.utils.bluetooth.ble.BLEState
import com.iyr.ian.utils.bluetooth.ble.BLEState.NOT_ENABLED

class PushButtonsSetupFragmentViewModel : ViewModel() {

    private val _bluetoothStatus = MutableLiveData<BLEState>(BLEState.OK)
    val bluetoothStatus: LiveData<BLEState> = _bluetoothStatus

    fun setBluetoothStatus(status: BLEState) {
        _bluetoothStatus.postValue(status)
    }


}