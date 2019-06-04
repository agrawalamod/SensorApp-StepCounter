package com.example.sensorapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SensorService extends Service implements SensorEventListener {


    private SensorManager mSensorManager;
    private Sensor mSensor;
    static final float ALPHA = 0.25f;

    public File dir; // Define dir and make sure it exists
    int count;
    int count2;

    List<String> bufferedData;
    List<String> writeBuffer;

    List<Double> accBuffer;
    List<Double> accyBuffer;
    List<Double> acczBuffer;
    List<Double> analysisBuffer;
    List<Double> gyroBuffer;
    List<Double> gyroRawBuffer;
    List<Long> gyroTimestamps;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    WifiManager wifiManager;
    MediaRecorder recorder;



    private static final float NS2S = 1.0f / 1000000000.0f;


    CurrentState currState = CurrentState.getInstance();

    Boolean isAccelerometer = false;
    Boolean isGravity = false;
    Boolean isPressure = false;
    Boolean isGyro = false;
    Boolean isLinearAcceleration = false;
    Boolean isMagnetometer = false;
    Boolean isRotation = false;
    Boolean isOrientation = false;
    Boolean isLight = false;

    Sensor mAccelerometer;
    Sensor mMagnetometer;
    Sensor mGravity;
    Sensor mPressure;
    Sensor mGyroscope;
    Sensor mLight;
    Sensor mLinearAcceleration;
    Sensor mRotation;
    Sensor mOrientation;
    Sensor mStepDetector;
    Sensor mMagneticRotation;
    Sensor mCompass;

    String timestamp = null;
    String androidID;
    String currentActivity;
    String distance;
    String filename;
    private int interval = 100000;
    private SensorEventListener mSensorEventListener;


    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getFileName(String currentActivity, String distance) {
        //SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yy_hh:mm:ss");

        String currentDateandTime = sdf.format(new Date());
        String filename = currentActivity+ "_" + currentDateandTime + "_"+ distance;

        return filename;
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        filename = null;
        Log.v("SensorService: ", "Sensor Service started!");

        if(intent != null)
        {
            currentActivity = intent.getStringExtra("currentActivity");
            distance = intent.getStringExtra("distance");

        }
        else
        {
            Log.v("SensorService: ", "Sensor Service intent is empty");
        }

        filename = getFileName(currentActivity, distance);
        File root = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        /*
        recorder.setOutputFile(root.getAbsolutePath()+File.separator + filename + ".3gpp");
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
        */

        Log.v("OnStartCommand: ", "writing to " + dir + "/" + filename);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!= null)
        {
            //Accelerometer is present
            isAccelerometer=true;
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        else {
            isAccelerometer=false;
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            isGyro = true;
            mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
        else {
            isGyro = false;
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            isMagnetometer = true;
            mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        }
        else {
            isMagnetometer = false;
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null){
            isRotation = true;
            mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_GAME);
        }
        else {
            Log.v("SensorService", "----------------------- Game Rotation not present! ---------------------");
            isRotation= false;
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) != null){
            mMagneticRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
            mSensorManager.registerListener(this, mMagneticRotation, SensorManager.SENSOR_DELAY_GAME);
        }
        else {
            Log.v("SensorService", "----------------------- Magnetometer not present! --------------------------");
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null){
            mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_GAME);
        }
        else {
            Log.v("SensorService", "----------------------- Compass not present! --------------------------");
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            isLight = true;
            mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            isLight=false;
        }

        mSensorEventListener = this;
        count=0;
        count2=0;
        currState.totalNumOfSteps = 0;

        Log.v("SensorService", "----- Writing New Sensor File ---- ");

        writeToSDFile("timestamp, acc_x, acc_y, acc_z, gyro_x, gyro_y, gyro_z, mag_x, mag_y, mag_z, light, rot_x, rot_y, rot_z, azimuth, pitch, roll, magnetic_azimuth, magnetic_pitch, magnetic_roll, compass_azimuth, sensor_change, wifi_rssi");
        //writeToSDFile("timestamp, acc_x, acc_y, acc_z, sensor_change");

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //Log.v("SensorService", "onSensorChanged");

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            currState.acc_x = event.values[0];
            currState.acc_y = event.values[1];
            currState.acc_z = event.values[2];

            //System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);

            accyBuffer.add(currState.acc_y);
            acczBuffer.add(currState.acc_z);
            accBuffer.add(Math.sqrt(Math.pow(currState.acc_y, 2) + Math.pow(currState.acc_z, 2)));

            count2 = count2+1;
            if(count2%100==0)
            {
                Log.v("DataAnalysis", "count2: " + count2);
            }

        } else if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            currState.gravity_x = event.values[0];
            currState.gravity_y = event.values[1];
            currState.gravity_z = event.values[2];

        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            currState.gyro_x = event.values[0];
            currState.gyro_y = event.values[1];
            currState.gyro_z = event.values[2];

            /*
            if(gyroRawBuffer.size()==10){
                gyroBuffer = new ArrayList<Double>(gyroRawBuffer);
                gyroRawBuffer.clear();
                new CalculateRoation().execute();
            }
            else{
                gyroRawBuffer.add(currState.gyro_z);
                gyroTimestamps.add(event.timestamp);
            }
            */

            /*
            if(currState.startingTime==0){
                currState.startingTime = event.timestamp;
            }
            else{
                Long currentTime = event.timestamp;
                float dTime = (currentTime - currState.startingTime)*NS2S;
                currState.startingTime = currentTime;

                Double dRotation = currState.gyro_z * dTime * 180.0/Math.PI;

                if(dRotation>5.0){
                currState.tRotation = currState.tRotation + Math.abs(dRotation);
                }
                //Log.v("Orientation", "Gyro: " + currState.tRotation);
            }
            */

        } else if (sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
            currState.rot_x = event.values[0];
            currState.rot_y = event.values[1];
            currState.rot_z = event.values[2];

            float[] orientation = new float[3];
            float[] rMat = new float[9];
            float[] angleChange = new float[3];

            // calculate the rotation matrix
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            SensorManager.getOrientation(rMat,orientation);
            // get the azimuth value (orientation[0]) in degree
            currState.orietantion_azimuth = (int) (Math.toDegrees(orientation[0]) + 180) % 360;
            currState.orietantion_pitch = (int) (Math.toDegrees(orientation[1]) + 180) % 360;
            currState.orietantion_roll = (int) (Math.toDegrees(orientation[2]) + 180) % 360;

            if(currState.oldRMat == null){

                currState.oldRMat = new float[9];
                currState.oldRMat = rMat;
                Log.v("GameRotation:", "Vector null");

            }
            else {
                SensorManager.getAngleChange(angleChange,rMat,currState.oldRMat);
                currState.oldRMat = rMat;
                double angleChangeDeg = Math.abs(Math.toDegrees(angleChange[0]));

                //Log.v("Angle Change:", "A: " + Math.toDegrees(angleChange[0]) + "B: " + Math.toDegrees(angleChange[1]) + "C: " + Math.toDegrees(angleChange[2]));

                /*if(angleChangeDeg>=0.2){
                    currState.tRotation = currState.tRotation + angleChangeDeg*2;
                    Log.v("Angle:", "Change: " + angleChangeDeg*2 + " | TotalRot: " + currState.tRotation);
                }
*/
                if(Math.abs(currState.gyro_z)>=0.5)
                {
                    currState.tRotation = currState.tRotation + angleChangeDeg;
                    Log.v("Angle:", "Change: " + angleChangeDeg + " | TotalRot: " + currState.tRotation);

                }
                sendBroadcastMessage(currState.tRotation);

            }


            //Log.v("Angle:", "Change:" +angleChangeDeg);

            //Log.v("Game Rotation:", "Azimuth rn: "+currState.orietantion_azimuth);

            /*
            if(currState.startAzimuth > 360.0){
                currState.startAzimuth = currState.orietantion_azimuth;
                currState.deltaAzimuth = 0.0;
            }
            else{
                double deltaDegree = currState.orietantion_azimuth - currState.startAzimuth;
                if(deltaDegree>180.0){
                    deltaDegree = Math.abs(currState.orietantion_azimuth-0.0) + Math.abs(360.0-currState.startAzimuth);
                }

                if(deltaDegree>5.0) {
                    currState.deltaAzimuth = currState.deltaAzimuth + Math.abs(deltaDegree);
                };

                currState.startAzimuth = currState.orietantion_azimuth;
                Log.v("Orientation", "deltaDegree: " + deltaDegree + "deltaAzimuth: " + currState.deltaAzimuth);

            }
            */
            //Log.v("Orientation", "Current degree:" + currState.orietantion_azimuth);

        } else if (sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {

            float rotationMatrix[] = new float[9];
            mSensorManager.getRotationMatrixFromVector(rotationMatrix,event.values);
            float[] orientationValues = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            currState.magnetic_azimuth = (int) (Math.toDegrees(orientationValues[0]) + 180)%360;
            currState.magnetic_pitch = (int) (Math.toDegrees(orientationValues[1]) + 180)%360;
            currState.magnetic_roll = (int) (Math.toDegrees(orientationValues[2]) + 180)%360;

        } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            currState.compass_azimuth = event.values[0];

            float[] orientationMat = new float[3];
            float[] rotationMat = new float[9];

            // calculate the rotation matrix
            SensorManager.getRotationMatrixFromVector(rotationMat, event.values);
            SensorManager.getOrientation(rotationMat, orientationMat);
            // get the azimuth value (orientation[0]) in degree
            currState.compass_azimuth = (int) (Math.toDegrees(orientationMat[0]) + 180) % 360;

        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            currState.mag_x = event.values[0];
            currState.mag_y = event.values[1];
            currState.mag_z = event.values[2];

        } else if (sensor.getType() == Sensor.TYPE_LIGHT) {
            currState.light = event.values[0]; //Ambient light level in SI lux units
        }

        String data = event.timestamp + ", " +
                Double.toString((currState.acc_x)) + ", " + Double.toString((currState.acc_y)) + ", " + Double.toString((currState.acc_z))
                + ", " + Double.toString((currState.gyro_x)) + ", " + Double.toString((currState.gyro_y)) + ", " + Double.toString((currState.gyro_z))
                + ", " + Double.toString((currState.mag_x)) + ", " + Double.toString((currState.mag_y)) + ", " + Double.toString((currState.mag_z))
                + ", " + Double.toString((currState.light))
                + ", " + Double.toString((currState.rot_x)) + ", " + Double.toString((currState.rot_y)) + ", " + Double.toString((currState.rot_z))
                + ", " + Double.toString((currState.orietantion_azimuth)) + ", " + Double.toString((currState.orietantion_pitch)) + ", " + Double.toString((currState.orietantion_roll))
                + ", " + Double.toString((currState.magnetic_azimuth)) + ", " + Double.toString((currState.magnetic_pitch)) + ", " + Double.toString((currState.magnetic_roll))
                + ", " + Double.toString((currState.compass_azimuth))
                + ", " + event.sensor.getName()
                + ", " + Integer.toString(wifiManager.getConnectionInfo().getRssi());



        // Uncomment the lines to write to File
        /*
        if (count == 5000) {
            Log.v("SensorService", "5000 lines reached");
            writeBuffer = new ArrayList<String>(bufferedData);
            bufferedData.clear();
            count = 0;
            new WriteFile().execute();
        } else {
            bufferedData.add((data));
        }
        count++;
        */


        if (count2 == 500) {
            Log.v("DataAnalysis", "500 data collected");
            analysisBuffer.addAll(accBuffer);
            accBuffer.clear();
            count2=0;
            new AnalyzeData().execute();
        }


    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        double azimuth_angle = orientationAngles[0];
        double azimuth_angle_deg = azimuth_angle *  180.0/Math.PI;
        Log.v("Android", "Orientation:" + azimuth_angle_deg);

        // "mOrientationAngles" now has up-to-date information.
    }

    private class WriteFile extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);
            Log.v("SensorService", "Data logging complete");
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Log.v("SensorService", "Size of write buffer: " + writeBuffer.size());
            Log.v("SensorService", "Data logging started");
            writeToSDFile();
            writeBuffer.clear();

            return null;

        }
    }

    private class AnalyzeData extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);
            Log.v("DataAnalysis:", "Step count updated!");
            Log.v("DataAnalysis:", "Steps in this cycle: "+ currState.currentSteps);
            Log.v("DataAnalysis:", "Total number of steps yet: " + currState.totalNumOfSteps);

            //Toast.makeText(getApplicationContext(), "Steps computation complete!", Toast.LENGTH_LONG).show();
            sendBroadcastMessage(currState.currentSteps, currState.totalNumOfSteps);

        }

        @Override
        protected Void doInBackground(Void... params) {
            //Log.v("SensorService", "Size of write buffer: " + writeBuffer.size());
            Log.v("DataAnalysis", "Computing Steps");
            analysisBuffer = smooth(analysisBuffer,40);
            //currState.currentSteps = countSteps(10.5, 40);
            currState.currentSteps = countSteps(10.03, 40);
            currState.totalNumOfSteps += currState.currentSteps;

            return null;

        }
    }

    private class CalculateRoation extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);
            Log.v("Orientation", "Gyro: " + currState.tRotation);
            sendBroadcastMessage(currState.tRotation);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Log.v("SensorService", "Size of write buffer: " + writeBuffer.size());
            Log.v("Orientation", "Computing Rotation");

            Double cRoation = 0.0;
            int i=1;
            while(i<gyroBuffer.size()){
                float dTime = (gyroTimestamps.get(i) - gyroTimestamps.get(i-1))*NS2S;
                Double dRotation = gyroBuffer.get(i) * dTime * 180.0/Math.PI;
                cRoation = cRoation+ Math.abs(dRotation);
                i =i+1;
            }

            Log.v("Orientation", "cRotation: "+cRoation);

            if(cRoation>=4.0){
                currState.tRotation += cRoation;
            }
            return null;

        }
    }

    private void writeToSDFile(String d){

        File file = new File(dir + File.separator + filename +".csv");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        OutputStreamWriter osw = new OutputStreamWriter(fOut);
        try {
            osw.write(d + "\n");
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeToSDFile(){

        Log.v("SensorService", "Size of write buffer: " + writeBuffer.size());


        File file = new File(dir + File.separator + filename +".csv");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        OutputStreamWriter osw = new OutputStreamWriter(fOut);
        try {
            for(int i=0;i<writeBuffer.size(); i++) {
                osw.write(writeBuffer.get(i) + "\n");
            }
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onCreate()
    {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        File root = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        Log.v("Downloads Path", root.getAbsolutePath());
        dir = new File (root.getAbsolutePath() + File.separator);

        bufferedData = new ArrayList<String>();

        accBuffer = new ArrayList<Double>();
        accyBuffer = new ArrayList<Double>();
        acczBuffer = new ArrayList<Double>();
        analysisBuffer = new ArrayList<Double>();
        gyroBuffer = new ArrayList<Double>();
        gyroTimestamps = new ArrayList<Long>();
        gyroRawBuffer = new ArrayList<Double>();

        currState.startAzimuth = 500.00;
        currState.deltaAzimuth = 0.00;
        currState.tRotation = 0.0;
        currState.startingTime = 0;

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //recorder = new MediaRecorder();
        //recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        currState.currentSteps = 0;
        currState.totalNumOfSteps = 0;
        currState.orientationChange=0.0;
        currState.tRotation = 0.0;
        currState.oldRMat = null;

    }
    public void onDestroy() {

        super.onDestroy();
        if(bufferedData.size()>0){
            writeBuffer = new ArrayList<String>(bufferedData);
            new WriteFile().execute();
        }
        //recorder.stop();
        //recorder.release();

        currState.currentSteps = 0;
        currState.totalNumOfSteps = 0;
        currState.orientationChange=0.0;
        currState.tRotation = 0.0;
        currState.oldRMat = null;

        Log.v("SensorService", "Unregistering sensors");

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!= null) {
            mSensorManager.unregisterListener(mSensorEventListener, mAccelerometer);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
            //mSensorManager.unregisterListener(mSensorEventListener, mGravity);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
            //mSensorManager.unregisterListener(mSensorEventListener, mPressure);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            mSensorManager.unregisterListener(mSensorEventListener, mGyroscope);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            //mSensorManager.unregisterListener(mSensorEventListener, mLinearAcceleration);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            mSensorManager.unregisterListener(mSensorEventListener, mMagnetometer);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null){
            mSensorManager.unregisterListener(mSensorEventListener, mRotation);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){
            mSensorManager.unregisterListener(mSensorEventListener, mOrientation);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            mSensorManager.unregisterListener(mSensorEventListener, mLight);
        }
    }

    public int countSteps(Double peakThreshold, int minDist){
        int numOfSteps = 0;

        Log.v("DataAnalysis", "Count Steps");

        int midpnt = minDist/2;
        //int i = 250;
        int i = midpnt;
        int latestPeak = i;
        while(i<analysisBuffer.size()-midpnt){
            List prev = analysisBuffer.subList(i-midpnt, i);
            List next = analysisBuffer.subList(i+1,i+midpnt);
            Double mean_prev = mean(prev);
            Double mean_next = mean(next);

            if(mean_prev < analysisBuffer.get(i) && mean_next < analysisBuffer.get(i) && analysisBuffer.get(i) > peakThreshold)
            {
                //Peak found
                latestPeak = i;
                numOfSteps = numOfSteps + 1;
                i=i+midpnt;
            }
            else{
                i=i+1;
            }
        }
        Log.v("DataAnalysis", "latestPeak: " + latestPeak);
        Log.v("DataAnalysis", "analysisBufferSize: " + analysisBuffer.size());
        analysisBuffer = analysisBuffer.subList(latestPeak,analysisBuffer.size());

        return numOfSteps;
    }

    public Double mean(List<Double> data){
        Double sum =0.0;
        for (int i=0; i<data.size();i++)
        {
            sum+=data.get(i);
        }
        return sum/data.size();
    }

    public List<Double> smooth(List<Double> x, int N){
        int n = N/2;
        List<Double> y = new ArrayList<Double>();
        for(int i=0; i<x.size();i++){
            if(i-n>=0 && x.size()-1-i>=n){
                y.add((1.0/((2*n)+1))*(listSum(x,i-n, i) + x.get(i) + listSum(x, i+1, i+n+1)));
            }
            else if(i-n<0) {
                y.add((1.0/((2*n)+1))*(listSum(x,0, i) + x.get(i) + listSum(x, i+1, i+1+i)));
            }
            else if(x.size()-1-i<n) {
                int t = x.size()-1-i;
                y.add((1.0/((2*n)+1))*(listSum(x,i-t, i) + x.get(i) + listSum(x, i+1, i+1+t)));

            }
        }
        Log.v("DataAnalysis", "Smoothening complete");
        Log.v("DataAnalysis", "Max of smooth array: " + Collections.max(y));
        return y;
    }
    public Double listSum(List<Double> list, int start, int end){
        Double sum = 0.00;
        for(int i = start; i<end; i++){
            sum+=list.get(i);
        }
        return sum;
    }

    private void sendBroadcastMessage(int currentSteps, int totalSteps) {

        Intent intent = new Intent("stepsUpdate");
        intent.putExtra("totalSteps", totalSteps);
        intent.putExtra("currentSteps", currentSteps);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }
    private void sendBroadcastMessage(double orientationChange) {

        Intent intent = new Intent("orientationUpdate");
        intent.putExtra("orientationChange", orientationChange);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }
}
