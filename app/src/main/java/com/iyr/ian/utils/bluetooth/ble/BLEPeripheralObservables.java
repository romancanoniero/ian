package com.iyr.ian.utils.bluetooth.ble;

import com.iyr.ian.utils.bluetooth.ble.rasat.Channel;
import com.iyr.ian.utils.bluetooth.ble.rasat.java.Observable;



class BLEPeripheralObservables implements BLEPeripheralObservablesInterface {

    final Channel<ConnectedEvent> channelConnected = new Channel<>();
    final Channel<ConnectionFailedEvent> channelConnectionFailed = new Channel<>();
    final Channel<DisconnectedEvent> channelDisconnected = new Channel<>();
    final Channel<DiscoveredServicesEvent> channelDiscoveredServices = new Channel<>();
    final Channel<CharacteristicEvent> channelWrite = new Channel<>();
    final Channel<BLECharacteristic> channelNotification = new Channel<>();
    final Channel<RSSIEvent> channelRSSI = new Channel<>();

    @Override
    public Observable<ConnectedEvent> observableConnected() {
        return channelConnected.observable;
    }

    @Override
    public Observable<ConnectionFailedEvent> observableConnectionFailed() {
        return channelConnectionFailed.observable;
    }

    @Override
    public Observable<DisconnectedEvent> observableDisconnected() {
        return channelDisconnected.observable;
    }

    @Override
    public Observable<DiscoveredServicesEvent> observableDiscoveredServices() {
        return channelDiscoveredServices.observable;
    }

    @Override
    public Observable<CharacteristicEvent> observableWrite() {
        return channelWrite.observable;
    }

    @Override
    public Observable<BLECharacteristic> observableNotification() {
        return channelNotification.observable;
    }

    @Override
    public Observable<RSSIEvent> observableRSSI() {
        return channelRSSI.observable;
    }
}
