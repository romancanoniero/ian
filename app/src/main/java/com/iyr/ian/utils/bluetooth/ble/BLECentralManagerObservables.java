package com.iyr.ian.utils.bluetooth.ble;

import com.iyr.ian.utils.bluetooth.ble.rasat.Channel;
import com.iyr.ian.utils.bluetooth.ble.rasat.java.Observable;


class BLECentralManagerObservables implements BLECentralManagerObservablesInterface {
    final Channel<BLEDiscoveryResult> observablePeripheralDiscovered = new Channel<>();
    @Override
    public Observable<BLEDiscoveryResult> observablePeripheralDiscovered() {
        return observablePeripheralDiscovered.observable;
    }
}
