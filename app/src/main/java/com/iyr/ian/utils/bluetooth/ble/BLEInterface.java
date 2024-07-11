package com.iyr.ian.utils.bluetooth.ble;

import androidx.annotation.NonNull;

import com.iyr.ian.utils.bluetooth.ble.BLEConnectionInterface;
import com.iyr.ian.utils.bluetooth.ble.BLEScannerInterface;
import com.iyr.ian.utils.bluetooth.ble.BLEState;
import com.iyr.ian.utils.bluetooth.ble.rasat.java.Observable;


public interface BLEInterface extends AutoCloseable {
    BLEScannerInterface scanner();
    @NonNull
    BLEState state();
    @SuppressWarnings("UnusedReturnValue")
    BLEError enable();
    Observable<BLEState> observableState();
    @NonNull
    BLEConnectionInterface connectionById(String id);
}
