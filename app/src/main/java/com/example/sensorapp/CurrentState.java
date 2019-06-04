package com.example.sensorapp;

public class CurrentState {
    private static CurrentState currState = new CurrentState();

    public static CurrentState getInstance()
    {
        return currState;
    }
    //Accelerometer
    public double acc_x,acc_y,acc_z;

    //Magnetometer
    public double mag_x, mag_y, mag_z;

    //Gravity
    public double gravity_x, gravity_y, gravity_z;

    //Barometer
    public double pressure;

    //Gyrometer
    public double gyro_x, gyro_y, gyro_z;

    //Light
    public double light;

    //Linear Acc
    public double la_x, la_y, la_z;

    //Rotation
    public double rot_x, rot_y, rot_z;

    //Orientation
    public double orietantion_azimuth;
    public double orietantion_pitch;
    public double orietantion_roll;

    public double magnetic_azimuth;
    public double magnetic_pitch;
    public double magnetic_roll;

    public double compass_azimuth;

    //NumberOfSteps
    public int totalNumOfSteps;
    public int currentSteps;
    public double startAzimuth;
    public double deltaAzimuth;
    public long startingTime;
    public Double tRotation;
    public Double orientationChange;

    public float[] oldRMat = null;


}
