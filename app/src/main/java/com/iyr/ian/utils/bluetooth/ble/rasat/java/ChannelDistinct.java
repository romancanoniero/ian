package com.iyr.ian.utils.bluetooth.ble.rasat.java;

public class ChannelDistinct<T> extends Channel<T> {

    public ChannelDistinct(T value) {
        super(value);
    }

    public ChannelDistinct() {
        super();
    }

    @Override
    public void broadcast(T value) {
        if (value == null && observable.value == null) {
            return;
        }
        if (value != null && value.equals(observable.value)) {
            return;
        }
        super.broadcast(value);
    }
}
