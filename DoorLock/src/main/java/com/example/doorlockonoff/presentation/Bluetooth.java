package com.example.doorlockonoff.presentation;

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

    boolean connected = false;

    double RSSI = 0;

    Activity myContext;
    BluetoothGattCharacteristic intensityCharacteristic;
    BluetoothGattCharacteristic pulseWidthCharacteristic;
    BluetoothGattCharacteristic frequencyCharacteristic;
    BluetoothGattCharacteristic startCharacteristic;
    BluetoothDevice device;

    LinkedList<String> startQ = new LinkedList<>();
    BluetoothGattCallback bluetoothGattCallback;

    BroadcastReceiver mReceiver;
    Handler queueHandler;
    Runnable queueRunnable;
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    public Bluetooth(Activity context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        myContext = context;


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mBluetoothAdapter.startDiscovery();
        Log.d("Discovering devices ...", "Discovering devices ...");


    }

    @SuppressLint("MissingPermission")
    public void startAdd(String data){
        startQ.add(data);

        runnableFunc();
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
                    Log.d("adsress",foundDevice.getAddress()+":"+foundDevice.getName());
                    if (foundDevice.getAddress().equals(address))
                    {
                        Log.d("here","here");
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

    public double getDistanceFromRSSI() {
        return  RSSI;
    }
    @SuppressLint("MissingPermission")
    public void startRssiRead() {
        if (mGatt != null) {
            mGatt.readRemoteRssi();
        }
    }

    public void runnableFunc(){
        queueHandler = new Handler();
        queueRunnable = new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                if (mGatt == null ||  startCharacteristic == null ){
                    queueHandler.postDelayed(this, 400);
                }
                else if (!startQ.isEmpty())
                {
                    String start = startQ.pop();
                    Log.d("test","popped: " + start + " at " + System.currentTimeMillis());
                    startCharacteristic.setValue(start);
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

    public boolean isConnected() {
        return connected;
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

    public void setBluetoothGattCallback(String devUuid, String uuidChar1) {
        bluetoothGattCallback = new BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    mGatt.readRemoteRssi();
                    connected = true;
                    startQ.clear();
                    myContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Connected", "connected");
                        }
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    connected = false;
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
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    Log.d("RSSI", "RSSI Value: " + rssi);
                    // Handle the RSSI value as needed
                } else {
                    Log.e("RSSI", "Error reading RSSI: " + status);
                }

                // Schedule the next RSSI read after a delay (adjust as needed)
                queueHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startRssiRead();
                        RSSI = rssi;
                    }
                }, 100); // 1000 milliseconds delay for the next RSSI read
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
