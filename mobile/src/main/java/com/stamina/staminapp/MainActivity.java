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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    private static String url, cookie;
    private GoogleMap googleMap;
    private TextView userText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.uti.log_it("onCREATE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Intent intent = getIntent();
        this.alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        this.notif = new Notifications(this);
        this.googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); //XXXX change that
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.getUiSettings().setZoomGesturesEnabled(true);
        this.googleMap.setTrafficEnabled(false);
        this.GPS = new Localisation(this, this.googleMap);
        this.mysensors = new Sensors(this);
        String username = "None";
        if (intent.getAction().equals("notification")) {
            String data_raw = this.readFromFile("network_session");
            String[] temp = data_raw.split("0000000");
            this.url = temp[0]; this.cookie = temp[1]; username = temp[2];
            this.uti.log_it("URL and cookie recovered: "+this.url+" | "+this.cookie);
            this.notif.finish_notif(intent.getIntExtra("id", -1));
            //show information such as map
        } else if (intent.getAction().equals("login")){
            this.cookie = intent.getStringExtra("cookie");
            this.url = intent.getStringExtra("url");
            username = intent.getStringExtra("username");
            new File(this.getFilesDir(), "network_session");
            this.writeToFile("network_session", this.url + "0000000" + this.cookie + "0000000" + username);
            this.GPS.set_network(this.net); //check from notification if this works XXX
            this.GPS.configure_permissions(findViewById(R.id.layout_1));
        }
        this.net.set_url(this.url);
        this.net.set_cookie(this.cookie);
        this.notif_receiver = new NotifReceiver();
        this.notif_receiver.set_objects(this.net, this.notif, this.uti, this.imageView);
        this.userText = (TextView) findViewById(R.id.username);
        this.userText.setText(username);
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.499131, -0.176300), 12));
        add_coffee_point(51.499131, -0.176300, "Imperial College EEE cafe");
        add_coffee_point(51.493782, -0.174287, "Starbucks (South Kensington)");
        add_coffee_point(51.485997, -0.172544, "Home");
        this.imageView = (ImageView) findViewById(R.id.candrink);
    }

    private void add_coffee_point(Double x, Double y, String name){
        GPS.add_coffee_area(x, y, name, false);
        final LatLng newLoc = new LatLng(x, y);
        Marker newMarker = this.googleMap.addMarker(new MarkerOptions().position(newLoc).title(name));
    }

    //@Override
    //public void setup_gmap(final ){

    //}

    private String readFromFile(String filename) {
        String ret = "";
        try {
            InputStream inputStream = openFileInput(filename);
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            uti.log_it("File not found: " + e.toString());
        } catch (IOException e) {
            uti.log_it("Can not read file: " + e.toString());
        }
        return ret;
    }

    private void writeToFile(String filename, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (Exception e) {
            uti.log_it("Exception, File write failed: " + e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 26) { //The location id code
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                uti.log_it("GPS: Location permissions granted successfully.");
                this.GPS.launch(3000, 3);
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission. XXX
            }
        }
    }

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            imageView.setImageResource(R.drawable.coffee_yes);
            notif.makeNotification("Have a cup of coffee if you plan to do sports soon ! ",
                    R.drawable.notif_coffee, R.drawable.notif_physical);
        }
    }


    public void onClicklogout(View view){
        this.deleteFile("network_session");
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
                this.imageView.setImageResource(R.drawable.coffee_yes);
                this.uti.log_it("You can drink a coffee now.");
                notif.makeNotification("You could grab a cup of coffee now",
                        R.drawable.notif_coffee, R.drawable.notif_staminapp);
            } else if (response.equals("no")){
                this.imageView.setImageResource(R.drawable.coffee_no);
                this.uti.log_it("You can't drink a coffee now sorry.");
                notif.makeNotification("Don't drink a coffee, your sleep is in danger...",
                        R.drawable.notif_coffee, R.drawable.notif_staminapp);
            } else{
                this.uti.log_it("You can drink your last coffee now.");
                this.imageView.setImageResource(R.drawable.coffee_last);
                notif.makeNotification("Enjoy your last cup of coffee today !",
                        R.drawable.notif_coffee, R.drawable.notif_staminapp);
            }
        }
    }

    public void onDrinkingMovement(View v){ //XXXX remove View and replace by sensors
        this.notif.makeQuestionNotification("coffee");
    }

    public void onPhysicalMovement(View v){ //XXX remove view
        this.notif.makeQuestionNotification("physical");
    }

    public void onClickUpload(View view) {
        String page = "insert";
        Map<String, String> urlparameters = new HashMap<String,String>();
        Long timestamp_long = System.currentTimeMillis() / 1000; //epoch in seconds
        urlparameters.put("timestamp",timestamp_long.toString());
        int id = view.getId();
        if (id == R.id.button_sleep){urlparameters.put("action","sleep");}
        else if (id == R.id.button_wakeup){
            this.GPS.sleeping = false; //updates Localisation.sleeping boolean
            this.imageView.setImageResource(R.drawable.coffee_yes);
            urlparameters.put("action","wake_up");
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
