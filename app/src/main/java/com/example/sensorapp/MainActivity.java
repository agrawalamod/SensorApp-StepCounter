package com.example.sensorapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button start = (Button) findViewById(R.id.start);
        Button stop = (Button) findViewById(R.id.stop);
        final EditText currentActivity = (EditText) findViewById(R.id.editText);
        final EditText distance = (EditText) findViewById(R.id.editText2);
        final EditText stepLength = (EditText) findViewById(R.id.editText3);

        final TextView totalNumOfSteps = (TextView) findViewById(R.id.totalSteps);
        final TextView currentSteps = (TextView) findViewById(R.id.currentSteps);
        final TextView orientationChange = (TextView) findViewById(R.id.orientationChange);
        final TextView distanceTravelled = (TextView) findViewById(R.id.distanceTraveled);

        Log.v("MainActivity", stepLength.getText().toString());

        final Intent startSensor = new Intent(MainActivity.this, SensorService.class);
        final Intent stopSensor = new Intent(MainActivity.this, SensorService.class);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int totalNumSteps = intent.getIntExtra("totalSteps",0);
                        int stepsCurrentCycle = intent.getIntExtra("currentSteps",0);
                        totalNumOfSteps.setText("Total number of steps: " + totalNumSteps);
                        currentSteps.setText("Steps in this cycle: " + stepsCurrentCycle);
                        distanceTravelled.setText("Distance travelled: " + totalNumSteps * Double.valueOf(stepLength.getText().toString()) + " ft");
                    }
                }, new IntentFilter("stepsUpdate")
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        double orientationChangeData = intent.getDoubleExtra("orientationChange", 0.0);
                        orientationChange.setText("Total change in orientation: " + orientationChangeData);
                    }
                }, new IntentFilter("orientationUpdate")
        );

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startSensor.putExtra("currentActivity", currentActivity.getText().toString());
                startSensor.putExtra("distance", distance.getText().toString());
                if(stepLength.getText().toString().equals("")){
                    stepLength.setText("2.5");
                }
                startService(startSensor);
                //Toast.makeText(getApplicationContext(), "Data collection started!", Toast.LENGTH_LONG).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                stopService(stopSensor);
                //Toast.makeText(getApplicationContext(), "Data collection stopped!", Toast.LENGTH_LONG).show();

                Log.v("MainActivity","Service stopped");
            }
        });

    }

}
