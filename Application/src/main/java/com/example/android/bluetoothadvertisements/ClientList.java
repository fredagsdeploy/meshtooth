package com.example.android.bluetoothadvertisements;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;

public class ClientList extends Observable {
    private static String TAG = ClientList.class.getSimpleName();

    private static ClientList instance = null;

    private Map<String, Client> results = new HashMap<>();

    private ClientList() {}

    public static ClientList getInstance() {
        if (instance == null) {
            instance = new ClientList();
        }
        return instance;
    }

    public List<Client> values() {
        return new ArrayList<>(results.values());
    }


    public void add(BluetoothDevice device) {
        results.put(device.getAddress(), new Client(device));
        setChanged();
        notifyObservers(this);
        Log.d(TAG, "add device thanks please " + device.getName());
    }

    public void setConnected(String id, boolean connected) {
        Client client = results.get(id);
        if (client != null) {
            client.setConnected(connected);
            setChanged();
            notifyObservers(this);
        }
    }

    public Optional<Client> getFirst() {
        return results.values().stream().findFirst();
    }

    public void setGatt(String id, BluetoothGatt gatt) {
        Client client = results.get(id);
        if (client != null) {
            client.setGatt(gatt);
        }
    }
}
