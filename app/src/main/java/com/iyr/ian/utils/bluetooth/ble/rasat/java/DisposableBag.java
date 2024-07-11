package com.iyr.ian.utils.bluetooth.ble.rasat.java;

import java.util.HashSet;
import java.util.Set;

public class DisposableBag implements AutoCloseable {
    private final Set<AutoCloseable> bag = new HashSet<>();

    public void add(AutoCloseable disposable) {
        bag.add(disposable);
    }

    public void dispose() {
        for (AutoCloseable disposable : bag) {
            try {
                disposable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bag.clear();
    }

    public void close() {
        dispose();
    }
}
