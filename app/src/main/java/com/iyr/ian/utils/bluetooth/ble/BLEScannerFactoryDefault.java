package com.iyr.ian.utils.bluetooth.ble;

import com.iyr.ian.utils.bluetooth.ble.BLEScannerDefault;
import com.iyr.ian.utils.bluetooth.ble.BLEScannerInterface;

class BLEScannerFactoryDefault implements BLEScannerFactoryInterface {

    @Override
    public BLEScannerInterface scanner(BLECentralManagerInterface manager) {
        return new BLEScannerDefault(manager);
    }
}
