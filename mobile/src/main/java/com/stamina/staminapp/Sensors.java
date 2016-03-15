package com.stamina.staminapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.List;

public class Sensors{
    private Context context;
    private Utilities uti;

    Sensors(Context context){
        this.context = context;
        this.uti = new Utilities(context);
    }

    public class Mylistener implements SensorEventListener{
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            int x = (int)event.values[0];
            uti.log_it("Sensor = " + Integer.toString(x));
        }
    }

    public void test() {
        SensorManager mSensorManager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
        final List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor type : deviceSensors) {
            //uti.log_it(type.getStringType());
        }
        Sensor mysensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mSensorManager.registerListener(new Mylistener(), mysensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
}
