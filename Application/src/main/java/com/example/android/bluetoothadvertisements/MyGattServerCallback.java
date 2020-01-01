package com.example.android.bluetoothadvertisements;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

class MyGattServerCallback extends BluetoothGattServerCallback {
    private BluetoothGattServer gattServer;

    int counter = 0;

    public void setGattServer(BluetoothGattServer gattServer) {
        this.gattServer = gattServer;
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, String.format("Suck it bitch %d", counter++).getBytes(StandardCharsets.UTF_8));
    }
}
