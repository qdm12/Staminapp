package com.stamina.staminapp;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    Networking net = new Networking("");
    Utilities uti = new Utilities(this);
    private long next_timestamp = 1457524540;
    private long expected_activity_timestamp = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Intent intent = getIntent();
        String cookie = intent.getStringExtra("cookie");
        String ip = intent.getStringExtra("ip");
        net.set_cookie(cookie);
        net.set_url(ip);
        setClockNext();
    }

    //Ask get_next_timestamp when the user didnt drink his recommended coffee each 30 minutes
    private void set_next_timestamp(){
        Map<String, String> urlparameters = new HashMap<String, String>();
        urlparameters.put("action", "next_timestamp");
        String response = net.post("select", urlparameters);
        if (response == null){
            uti.toast_it("Server can't be reached now.", 600);
            return;
        } else if(response.equals("false")) {
            uti.toast_it("Upload failed", 600);
            //retry? Bad login ?
            return;
        }
        try {
            this.next_timestamp = Integer.parseInt(response);
        } catch (NumberFormatException e) {
            uti.toast_it("Received bad next timestamp", 600);
        }
    }

    public void setClockNext(){
        set_next_timestamp();
        TextView next_time = (TextView)findViewById(R.id.text_nextclock);
        long present_epoch = System.currentTimeMillis() / 1000; //epoch in seconds
        int difference = (int)(this.next_timestamp - present_epoch);
        if (difference > 300){ //more than 5 minutes
            int minutes_ahead = difference / 60;
            int temp = minutes_ahead % 60;
            int hours_ahead = (minutes_ahead-temp)/60;
            minutes_ahead = temp;

            Calendar c = Calendar.getInstance();
            int hours = c.get(Calendar.HOUR);
            int minutes = c.get(Calendar.MINUTE);

            minutes += minutes_ahead;
            temp = minutes % 60;
            hours += hours_ahead + ((minutes - temp) / 60);
            minutes = temp;
            String half = "AM";
            if (hours > 11) {
                half = "PM";
            }
            if (hours > 12){
                hours -= 12;
            }
            String hours_str = Integer.toString(hours);
            String minutes_str = Integer.toString(minutes);
            if (hours < 10){
                hours_str = "0"+hours_str;
            }
            if (minutes < 10){
                minutes_str = "0"+minutes_str;
            }
            next_time.setText("at "+hours_str+":"+minutes_str+" "+half);
        }else{
            next_time.setText("as soon as possible");
        }
    }

    public void onClicklogout(View view){
        String page = "logout", response;
        Map<String, String> urlparameters = new HashMap<String, String>();
        //based on cookie
        response = net.post(page, urlparameters);
        if (response == null){
            uti.toast_it("Server can't be reached now.", 600);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else if (response.equals("true")){
            //delete vieww
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else{
            //unsuccesful logout
            uti.toast_it("Logout unsuccessful", 1000);
        }
    }

    public void onClickupload(View view) {
        String page = "insert";
        Map<String, String> urlparameters = new HashMap<String,String>();
        Long timestamp_long = System.currentTimeMillis() / 1000; //epoch in seconds
        //urlparameters.put("timestamp",timestamp_long.toString());
        switch (view.getId()) {
            case (R.id.button_coffee_virtual):
                urlparameters.put("action","coffee_virtual");
                break;
            case (R.id.button_coffee):
                urlparameters.put("action","coffee");
                break;
            case (R.id.button_activity):
                urlparameters.put("action","activity");
                break;
            case (R.id.button_sleep):
                urlparameters.put("action","sleep");
                break;
            case (R.id.button_wakeup):
                urlparameters.put("action","wakeup");
                urlparameters.put("quality","68");
                break;
        }
        String response = net.post(page, urlparameters);
        if (response == null){
            uti.toast_it("Server can't be reached now.", 600);
        } else if(response.equals("false")) {
            uti.toast_it("Upload failed", 600);
            //retry? Bad login ?
        } else {
            switch (view.getId()) {
                case (R.id.button_coffee_virtual):
                    //response is yes / no / last - problem: if activity is after then it may be wrong (new cluster)
                    break;
                case (R.id.button_coffee):
                    //response is true / last
                    break;
                case (R.id.button_activity):
                    // response is true (+reevaluate caffeine threshold etc with cluster)
                    break;
                case (R.id.button_sleep):
                    // response is true
                    break;
                case (R.id.button_wakeup):
                    // response is expected timestamp of physical activity or NONE
                    if (response == "NONE") {
                        this.expected_activity_timestamp = -1;
                    }
                    try {
                        this.expected_activity_timestamp = Integer.parseInt(response);
                    } catch (NumberFormatException e) {
                        uti.toast_it("Received bad timestamp", 600);
                        //retry eventually
                    }
                    break;
            }
        }
    }

    public void onClickdownload(View view) {
        String page = "select";
        Map<String, String> urlparameters = new HashMap<String,String>();
        switch (view.getId()) {
            case (R.id.button_receive):
                break;
        }
        String response = net.post(page, urlparameters);
        if(response == null){
            uti.toast_it("Server can't be reached now.", 600);;
        } else {
            uti.log_it("Received data: " + response);
        }
    }

    public void onClickNotification(View view){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.coffee)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        Intent resultIntent = new Intent(this, MainActivity.class);
// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

// Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }



}
