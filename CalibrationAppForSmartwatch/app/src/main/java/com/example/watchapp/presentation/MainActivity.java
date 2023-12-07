package com.example.watchapp.presentation;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.watchapp.R;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity {
    String receivedData = "";
    private static final int INTENSITY_ACTIVITY_REQUEST_CODE = 123;
    private BluetoothAdapter mBluetoothAdapter;

    BluetoothGatt mGatt;
    BluetoothGattService service;
    BluetoothGattCharacteristic intensityCharacteristic;
    BluetoothGattCharacteristic pulseWidthCharacteristic;
    BluetoothGattCharacteristic frequencyCharacteristic;
    BluetoothGattCharacteristic startCharacteristic;
    BluetoothDevice device;

    LinkedList<String> intensityQ = new LinkedList<>();
    TextView debugTextView;

    LinkedList<String> frequencyQ = new LinkedList<>();
    LinkedList<String> pulseQ = new LinkedList<>();
    LinkedList<Boolean> startQ = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch startStopSwitch = findViewById(R.id.startStopSwitch);
        startStopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) startQ.add(Boolean.TRUE);
                else startQ.add(Boolean.FALSE);
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
        debugTextView = findViewById(R.id.debug);

//
        Button rescan = findViewById(R.id.rescan);
        rescan.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.cancelDiscovery();
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }
                mBluetoothAdapter.startDiscovery();
                debugTextView.setText("Discovering Devices...");
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            debugTextView.setText("Connected");
                            rescan.setEnabled(false);
                        }
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            debugTextView.setText("Not Connected");
                            rescan.setEnabled(true);
                        }
                    });
                }
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                String value = characteristic.getStringValue(0);
                Log.d("test","Characteristic Written: " + value +" at " + System.currentTimeMillis());
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                final List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService s : services)
                {
                    String uuid = s.getUuid().toString();
                    if (uuid.equals("19b10000-e9f3-537e-4f6c-d104768a1214"))
                    {
                        Log.i("bluetooth", "Service Found: " + uuid);
                        service = s;
                        for (BluetoothGattCharacteristic mCharacteristic : s.getCharacteristics()) {
                            if (mCharacteristic.getUuid().toString().equals("19b10001-e9f3-537e-4f6c-d104768a1214"))
                            {
                                intensityCharacteristic = mCharacteristic;
                                Log.d("Intensity characteristic established","Intensity characteristic established");
                            }
                            if (mCharacteristic.getUuid().toString().equals("19b10002-e9f3-537e-4f6c-d104768a1214"))
                            {
                                pulseWidthCharacteristic = mCharacteristic;
                                Log.d("Pulse width characteristic established","Pulse width characteristic established");
                            }
                            if (mCharacteristic.getUuid().toString().equals("19b10003-e9f3-537e-4f6c-d104768a1214"))
                            {
                                frequencyCharacteristic = mCharacteristic;
                                Log.d("Frequency characteristic established","Frequency characteristic established");
                            }
                            if (mCharacteristic.getUuid().toString().equals("19b10004-e9f3-537e-4f6c-d104768a1214"))
                            {
                                startCharacteristic = mCharacteristic;
                                Log.d("Start characteristic established","Start characteristic established");
                            }
                        }
                    }
                }
            }
        };

        mBluetoothAdapter.startDiscovery();
        debugTextView.setText("Discovering Devices...");

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (foundDevice.getAddress().equals("E7:96:1E:84:FD:75"))
                    {
                        mBluetoothAdapter.cancelDiscovery();
                        debugTextView.setText("Connecting...");
                        device = foundDevice;
                        Log.d("test","FOUND:" + device.getAddress());
                        mGatt = device.connectGatt(getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        Handler queueHandler = new Handler();
        Runnable queueRunnable = new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                if (mGatt == null || intensityCharacteristic == null || pulseWidthCharacteristic == null ||  startCharacteristic == null||  frequencyCharacteristic == null ){
                    queueHandler.postDelayed(this, 400);
                }
                else if (!intensityQ.isEmpty())
                {
                    Log.d("beforeIntensitypopped","");
                    String intensity = intensityQ.pop();
                    Log.d("test","popped: " + intensity + " at " + System.currentTimeMillis());
                    intensityCharacteristic.setValue(intensity);
                    mGatt.writeCharacteristic(intensityCharacteristic);
                    queueHandler.postDelayed(this, 200);
                }
                else if (!frequencyQ.isEmpty())
                {
                    String frequency = frequencyQ.pop();
                    Log.d("test","popped: " + frequency + " at " + System.currentTimeMillis());
                    frequencyCharacteristic.setValue(frequency);
                    mGatt.writeCharacteristic(frequencyCharacteristic);
                    queueHandler.postDelayed(this, 200);
                    queueHandler.postDelayed(this, 200);
                }
                else if (!pulseQ.isEmpty())
                {
                    String PW = pulseQ.pop();
                    Log.d("test","popped: " + PW + " at " + System.currentTimeMillis());
                    pulseWidthCharacteristic.setValue(PW);
                    mGatt.writeCharacteristic(pulseWidthCharacteristic);
                    queueHandler.postDelayed(this, 200);
                }
                else if (!startQ.isEmpty())
                {
                    boolean start = startQ.pop();
                    Log.d("test","popped: " + start + " at " + System.currentTimeMillis());
                    startCharacteristic.setValue(Boolean.toString(start));
                    mGatt.writeCharacteristic(startCharacteristic);
                    queueHandler.postDelayed(this, 200);
                }
                else {
                    queueHandler.postDelayed(this, 300);
                }
            }
        };
        queueRunnable.run();



        Button freq = findViewById(R.id.freqBut);
        Button inten = findViewById(R.id.intensBut);
        Button puls = findViewById(R.id.pulseBut);

        inten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, intensityActivity.class);
                startActivityForResult(intent, INTENSITY_ACTIVITY_REQUEST_CODE);

            }
        });

        freq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, freqActivity.class);
                startActivityForResult(intent, INTENSITY_ACTIVITY_REQUEST_CODE);

            }
        });

        puls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, pulseActivity.class);
                startActivityForResult(intent, INTENSITY_ACTIVITY_REQUEST_CODE);

            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENSITY_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("key")) {
                receivedData = data.getStringExtra("key");
                Log.d(receivedData,receivedData);
                sendData(receivedData);
            }
        }
    }

    private void sendData(String data){
        int positionOfSpace = data.indexOf(" ");
        String type = data.substring(0,positionOfSpace);
        String dataWithoutClassiy = data.substring(positionOfSpace+1);
        if (type.equals("f")){
            frequencyQ.add(dataWithoutClassiy);
        }
        if (type.equals("p")){
            pulseQ.add(dataWithoutClassiy);
        }
        if (type.equals("i")){
            intensityQ.add(dataWithoutClassiy);
        }
    }




}