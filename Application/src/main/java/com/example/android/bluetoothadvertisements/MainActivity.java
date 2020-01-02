/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothadvertisements;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Optional;

import static com.example.android.bluetoothadvertisements.Constants.Characteristic_UUID;
import static com.example.android.bluetoothadvertisements.Constants.Descriptor_UUID;
import static com.example.android.bluetoothadvertisements.Constants.Service_UUID;

/**
 * Setup display fragments and ensure the device supports Bluetooth.
 */
public class MainActivity extends FragmentActivity {

    private static final String TAG = FragmentActivity.class.getSimpleName();


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 167;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;


    public boolean areLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.activity_main_title);


        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            if(areLocationServicesEnabled(this)) {
                mHandler = new Handler();

                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                    finish();
                }

                final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();


            }
        }

        if (savedInstanceState == null) {

            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();

            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter.isEnabled()) {

                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.
                        setupFragments();

                    } else {

                        // Bluetooth Advertisements are not supported.
                        showErrorText(R.string.bt_ads_not_supported);
                    }
                } else {

                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                }
            } else {

                // Bluetooth is not supported.
                showErrorText(R.string.bt_not_supported);
            }
        }


        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        MyGattServerCallback myGattServerCallback = new MyGattServerCallback();

        BluetoothGattServer btGattServer = bluetoothManager.openGattServer(this, myGattServerCallback);

        myGattServerCallback.setGattServer(btGattServer);
        BluetoothGattService myService = new BluetoothGattService(Service_UUID.getUuid(), BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic myCharacteristic = new BluetoothGattCharacteristic(
                Characteristic_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        // myCharacteristic.setValue("Oh yeah bby");
        // myCharacteristic.addDescriptor(new BluetoothGattDescriptor(Descriptor_UUID.getUuid(), BluetoothGattDescriptor.PERMISSION_READ));
        myService.addCharacteristic(myCharacteristic);
        btGattServer.addService(myService);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:

                if (resultCode == RESULT_OK) {

                    // Bluetooth is now Enabled, are Bluetooth Advertisements supported on
                    // this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.
                        setupFragments();

                    } else {

                        // Bluetooth Advertisements are not supported.
                        showErrorText(R.string.bt_ads_not_supported);
                    }
                } else {

                    // User declined to enable Bluetooth, exit the app.
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        ScannerFragment scannerFragment = new ScannerFragment();
        // Fragments can't access system services directly, so pass it the BluetoothAdapter
        scannerFragment.setBluetoothAdapter(mBluetoothAdapter);
        transaction.replace(R.id.scanner_fragment_container, scannerFragment);

        AdvertiserFragment advertiserFragment = new AdvertiserFragment();
        transaction.replace(R.id.advertiser_fragment_container, advertiserFragment);

        transaction.commit();

        Log.d(TAG, "On activity result");
        final Button fetchButton = findViewById(R.id.fetchButton);
        final TextView textView = findViewById(R.id.lastMessage);
        fetchButton.setOnClickListener(v -> {
            Optional<Client> firstNeighbout = ClientList.getInstance().getFirst();
            if (!firstNeighbout.isPresent()) {
                Log.d(TAG, "no neighbour present");
                return;
            }

            Client client = firstNeighbout.get();
            String address = client.getDevice().getAddress();
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            Log.d(TAG, device.getName());
            Log.d(TAG, device.getAddress());


            if (client.isConnected()) {
                BluetoothGatt gatt = client.getGatt();
                if (gatt != null) {
                    BluetoothGattCharacteristic characteristic = gatt.getService(Service_UUID.getUuid()).getCharacteristic(Characteristic_UUID.getUuid());
                    gatt.readCharacteristic(characteristic);
                    return;
                }

            }


            device.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    Log.d(TAG, "connect state updated: " + status + ", " + newState);
                    //BluetoothGatt. 0 = success
                    //BluetoothProfile. disconnected, connecting connected
                    ClientList.getInstance().setConnected(address, newState == BluetoothProfile.STATE_CONNECTED);
                    ClientList.getInstance().setGatt(address, gatt);

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        gatt.discoverServices();
                    } else {
                        Log.d(TAG, "status is not success");
                    }

                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG, "services discovered");
                        BluetoothGattCharacteristic characteristic = gatt.getService(Service_UUID.getUuid()).getCharacteristic(Characteristic_UUID.getUuid());
                        gatt.readCharacteristic(characteristic);
                    } else {
                        Log.d(TAG, "failed to discover services");
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        Log.d(TAG, "characteristic: "  + characteristic.getStringValue(0));

                        Handler mainHandler = new Handler(MainActivity.this.getMainLooper());

                        mainHandler.post(() -> {
                            textView.setText(characteristic.getStringValue(0));
                        });

                    } else {
                        Log.d(TAG, "failed to discover services");
                    }
                }
            });


            Log.d(TAG, "fetch button");
        });
    }

    private void showErrorText(int messageId) {

        TextView view = (TextView) findViewById(R.id.error_textview);
        view.setText(getString(messageId));
    }
}