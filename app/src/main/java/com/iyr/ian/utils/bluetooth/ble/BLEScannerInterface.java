package com.iyr.ian.utils.bluetooth.ble;


import com.iyr.ian.utils.bluetooth.models.BLEScanResult;
import com.iyr.ian.utils.bluetooth.ble.rasat.java.Observable;

public interface BLEScannerInterface {
    boolean isScanning();
    Observable<Integer> observableTimer();
    Observable<BLEScanResult> observableScan();
    Observable<Boolean> observableActive();

    void start(int timeout, String[] forceCancelIds);
    void stop();
}
