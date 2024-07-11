package com.iyr.ian.utils.bluetooth.ble;

import androidx.annotation.NonNull;

import com.iyr.ian.utils.bluetooth.ble.rasat.java.Observable;



public interface BLEConnectionInterface extends AutoCloseable {
    Observable<AlertVolume> observableImmediateAlert();
    Observable<Integer> observableClick();
    Observable<Integer> observableRSSI();
    Observable<BLEConnectionState> observableState();

    @NonNull
    String id();
    boolean isConnected();
    boolean isDisconnected();
    BLEError connect() throws InterruptedException;
    BLEError disconnect( int timeout);
    BLEError disconnect();
    BLEError connect(boolean infinity);
    BLEError writeImmediateAlert(AlertVolume volume, int timeout);
    BLEError writeImmediateAlert(AlertVolume volume);
    void enableRSSI();
    void disableRSSI();
    boolean rssiEnabled();
    int rssi();
    int getLastStatus();
    BLEConnectionState state();
    boolean isAlerting();
    boolean isFindMe();
    void resetFindeMe();

    void isFindingActive(boolean active);

    boolean isFindingActive();
}
