package com.stamina.staminapp;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private Networking net = new Networking(this, "");
    private Localisation GPS;
    private static NotifReceiver notif_receiver;
    private Utilities uti = new Utilities(this);
    private AlarmManager alarm;
    private static Notifications notif;
    private Sensors mysensors;
    private static long expected_activity_time = -1;
    private static String ip, cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.uti.log_it("onCREATE");
        Intent launchWear = new Intent("com.stamina.staminapp.wear");
        sendBroadcast(launchWear);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Intent intent = getIntent();
        String source = intent.getStringExtra("source");
        //GPS.set_default_coffee_areas(); XXXX
        this.alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        this.notif = new Notifications(this);
        this.GPS = new Localisation(this);
        this.mysensors = new Sensors(this);
        if (source.equals("notification")) {
            this.notif.finish_notif(intent.getIntExtra("id", -1));

            //show information such as map
        } else if (source.equals("LoginActivity")){
            this.cookie = intent.getStringExtra("cookie");
            this.ip = intent.getStringExtra("ip");
            this.net.set_cookie(this.cookie);
            this.net.set_url(this.ip);
            this.GPS.set_network(this.net); //check from notification if this works XXX
            this.GPS.configure_permissions(findViewById(R.id.layout_1));
        }
        this.notif_receiver = new NotifReceiver();
        this.notif_receiver.set_objects(this.net, this.notif, this.uti);
        this.mysensors.test();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 26) { //The location id code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                uti.log_it("GPS: Location permissions granted successfully.");
                this.GPS.launch(5000, 2);
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission. XXX
            }
        }
    }

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            notif.makeNotification("Have a cup of coffee if you plan to do sports soon ! ",
                    R.drawable.notif_coffee, R.drawable.notif_physical);
        }
    }


    public void onClicklogout(View view){
        String response = net.post("logout", new HashMap<String, String>());
        if (response == null){ //no network but go back to login screen
            this.uti.log_it("onClicklogout: No network so logging out locally.");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else if (response.equals("true")){ //working log out
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else{
            this.uti.log_it("onClicklogout: Server did not authorise to log out.");
            this.uti.toast_it("Logout unsuccessful", 700);
        }
    }

    public void TestVirtualCoffee(View v){
        Map<String, String> urlparameters = new HashMap<String,String>();
        urlparameters.put("req","coffee_virtual");
        String response = this.net.post("select", urlparameters);
        if (response != null){
            if (response.equals("yes")) {
                this.uti.log_it("You can drink a coffee now.");
            } else if (response.equals("no")){
                this.uti.log_it("You can't drink a coffee now sorry.");
            } else{
                this.uti.log_it("You can drink your last coffee now.");
            }
        }
    }

    public void onDrinkingMovement(View v){ //XXXX remove View and replace by sensors
        this.notif.makeQuestionNotification("coffee");
    }

    public void onPhysicalMovement(View v){ //XXX remove view
        this.notif.makeQuestionNotification("physical");
    }

    public void onDrinkingCoffee(){
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
        this.uti.log_it("onDrinkingCoffee: Test, works if server works too");
        //XXXX else retry later if response
    }

    public void onClickUpload(View view) {
        String page = "insert";
        Map<String, String> urlparameters = new HashMap<String,String>();
        Long timestamp_long = System.currentTimeMillis() / 1000; //epoch in seconds
        urlparameters.put("timestamp",timestamp_long.toString());
        int id = view.getId();
        if (id == R.id.button_sleep){urlparameters.put("action","sleep");}
        else if (id == R.id.button_wakeup){
            urlparameters.put("action","wakeup");
            urlparameters.put("quality","68");
        }
        String response = this.net.post(page, urlparameters);
        if (response != null){
            if (id == R.id.button_wakeup) {
                this.expected_activity_time = -1;
                if (!response.equals("0")) {
                    try {
                        this.expected_activity_time = Integer.parseInt(response);
                        //when approaching this timestamp, ask can_I_drink
                    } catch (NumberFormatException e) {
                        uti.toast_it("Received bad timestamp", 600);
                        //retry eventually
                    }
                }
                Intent alarm_intent = new Intent(this, AlarmReceiver.class);
                //maybe add alarmIntent.setAction("com.stamina....
                PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, alarm_intent, 0);
                if (this.expected_activity_time < 0){
                    //No expected time from cluster so disable timer
                    if (this.alarm != null){
                        this.alarm.cancel(alarmIntent);
                    }
                } else{ //set timer for notification before activity
                    this.alarm.set(AlarmManager.RTC_WAKEUP,  1000*(this.expected_activity_time - 1800), alarmIntent);
                }
            } else{ //R.id.button_sleep - response is true
            }
        }
    }


}
