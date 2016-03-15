package com.stamina.staminapp;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NotifReceiver extends BroadcastReceiver {
    public static Networking net;
    public static Utilities uti;
    public static Notifications notif;
    public int activity_coffee_state; //green, orange, red

    public void set_objects(Networking net, Notifications notif, Utilities uti){
        this.net = net;
        this.uti = uti;
        this.notif = notif;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String option = intent.getStringExtra("option");
        int id = intent.getIntExtra("id", -1);
        if (option.equals("coffee")) {
            if (action.equals("com.stamina.staminapp.neutral")) { //do in app yes/no
            } else if (action.equals("com.stamina.staminapp.negative")) { //false detection - do nothing
            } else if (action.equals("com.stamina.staminapp.positive")) { //true detection - upload !
                onDrinkingCoffee();
            }
        } else if(option.equals("physical")){
            if (action.equals("com.stamina.staminapp.neutral")) { //do in app yes/no
            } else if (action.equals("com.stamina.staminapp.negative")) { //false detection - do nothing
            } else if (action.equals("com.stamina.staminapp.positive")) { //true detection - upload !
                onPhysicalActivity();
            }
        }
        notif.finish_notif(id);
    }

    private void onDrinkingCoffee(){
        Map<String, String> urlparameters = new HashMap<String,String>();
        Long timestamp_long = System.currentTimeMillis() / 1000; //epoch in seconds
        urlparameters.put("timestamp",timestamp_long.toString());
        urlparameters.put("action","coffee");
        String response = this.net.post("insert", urlparameters);
        // ANSWER IS IS IT THE LAST COFFEE FOR TODAY ?
        if (response != null){
            if (response.equals("yes")) {
                this.uti.toast_it("Last coffee drunk !", 700);
                //XXX Turn main in red
            } else if (response.equals("no")) {
                this.uti.toast_it("Not the last coffee drunk !", 500);
                //XXX Keep main in green
            } else {
                this.uti.toast_it("Bad response", 800);
                this.uti.log_it("onDrinkingCoffee: Response "+response+" is malformed. It should be yes or no.");
            }
        }
        this.uti.log_it("onDrinkingCoffee: Test, works if server works too...");
        //XXXX else retry later if response
    }

    private void onPhysicalActivity(){
        Map<String, String> urlparameters = new HashMap<String,String>();
        Long timestamp_long = System.currentTimeMillis() / 1000; //epoch in seconds
        urlparameters.put("timestamp",timestamp_long.toString());
        urlparameters.put("action","start_activity");
        String response = net.post("insert", urlparameters);
        // ANSWER IS IS IT THE LAST COFFEE FOR TODAY ?
        if (response != null){
            if (!response.equals("true")) {
                uti.toast_it("Bad response", 800);
                uti.log_it("onPhysicalActivity: Response " + response + " is malformed. It should be true.");
            }
        }
        //XXXX else retry later if response
    }
}