package com.example.doorlockonoff.presentation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.example.doorlockonoff.R;
import java.util.LinkedList;
import java.util.List;
// GO TO LINE 173 TO CHANGE FUNCTIONALITY
public class MainActivity extends Activity {

    private Bluetooth bluetooth;
    private Bluetooth bluetoothEMS;

    private boolean monitoring = false;
    private LinearLayout linLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetooth = new Bluetooth(this);
            bluetoothEMS = new Bluetooth(this);
        }

        linLay  = findViewById(R.id.layout);

        Button rescanEMS = findViewById(R.id.rescanEMS);
        rescanEMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothEMS.isConnected()){
                    bluetoothEMS.setmBluetoothAdapter();

                }else{
                    bluetoothEMS.startAdd("connected with beacon");
                    Log.d("Connected", "Bluetooth EMS connected");
                }
            }
        });

        Button rescanBeac = findViewById(R.id.rescanBeacon);
        rescanBeac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetooth.isConnected()){
                    bluetooth.setmBluetoothAdapter();

                }else{
                    bluetooth.startAdd("connected with beacon");
                    Log.d("Connected", "Beacon connected");

                }

            }
        });

        bluetoothEMS.setBluetoothGattCallback(
                "19b10000-e9f3-537e-4f6c-d104768a1214",
                "19b10004-e9f3-537e-4f6c-d104768a1214"
        );
        bluetooth.setBluetoothGattCallback(
                "4fafc201-1fb5-459e-8fcc-c5c9c331914b",
                "beb5483e-36e1-4688-b7f5-ea07361b26a8"
        );

        // Create an IntentFilter for BluetoothDevice.ACTION_FOUND
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterEMS = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // Register the receiver with the specified Bluetooth address
        registerReceiver(bluetoothEMS.createReciever("E7:96:1E:84:FD:75"), filterEMS);
        registerReceiver(bluetooth.createReciever("34:85:18:07:92:D6"), filter);


        // Call the runnableFunc method on the Bluetooth instance
        bluetooth.runnableFunc();
        bluetoothEMS.runnableFunc();

        // Check for Bluetooth-related permissions and request if not granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1
            );
        }


        Switch onOff = findViewById(R.id.onOffSwitch);
        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ColorStateList colorStateList;
                Log.d(Double.toString(bluetooth.getDistanceFromRSSI()),"d");

                if (isChecked) {
                    colorStateList = ColorStateList.valueOf(Color.parseColor("#700114"));
                    monitoring = true;
                    onOff.setText("Locked");
                }
                else {
                    colorStateList = ColorStateList.valueOf(Color.BLACK);
                    monitoring = false;
                    onOff.setText("Unlocked");


                }
                bluetooth.runnableFunc();
                bluetoothEMS.runnableFunc();
                linLay.setBackgroundTintList(colorStateList);
            }
        });

        startRssiMonitoring();
    }

    Handler queueHandler;
    Runnable queueRunnable;
    private void startRssiMonitoring() {
        queueHandler = new Handler();
        queueRunnable = new Runnable() {
            @SuppressLint("MissingPermission")
            public void run() {
                double rssi = bluetooth.getDistanceFromRSSI();

                if (bluetooth.isConnected()){
                    Log.d("rssi value",rssi+" is the rssi");
                }

//           Change the number (-40) to change the distance. If you want it to do opposite, switch the symbol from < to >.
                if (rssi < -40 && monitoring) {
                    bluetoothEMS.startAdd("stim");
                }

                queueHandler.postDelayed(this, 300);
            }
        };
        queueRunnable.run();

    }




}
