package com.iyr.ian.utils.bluetooth.ble;

interface BLEPeripheralInterace extends AutoCloseable {
    String identifier();
    String name();
    String address();
    boolean isConnected();
    boolean isDisconnected();
    BLEService[] services();

    BLEPeripheralObservablesInterface observables();

    void connect(boolean auto);
    void disconnect();
    void discoveryServices();
    BLEError writeInt8(BLECharacteristic characteristic, int value);
    BLEError setNotify(BLECharacteristic characteristic, boolean enable);
    void enableRSSI();
    void disableRSSI();
    boolean rssiEnabled();

    boolean cached();
}
