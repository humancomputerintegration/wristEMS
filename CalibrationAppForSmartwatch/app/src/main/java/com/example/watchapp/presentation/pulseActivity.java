package com.example.watchapp.presentation;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.watchapp.R;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class pulseActivity extends Activity {

    private int percent = 0;

    private CircularSeekBar circle;
    private int node = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        TextView percentageDisplay = findViewById(R.id.textView);

        circle = findViewById(R.id.circularSeekBar);
        circle.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                // Log the progress value whenever it changes
                Log.d("CircularSeekBar", "Progress: " + progress);
                percentageDisplay.setText((int)circle.getProgress()+"%");
                percent=(int)circle.getProgress();
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                // Handle when the user stops dragging the thumb (optional)
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                // Handle when the user starts dragging the thumb (optional)
            }
        });



        NumberPicker picker = findViewById(R.id.number_picker);
        String[] data = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12"};
        picker.setMinValue(0);
        picker.setMaxValue(data.length-1);
        picker.setDisplayedValues(data);
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                node = newVal;
                Log.d(Integer.toString(node),"a");
            }
        });


        Button send = findViewById(R.id.button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dataToSend = "p "+node+" "+percent;

                Intent intent = new Intent(pulseActivity.this, MainActivity.class);
                intent.putExtra("key", dataToSend);
                setResult(RESULT_OK, intent);
                finish();

            }
        });

    }

}