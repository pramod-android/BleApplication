package com.mywork.bleapplication;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import static com.mywork.bleapplication.Constants.CHARACTERISTIC_NOTIFY_UUID;
import static com.mywork.bleapplication.Constants.CHARACTERISTIC_UUID;
import static com.mywork.bleapplication.Constants.SCAN_PERIOD;
import static com.mywork.bleapplication.Constants.SERVICE_UUID;

public class BleModel {

    private static final String TAG = "BleModel";
    Context mContext;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;

    private ScanSettings settings;
    private List<ScanFilter> filters = new ArrayList<>();
    private BluetoothGatt mGatt;
    LiveData<List<BluetoothDevice>> mBluetoothDevices;

    List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    BleRepository bleRepository;
    List<BluetoothGattService> services;
    BluetoothGattCharacteristic characteristic;

    private Handler handler;
    private Runnable runnable;
    private TestResultsViewModel testResultsViewModel;

    public BleModel(Context mContext, BluetoothAdapter bluetoothAdapter) {
        this.mContext = mContext;
        this.mBluetoothAdapter = bluetoothAdapter;
        mHandler = new Handler();
        this.mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        bleRepository = BleRepository.getInstance();
        testResultsViewModel = new ViewModelProvider((FragmentActivity) mContext).get(TestResultsViewModel.class);

    }

    public BleModel(Application context) {

        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = bluetoothManager.getAdapter();
        this.mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {

                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            String deviceName = btDevice.getName();
            if (deviceName != null) {
                    mLEScanner.stopScan(mScanCallback);
                    if (!bluetoothDevices.contains(btDevice)) {
                        bluetoothDevices.add(btDevice);
                        bleRepository.insert(btDevice);
                    }

            }


        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    connectToDevice(device);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.i("onLeScan", device.toString());
//                            connectToDevice(device);
//                        }
//                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(mContext, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");

                    gatt.discoverServices();
                    ((BleDeviceActivity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    ((BleDeviceActivity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Disconnected", Toast.LENGTH_SHORT).show();
                        }
                    });

                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            services = gatt.getServices();
            mGatt = gatt;

            for (BluetoothGattService service : services) {
                if (service.getUuid().equals(SERVICE_UUID)) {
                    characteristic = service.getCharacteristic(CHARACTERISTIC_NOTIFY_UUID);
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mGatt.writeDescriptor(descriptor);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                    }
                    mGatt.setCharacteristicNotification(characteristic, true);
                }
            }


            List<BluetoothGattService> services = gatt.getServices();


            Log.i("onServicesDiscovered", services.toString());
            //  gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final byte[] messageBytes = characteristic.getValue();
            ((BleDeviceActivity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            testResultsViewModel.getCurrentValue().setValue(messageBytes);
                        }
                    };
                    handler.postDelayed(runnable, 200);
                }
            });

//            Byte b1 = messageBytes[3];
//            Byte b2 = messageBytes[4];
//
//            int val;//=((messageBytes[3]+256)+((messageBytes[4]+256)*256));
//            int firstVal = b1.byteValue();
//            if (firstVal < 0) {
//                firstVal = firstVal + 256;
//                val = ((messageBytes[3] + 256) + ((messageBytes[4] + 256) * 256));
//            } else {
//                int secVal = b2.byteValue() + 256;
//                val = ((messageBytes[3]) + ((messageBytes[4] + 256) * 256));
//            }
//
//            Log.i(TAG, String.valueOf(val));
//            bleRepository.insertValue(String.valueOf(val));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };

    public static int twoBytesToShort(byte b1, byte b2) {

        int val = (b1 & 0xFF) + (b2 & 0xFF) << 8;

        return val; //(short) ((b1 << 8) | (b2 & 0xFF));
    }

    public void ScanBleDevice() {
        //add following null check in activity

//        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        } else {
        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
//        }
            scanLeDevice(true);
        }
    }

    public void StopScanBleDevice() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }


    public LiveData<List<BluetoothDevice>> getDevices() {

        //    mBluetoothDevices=new MediatorLiveData<>();
        mBluetoothDevices = (LiveData<List<BluetoothDevice>>) bluetoothDevices;
        return mBluetoothDevices;
    }


    public boolean writeCharacteristic(int val) {
        boolean status = false;
        //check mBluetoothGatt is available
        if (mGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        } else {


            BluetoothGattService Service = mGatt.getService(SERVICE_UUID);
            if (Service == null) {
                Log.e(TAG, "service not found!");
                return false;
            }
            BluetoothGattCharacteristic charac = Service
                    .getCharacteristic(CHARACTERISTIC_UUID);
            if (charac == null) {
                Log.e(TAG, "char not found!");
                return false;
            }


            // int val = Integer.valueOf(startStopVal);
            byte[] value = new byte[1];
            value[0] = (byte) (val & 0xFF);
            charac.setValue(value);
            status = mGatt.writeCharacteristic(charac);


//            String message = editText.getText().toString();
//            byte[] messageBytes = new byte[4];
//            try {
//                messageBytes = message.getBytes("UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                Log.e(TAG, "Failed to convert message string to byte array");
//            }
//
//            charac.setValue(messageBytes);
//            status = mGatt.writeCharacteristic(charac);


            if (status) {
                Log.i(TAG, String.valueOf(val));
                Toast.makeText(mContext, "Requested with" + val, Toast.LENGTH_SHORT).show();
                if (val == 00) {
                    mGatt.disconnect();
                    mGatt = null;
                }
            }
            // textViewValue.setText("Written value :" + editText.getText().toString());
        }
        return status;
    }
}
