package com.mywork.bleapplication;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class BleRepository {
    MutableLiveData<List<BluetoothDevice>> mBluetoothDevices = new MutableLiveData<>();
    List<BluetoothDevice> mBluetoothDeviceslist = new ArrayList<>();
    BleModel bleModel;
    static BleRepository bleRepository;


    MutableLiveData<List<String>> mValues = new MutableLiveData<>();
    List<String> values = new ArrayList<>();
    private BleRepository() {

    }

    public static BleRepository getInstance() {
        if (bleRepository == null)
            bleRepository = new BleRepository();
        return bleRepository;
    }

    public MutableLiveData<List<BluetoothDevice>> getAllDevicesList() {
        return mBluetoothDevices;
    }

    public MutableLiveData<List<String>> getAllValuesList() {
        return mValues;
    }

    public void insert(BluetoothDevice bluetoothDevice) {
        mBluetoothDeviceslist = new ArrayList();
        mBluetoothDeviceslist.add(bluetoothDevice);

        mBluetoothDevices.postValue(mBluetoothDeviceslist);
    }

    public void insertValue(String val) {
       // values = new ArrayList();
        values.add(val);

        mValues.postValue(values);
    }
}
