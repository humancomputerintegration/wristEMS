package com.mapbox.navigation.examples.dropinui.basic;

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
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.LinkedList;
import java.util.List;


public class Bluetooth {
    BluetoothAdapter mBluetoothAdapter;

    BluetoothGatt mGatt;
    BluetoothGattService service;

    Activity myContext;
    BluetoothGattCharacteristic intensityCharacteristic;
    BluetoothGattCharacteristic pulseWidthCharacteristic;
    BluetoothGattCharacteristic frequencyCharacteristic;
    BluetoothGattCharacteristic startCharacteristic;
    BluetoothDevice device;

    LinkedList<String> intensityQ = new LinkedList<>();
    LinkedList<String> frequencyQ = new LinkedList<>();
    LinkedList<String> pulseQ = new LinkedList<>();
    LinkedList<Boolean> startQ = new LinkedList<>();
    BluetoothGattCallback bluetoothGattCallback;

    BroadcastReceiver mReceiver;
    Handler queueHandler;
    Runnable queueRunnable;
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    public Bluetooth(Activity context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        myContext = context;


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mBluetoothAdapter.startDiscovery();
        Log.d("Discovering devices ...", "Discovering devices ...");


    }

    public void freqAdd(String data){
        frequencyQ.add(data);
    }

    public void intenAdd(String data){
        intensityQ.add(data);
    }

    public void pulseAdd(String data){
        pulseQ.add(data);
    }

    //Put your bluetooth device address here.
    public BroadcastReceiver createReciever(String address){
        mReceiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (foundDevice.getAddress().equals(address))
                    {
                        mBluetoothAdapter.cancelDiscovery();
                        Log.d("Connecting...","Connecting...");
                        device = foundDevice;
                        Log.d("test","FOUND:" + device.getAddress());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mGatt = device.connectGatt(context.getApplicationContext(), false, bluetoothGattCallback, TRANSPORT_LE);
                        }
                    }
                }
            }
        };
        return mReceiver;
    }

    public void runnableFunc(){
        queueHandler = new Handler();
        queueRunnable = new Runnable() {
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
    }
    @SuppressLint("MissingPermission")
    public void setmBluetoothAdapter(){
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mBluetoothAdapter.startDiscovery();
        Log.d("Discovering devices ...", "Discovering devices ...");
        ;

    }

    public void setBluetoothGattCallback(String devUuid, String uuidChar1, String uuidChar2, String uuidChar3, String uuidChar4) {
        bluetoothGattCallback = new BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    myContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Connected", "connected");
                        }
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    myContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(" not Connected", "not connected");
                        }
                    });
                }
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                String value = characteristic.getStringValue(0);
                Log.d("test", "Characteristic Written: " + value + " at " + System.currentTimeMillis());
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                final List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService s : services) {
                    String uuid = s.getUuid().toString();
                    if (uuid.equals(devUuid)) {
                        Log.i("bluetooth", "Service Found: " + uuid);
                        service = s;
                        for (BluetoothGattCharacteristic mCharacteristic : s.getCharacteristics()) {
                            if (mCharacteristic.getUuid().toString().equals(uuidChar1)) {
                                intensityCharacteristic = mCharacteristic;
                                Log.d("Intensity ", "Intensity characteristic established");
                            }
                            if (mCharacteristic.getUuid().toString().equals(uuidChar2)) {
                                pulseWidthCharacteristic = mCharacteristic;
                                Log.d("Pulse width ", "Pulse width characteristic established");
                            }
                            if (mCharacteristic.getUuid().toString().equals(uuidChar3)) {
                                frequencyCharacteristic = mCharacteristic;
                                Log.d("Frequency ", "Frequency characteristic established");
                            }
                            if (mCharacteristic.getUuid().toString().equals(uuidChar4)) {
                                startCharacteristic = mCharacteristic;
                                Log.d("Start ", "Start characteristic established");
                            }
                        }
                    }
                }
            }
        };

    }
}
