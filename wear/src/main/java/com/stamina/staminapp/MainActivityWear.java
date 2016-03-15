package com.stamina.staminapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class MainActivityWear extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("com.stamina.staminapp","Wear here");
        //Intent i = new Intent();
        //i.setAction("com.stamina.staminapp.SHOW_NOTIFICATION");
        //sendBroadcast(i);
        finish();
    }
}
