package com.iyr.ian.utils.bluetooth.ble;

public enum BLEError {
    ok,
    timeout,
    noPeripheral,
    noAdapter,
    noGatt,
    notConnected,
    noImmediateAlertCharacteristic,
    noFindMeAlertCharacteristic,
    badStatus
}
