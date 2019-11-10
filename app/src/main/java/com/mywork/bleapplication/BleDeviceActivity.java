package com.mywork.bleapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.mywork.bleapplication.Constants.REQUEST_ENABLE_BT;
import static com.mywork.bleapplication.Constants.REQUEST_FINE_LOCATION;


public class BleDeviceActivity extends AppCompatActivity implements DeviceRecyclerViewAdapter.ItemClickListener {
    private BluetoothAdapter mBluetoothAdapter;
    Boolean isGPS = false;
    BleModel bleModel;
    RecyclerView recyclerView;
    DeviceRecyclerViewAdapter deviceRecyclerViewAdapter;
    TextView textViewResult;
    BleDeviceViewModel bleDeviceViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        textViewResult = (TextView) findViewById(R.id.textViewNotification);

        deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter(BleDeviceActivity.this);
        deviceRecyclerViewAdapter.setClickListener(BleDeviceActivity.this);
        recyclerView.setAdapter(deviceRecyclerViewAdapter);
        // check device supports BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {

            // Bluetooth adapter
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();


            // check permissions and enable bluetooth and location
            if (hasPermissions()) {
                new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
                    @Override
                    public void gpsStatus(boolean isGPSEnable) {
                        // turn on GPS
                        isGPS = isGPSEnable;
                    }
                });
            }
            bleDeviceViewModel = ViewModelProviders.of(this).get(BleDeviceViewModel.class);

            bleDeviceViewModel.getAllDevicesList().observe(this, new Observer<List<BluetoothDevice>>() {
                @Override
                public void onChanged(List<BluetoothDevice> bluetoothDevices) {
                    // Update the cached copy of the words in the adapter.

                    deviceRecyclerViewAdapter.SetDeviceList(bluetoothDevices);
                    //textViewPersonCount.setText("("+String.valueOf(persons.size())+")");
                }
            });

            bleDeviceViewModel.getAllValuesList().observe(this, new Observer<List<String>>() {
                @Override
                public void onChanged(List<String> values) {
                    // Update the cached copy of the words in the adapter.
                    textViewResult.setText(values.get(values.size() - 1));

//                    if(newFragment==null) {
//                        newFragment = new GraphFragment();
//                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                        // Replace whatever is in the fragment_container view with this fragment,
//                        // and add the transaction to the back stack so the user can navigate back
//                        transaction.replace(R.id.frameLayoutFragmentContainer, newFragment);
//                        transaction.addToBackStack(null);
//
//                        // Commit the transaction
//                        transaction.commit();
//                    }
                }
            });

            bleModel = new BleModel(this, mBluetoothAdapter);
        }

        textViewResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(BleDeviceActivity.this, DynamicalAddingActivity.class);
//                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            bleModel.scanLeDevice(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            bleModel.scanLeDevice(false);
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(View view, int position) {
        bleModel.scanLeDevice(true);
    }

    @Override
    public void onConnectButtonClick(View view, BluetoothDevice bluetoothDevice) {
        bleModel.connectToDevice(bluetoothDevice);
    }

    @Override
    public void onStartButtonClick(View view, BluetoothDevice bluetoothDevice) {
        bleModel.writeCharacteristic(01);
    }

    @Override
    public void onStopButtonClick(View view, BluetoothDevice bluetoothDevice) {
        bleModel.writeCharacteristic(00);
    }


    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.i("ENABLE_BT", "Requested user enables Bluetooth. Try starting the scan again.");
    }

    private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        Log.i("FINE_LOCATION", "Requested user enable Location. Try starting the scan again.");
    }


    public void updateTextView(String str) {
        textViewResult.setText(str);
    }
}
