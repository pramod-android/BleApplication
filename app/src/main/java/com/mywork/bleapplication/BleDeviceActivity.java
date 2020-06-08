package com.mywork.bleapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.mywork.bleapplication.Constants.REQUEST_ENABLE_BT;
import static com.mywork.bleapplication.Constants.REQUEST_FINE_LOCATION;


public class BleDeviceActivity extends AppCompatActivity implements DeviceRecyclerViewAdapter.ItemClickListener, OnChartValueSelectedListener {
    private BluetoothAdapter mBluetoothAdapter;
    Boolean isGPS = false;
    BleModel bleModel;
    RecyclerView recyclerView;
    DeviceRecyclerViewAdapter deviceRecyclerViewAdapter;
    TextView textViewResult;
    BleDeviceViewModel bleDeviceViewModel;
    private int prevValue = 0;
    private boolean startFlag = false;
    private int count = 0;
    private double prevSec = 0;
    String TAG = "BleDeviceActivity";
    private AirGraphItem airGraphItem;
    private List<AirGraphItem> airGraphItemList = new ArrayList<>();
    private int plateuaPos = 0;

    private double x1 = 0, x2 = 0, x3 = 0;
    private double n1 = 0, n2 = 0, n3 = 0, n4 = 0, n5 = 0;
    public static final double meterPerSec = (Math.PI * 12.26 * Math.cos(Math.toRadians(31.78))) / 1000;//*Math.cos(Math.toRadians(40))
    public static final Double squireMeterPerSec = (Math.PI * (12.26 * 12.26)) / 1000000;// 0.000472205251;
    public static final double flowStart = 0.1;
    public static final double flowStop = 0.1;
    public static final int secToStopData = 15;
    private String endBy = "User stopped blowing";
    private LineChart chartFlow;

    double n6 = 0, n7 = 0, n8 = 0, n9 = 0, n10 = 0;
    ArrayList<Entry> flowValues = new ArrayList<>();
    FileWriter fileWriter;
  //  File sdCardFile;// = new File(Environment.getExternalStorageDirectory() + " \filename.txt");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        textViewResult = (TextView) findViewById(R.id.textViewNotification);
        chartFlow = findViewById(R.id.chartFlow);
        SetupChartFlow();
        checkHavePermission();
                File root = new File(getExternalFilesDir(null), "SpiromedData");
        if (!root.exists()) {
            root.mkdirs();
        }
        File gpxfile = new File(root, "SpiromedData.csv");
        try {
            fileWriter = new FileWriter(gpxfile);
        } catch (IOException e) {
            e.printStackTrace();
        }


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
            TestResultsViewModel testResultsViewModel = ViewModelProviders.of(Objects.requireNonNull(this)).get(TestResultsViewModel.class);

