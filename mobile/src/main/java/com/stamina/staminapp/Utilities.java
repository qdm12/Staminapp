package com.stamina.staminapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Utilities {
    Context context;
    String TAG = "com.stamina.staminapp";
    int i = 0;

    public Utilities(Context ApplicationContext){
        this.context = ApplicationContext;
    }

    public void debug_log(){
        Log.d(TAG, Integer.toString(i));
        i += 1;
    }

    public void log_it(String text){
        if (text==""){
            text = "The string is empty here :(";
        }
        Log.d(TAG, text);
    }

    public void toast_it(String text, int milliduration){
        if (context != null) {
            Toast.makeText(context, text, milliduration).show();
        }
    }

}
