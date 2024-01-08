package com.example.watchapp.presentation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.watchapp.R;
import com.example.watchapp.presentation.CountdownActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button timer1MinButton = findViewById(R.id.timer1MinButton);
        Button timer2MinButton = findViewById(R.id.timer2MinButton);
        Button timer3MinButton = findViewById(R.id.timer3MinButton);

        timer1MinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountdown(1 * 60 * 1000);
            }
        });

        timer2MinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountdown(2 * 60 * 1000);
            }
        });

        timer3MinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountdown(3 * 60 * 1000);
            }
        });
    }

    private void startCountdown(long duration) {
        Intent intent = new Intent(this, CountdownActivity.class);
        intent.putExtra("duration", duration);
        startActivity(intent);
    }
}
