package com.example.android.bluetoothadvertisements;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

class MyGattServerCallback extends BluetoothGattServerCallback {
    private static final String TAG = FragmentActivity.class.getSimpleName();
    private BluetoothGattServer gattServer;

    private int counter = 0;

    public void setGattServer(BluetoothGattServer gattServer) {
        this.gattServer = gattServer;
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        boolean b = gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, String.format("%d", counter++).getBytes(StandardCharsets.UTF_8));
        Log.d(TAG, "sendResponse " + b);
    }
}
