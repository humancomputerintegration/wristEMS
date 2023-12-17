package com.example.watchapp.presentation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.watchapp.R;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class CountdownActivity extends Activity {
    private TextView timerTextView;
    private Button pauseOrDoneButton;
    private long duration;
    private long remainingTime;
    private boolean isPaused = false;
    private CountDownTimer countDownTimer;

    private BluetoothAdapter mBluetoothAdapter;

    BluetoothGatt mGatt;
    BluetoothGattService service;
    BluetoothGattCharacteristic intensityCharacteristic;
    BluetoothGattCharacteristic pulseWidthCharacteristic;
    BluetoothGattCharacteristic frequencyCharacteristic;
    BluetoothGattCharacteristic startCharacteristic;

    boolean connected = false;

    BluetoothDevice device;

    LinkedList<Integer> intensityQ = new LinkedList<>();

    LinkedList<Integer> frequencyQ = new LinkedList<>();
    LinkedList<Double> pulseQ = new LinkedList<>();
    LinkedList<Boolean> startQ = new LinkedList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }

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
                            Log.d("Connected", "Status");
                            connected = true;

                        }
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("disconnected", "Status");
                            connected = false;
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
                    int intensity = intensityQ.pop();
                    Log.d("test","popped: " + intensity + " at " + System.currentTimeMillis());
                    intensityCharacteristic.setValue(Integer.toString(intensity));
                    mGatt.writeCharacteristic(intensityCharacteristic);
                    queueHandler.postDelayed(this, 200);
                }
                else if (!frequencyQ.isEmpty())
                {
                    int frequency = frequencyQ.pop();
                    Log.d("test","popped: " + frequency + " at " + System.currentTimeMillis());
                    frequencyCharacteristic.setValue(Integer.toString(frequency));
                    mGatt.writeCharacteristic(frequencyCharacteristic);
                    queueHandler.postDelayed(this, 200);
                }
                else if (!pulseQ.isEmpty())
                {
                    double PW = pulseQ.pop();
                    Log.d("test","popped: " + PW + " at " + System.currentTimeMillis());
                    pulseWidthCharacteristic.setValue(Double.toString(PW));
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


        timerTextView = findViewById(R.id.timerTextView);
        pauseOrDoneButton = findViewById(R.id.pauseOrDoneButton);
        duration = getIntent().getLongExtra("duration", 0);

        if (savedInstanceState != null) {
            remainingTime = savedInstanceState.getLong("remainingTime");
        } else {
            remainingTime = duration;
        }

        updateTimerDisplay(remainingTime);


        if (!isPaused) {
            if (connected){
                startTimer(remainingTime);

            }
        }
    }

    public void onPauseOrDoneClick(View view) {
        if (pauseOrDoneButton.getText().equals("Pause")) {
            isPaused = true;
            pauseOrDoneButton.setText("Resume");
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        } else if (pauseOrDoneButton.getText().equals("Resume")) {
            if (connected){
                isPaused = false;
                pauseOrDoneButton.setText("Pause");
                startTimer(remainingTime);
            }else{
                Log.d("Connect Device first.","d");
            }
        } else if (pauseOrDoneButton.getText().equals("Done")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void startTimer(long time) {
        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                pauseOrDoneButton.setText("Done");
            }
        }.start();
    }

    private void updateTimerDisplay(long time) {
        long seconds = time / 1000;
        timerTextView.setText(formatTime(seconds));
        if (seconds == 50){
            Log.d("Send message", "jd");
            intensityQ.add(90);//Change Value for intensity
            frequencyQ.add(80);//Change frequency value
            pulseQ.add(100.0);//Change pulsewidth value
            startQ.add(Boolean.TRUE);//Change val based on necessity
        }
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("remainingTime", remainingTime);
    }
}
