package com.iyr.ian.utils.bluetooth.ble;

interface BLEScannerFactoryInterface {
    BLEScannerInterface scanner(BLECentralManagerInterface manager);
}
