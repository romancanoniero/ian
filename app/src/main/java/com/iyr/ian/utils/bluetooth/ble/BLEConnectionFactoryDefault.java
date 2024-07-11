package com.iyr.ian.utils.bluetooth.ble;

import androidx.annotation.NonNull;

import com.iyr.ian.utils.bluetooth.ble.BLEConnectionFactoryInterface;
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionInterface;
import com.iyr.ian.utils.bluetooth.ble.BLEPeripheralInterace;

class BLEConnectionFactoryDefault implements BLEConnectionFactoryInterface {
    @Override
    public BLEConnectionInterface connection(
            @NonNull BLECentralManagerInterface manager,
            @NonNull String id) {

        return new BLEConnectionDefault(manager, id);
    }

    @Override
    public BLEConnectionInterface connection(@NonNull BLECentralManagerInterface manager,
                                             @NonNull BLEPeripheralInterace peripheral) {
        return new BLEConnectionDefault(manager, peripheral);
    }

}
