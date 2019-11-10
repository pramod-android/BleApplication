package com.mywork.bleapplication;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class BleDeviceViewModel extends AndroidViewModel {
    BleRepository bleRepository;
   MutableLiveData<List<BluetoothDevice>> mBluetoothDevices=new MutableLiveData<>();
    MutableLiveData<List<String>> values=new MutableLiveData<>();

    public BleDeviceViewModel(@NonNull Application application) {
        super(application);
        bleRepository = BleRepository.getInstance();
        mBluetoothDevices= bleRepository.getAllDevicesList();
        values= bleRepository.getAllValuesList();
    }



    public MutableLiveData<List<BluetoothDevice>> getAllDevicesList() {
        return mBluetoothDevices;
    }

    public MutableLiveData<List<String>> getAllValuesList() {
        return values;
    }


//    public void insert(BluetoothDevice bluetoothDevice) {
//        bleRepository.insert(bluetoothDevice);
//    }
}