            final Observer<byte[]> nameObserver = this::ConvertData;
            // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
            testResultsViewModel.getCurrentValue().observe(this, nameObserver);

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
        n6 = 0;
        n7 = 0;
        n8 = 0;
        n9 = 0;
        n10 = 0;
        flowValues = new ArrayList<>();
    }

    @Override
    public void onStartButtonClick(View view, BluetoothDevice bluetoothDevice) {
        bleModel.writeCharacteristic(01);
    }

    @Override
    public void onStopButtonClick(View view, BluetoothDevice bluetoothDevice) {
        bleModel.writeCharacteristic(00);

//        setData(airGraphItemList);

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

    private void ConvertData(byte[] messageBytes) {//, long date
        synchronized (messageBytes) {
            for (int i =0; i < messageBytes.length; i++) {
                byte b1 = messageBytes[i];
                byte b2 = messageBytes[++i];
                int b1int = b1;
                if (b1 < 0)
                    b1int = (int) b1 + 256;
                int b2int = b2;
                if (b2 < 0)
                    b2int = (int) b2 + 256;

                String b1hex = decToHex(b1int);
                if (b1hex.length() <= 1) {
                    b1hex = "0" + b1hex;
                }
                String b2hex = decToHex(b2int);
                if (b2hex.length() <= 1) {
                    b2hex = "0" + b2hex;
                }

                // Concatenate both strings
                String hexDirection = b2hex + b1hex;// b4hex +b3hex +

                byte b3 = messageBytes[++i];
                byte b4 = messageBytes[++i];
                int b3int = b3;
                if (b3 < 0)
                    b3int = (int) b3 + 256;
                int b4int = b4;
                if (b4 < 0)
                    b4int = (int) b4 + 256;

                String b3hex = decToHex(b3int);
                if (b3hex.length() <= 1) {
                    b3hex = "0" + b3hex;
                }
                String b4hex = decToHex(b4int);
                if (b4hex.length() <= 1) {
                    b4hex = "0" + b4hex;
                }

                // Concatenate both strings
                String hexValue =  b4hex +b3hex;


                //hex to int
                int intInterrupt = Integer.parseInt(hexValue, 16);
                int intDirection = Integer.parseInt(hexDirection, 16);

                int diff = GetTimeBetweenInterupts(intInterrupt);
                // Ignore negative values (Spikes)

                    if (diff >= 1) {
                        CalculateFlowVolumeSec(diff,intDirection);
                    }


            }
        }
    }

    private static String decToHex(int dec) {
        return Integer.toHexString(dec);
    }

    private int GetTimeBetweenInterupts(int val) {
        int interupt = val - prevValue;
        prevValue = val;
        return interupt;
    }

    private void CalculateFlowVolumeSec(int val, int direction) {
        try {

            if(direction==1){
                val *= -1;
            }

            //Microsecond to second
            double second = (double) val * 10 / 1000000;
            double literPerSec_flow;
            //Calculation for flow
            literPerSec_flow = (squireMeterPerSec * meterPerSec / second) * 1000;
            if (literPerSec_flow != 0) {
                //Calculate Median
                double median_flow;
                x1 = x2;
                x2 = x3;
                x3 = literPerSec_flow;
                if (x1 != 0) {
                    double[] mFlowList = {x1, x2, x3};
                    median_flow = median(mFlowList);
                } else {
                    median_flow = literPerSec_flow;
                }
                //Get average
                n1 = n2;
                n2 = n3;
                n3 = n4;
                n4 = n5;
                n5 = median_flow;
                double final_flow = 0;
                if (n1 != 0) {
                    final_flow = (n1 + n2 + n3 + n4 + n5) / 5;
                } else if (n2 != 0) {
                    final_flow = (n2 + n3 + n4 + n5) / 4;
                } else if (n3 != 0) {
                    final_flow = (n3 + n4 + n5) / 3;
                } else if (n4 != 0) {
                    final_flow = (n4 + n5) / 2;
                } else if (n5 != 0) {
                    final_flow = median_flow;
                }

                if (!startFlag) {
                    if (final_flow != flowStart) {
                        startFlag = true;
                    }
                }

                if (startFlag) {
                    second = second + prevSec;
                    prevSec = second;
                    count++;
                    //Calculation for volume
                    double volume = count * meterPerSec * squireMeterPerSec * 1000;

                    airGraphItem = new AirGraphItem();
                    airGraphItem.setSecond(Math.round(second * 100) / 100D);
                    airGraphItem.setFlow(Math.round(final_flow * 100) / 100D);
                    airGraphItem.setVolume(Math.round(volume * 100) / 100D);
                    Log.i(TAG, "Received  Flw " + airGraphItem.getFlow() + " vol " + airGraphItem.getVolume() + " Sec " + airGraphItem.getSecond());
                    airGraphItemList.add(airGraphItem);
                    textViewResult.setText("Flow" + airGraphItem.getFlow() + "Volume" + airGraphItem.getVolume() + "Second" + airGraphItem.getSecond());
                    setData(airGraphItem);
                    if (plateuaPos == 0) {
                        if (airGraphItem.getSecond() >= 1) {
                            plateuaPos = 1;
                        }
                    } else {
                        plateuaPos++;
                    }

                    try {
                        fileWriter.write(System.getProperty("line.separator"));
                        fileWriter.append("Direction ,"+direction+" ,Time bttween interrupt ,"+val+ " ,Flow ,"+airGraphItem.getFlow()+" ,Volume ,"+airGraphItem.getVolume()+" ,Sec ,"+airGraphItem.getSecond());
                        fileWriter.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.i(TAG, " plateuaPos " + plateuaPos);
                    Log.i(TAG, " plateuaPos list size " + airGraphItemList.size());
                    if (second >= secToStopData || final_flow < flowStop) {
                        startFlag = false;
                        // bleModel.writeCharacteristic(00);
                        if (second >= secToStopData) {
                            endBy = " reached FET (Forced expiratory time) 15 sec";
                        } else {
                            endBy = " Flow reaches below 0.1 sec";
                        }
                    } else {
                        if (airGraphItem.getSecond() >= 1) {
                            if (airGraphItem.getVolume() - airGraphItemList.get(plateuaPos).getVolume() < 0.0025) {
                                startFlag = false;
                                //  bleModel.writeCharacteristic(00);
                                endBy = "Palteu achived (last sec volume < 0.0025";
                            }
                        }
                    }
                }
            }
        } catch (ArithmeticException e) {
            Log.i(TAG, "ArithmeticException occured!" + e);
        }
    }

    private static double median(double[] m) {
        Arrays.sort(m);
        int middle = (m.length - 1) / 2;
        if (m.length % 2 == 1) {
            return m[middle];
        } else {
            return (m[middle - 1] + m[middle]) / 2.0;
        }
    }

    private void setDataChartFlow(ArrayList<Entry> values) {
        LineDataSet set1;
        if (chartFlow.getData() != null &&
                chartFlow.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chartFlow.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chartFlow.getData().notifyDataChanged();
            chartFlow.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "Flow DataSet ");
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);
            set1.setDrawCircles(false);
            set1.setLineWidth(4.8f);
            set1.setCircleRadius(4f);
            set1.setCircleColor(getResources().getColor(R.color.azure));
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setColor(getResources().getColor(R.color.azure));

            LineData data = new LineData(set1);
            data.setValueTextSize(9f);
            data.setDrawValues(false);
            // set data
            chartFlow.setData(data);
            chartFlow.animateX(2000);
            chartFlow.notifyDataSetChanged();
            chartFlow.invalidate();
        }
    }

    private void SetupChartFlow() {
        chartFlow.setOnChartValueSelectedListener(BleDeviceActivity.this);
        // enable description text
        chartFlow.getDescription().setEnabled(false);
        // enable touch gestures
        chartFlow.setTouchEnabled(true);
        // enable scaling and draggingF
        chartFlow.setDragEnabled(true);
        chartFlow.setScaleEnabled(true);
        //chartFlow.setBackgroundColor(Color.TRANSPARENT);
        chartFlow.setDrawGridBackground(true);
        chartFlow.setGridBackgroundColor(Color.TRANSPARENT);
        // if disabled, scaling can be done on x- and y-axis separately
        chartFlow.setPinchZoom(true);
        // set an alternative background color
        chartFlow.setBackgroundColor(getResources().getColor(R.color.dark_slate_blue));
        LineData data = new LineData();
        data.setValueTextColor(getResources().getColor(R.color.cornflower_blue));
        // add empty data
        chartFlow.setData(data);
        // get the legend (only possible after setting data)
        Legend l = chartFlow.getLegend();
        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        //  l.setTypeface(tfLight);
        l.setTextColor(getResources().getColor(R.color.cornflower_blue));
        chartFlow.getLegend().setEnabled(false);
        XAxis xl = chartFlow.getXAxis();
        //   xl.setTypeface(tfLight);
        xl.setTextColor(getResources().getColor(R.color.cornflower_blue));
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setAxisLineColor(getResources().getColor(R.color.cornflower_blue));

        YAxis leftAxis = chartFlow.getAxisLeft();
        //   leftAxis.setTypeface(tfLight);
        // leftAxis.setGridColor(getResources().getColor(R.color.cornflower_blue));
        leftAxis.setDrawGridLines(true);
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(getResources().getColor(R.color.cornflower_blue));
        leftAxis.setAxisLineColor(getResources().getColor(R.color.cornflower_blue));

        //leftAxis.setAxisMaximum(60f);
        //leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = chartFlow.getAxisRight();
        rightAxis.setEnabled(false);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void setData(AirGraphItem gd) {
        double n6 = 0, n7 = 0, n8 = 0, n9 = 0, n10 = 0;
        n6 = n7;
        n7 = n8;
        n8 = n9;
        n9 = n10;
        n10 = gd.getFlow();
        double final_flow1 = 0;
        if (n6 != 0) {
            final_flow1 = (n6 + n7 + n8 + n9 + n10) / 5;
        } else if (n7 != 0) {
            final_flow1 = (n7 + n8 + n9 + n10) / 4;
        } else if (n8 != 0) {
            final_flow1 = (n8 + n9 + n10) / 3;
        } else if (n9 != 0) {
            final_flow1 = (n9 + n10) / 2;
        } else if (n10 != 0) {
            final_flow1 = gd.getFlow();
        }
        if (gd != null) {
            flowValues.add(new Entry((float) gd.getVolume(), (float) final_flow1));
            if (flowValues.size() == 1) {
                setDataChartFlow(flowValues);
            } else {
                addEntry(new Entry((float) gd.getVolume(), (float) final_flow1));
            }
        }
    }

    private void addEntry(Entry entry) {
        LineData data = chartFlow.getData();
        if (data == null) {
            data = new LineData();
            chartFlow.setData(data);
        }
        // choose a random dataSet
        int randomDataSetIndex = (int) (Math.random() * data.getDataSetCount());
        data.addEntry(entry, randomDataSetIndex);
        data.notifyDataChanged();
        // let the chart know it's data has changed
        chartFlow.notifyDataSetChanged();
        chartFlow.setVisibleXRangeMaximum(6);
        chartFlow.moveViewTo(data.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
    }
    private boolean checkHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(BleDeviceActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
            return false;
        }
    }
}
