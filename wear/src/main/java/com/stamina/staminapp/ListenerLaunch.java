package com.stamina.staminapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ListenerLaunch extends BroadcastReceiver {
    private static Context context;

    public void set_context(Context context){
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Intent newIntent = new Intent(this.context, MainActivityWear.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(newIntent);
        Log.d("WEAR","here");
    }
}