package com.stamina.staminapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class MainActivityWear extends Activity {
    private static int i =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("com.stamina.staminapp", "onCreate");
        SensorManager mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        /*final List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor type : deviceSensors) {
            Log.d(TAG, type.getStringType());
        } */
        Sensor mysensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        while(true) {
            mSensorManager.registerListener(new Mylistener(), mysensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("com.stamina.staminapp", "Listener registered..");
            try {
                Thread.sleep(12000);
            } catch (Exception e) {
            }
        }
    }


    private class Mylistener implements SensorEventListener {
        DecimalFormat df = new DecimalFormat("#.##");

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            double xD = (double) event.values[0];
            double yD = (double) event.values[1];
            double zD = (double) event.values[2];
            int tI = (int) (event.timestamp / 100000000);
            String x = df.format(xD);
            String y = df.format(yD);
            String z = df.format(zD);
            String t = Integer.toString(tI);
            Log.d("com.stamina.staminapp", "@ " + t + ": x=" + x + " y=" + y + " z=" + z);
        }
    }

}
