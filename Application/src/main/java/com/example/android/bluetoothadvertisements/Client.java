package com.example.android.bluetoothadvertisements;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public class Client {

    private BluetoothDevice device;
    private BluetoothGatt gatt;
    private boolean connected = false;

    public Client(BluetoothDevice result) {
        this.device = result;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }
}
