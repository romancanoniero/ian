package com.iyr.ian.utils.bluetooth.ble;

//import com.iyr.fewtouchs.bluetooth.rasat.java.Observable;

import com.iyr.ian.utils.bluetooth.ble.rasat.java.Observable;

interface BLECentralManagerObservablesInterface {
    Observable<BLEDiscoveryResult> observablePeripheralDiscovered();
}
