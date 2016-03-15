package com.stamina.staminapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Localisation {
    private LocationManager locManager;
    private Vector<Area> coffee_areas = new Vector<>();
    static public LocationListener locListener;
    static private Localisation.Area current_area;
    private Context context;
    private Utilities uti;
    private Networking net;
    private Notifications notif;



    Localisation(Context context){
        this.context = context;
        uti = new Utilities(context);
        notif = new Notifications(this.context);
        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location){
                boolean check_virtual_coffee = false;
                double x = location.getLatitude(), y = location.getLongitude();
                uti.log_it("GPS: Coordinates X = " + Double.toString(x)+", Y = "+Double.toString(y));
                for (Area area : coffee_areas){
                    if (area.isInArea(x, y)) {
                        long t_now = System.currentTimeMillis() / 1000;
                        if ((area.equals(current_area)) && (t_now > area.getTimein() + 10800)) {
                            //if 3 hours have elapsed since you entered the area and you are still here.
                            check_virtual_coffee = true;
                        } else if (!area.equals(current_area)) {
                            current_area = area;
                            current_area.setTimein((int) t_now);
                            check_virtual_coffee = true;
                        }
                    }
                    if (check_virtual_coffee){
                        String page = "select";
                        Map<String, String> urlparameters = new HashMap<String,String>();
                        urlparameters.put("req", "coffee_virtual");
                        String response = net.post(page, urlparameters);
                        if (response != null){
                            String result = "Uninitialized";
                            if (response.equals("yes")) {
                                result = "Have a cup of coffee!";
                                if (area.isPersonal()) {
                                    result += "It seems you're in one of your coffee areas!";
                                } else {
                                    result += "The nearest coffee shop is "+area.getName();
                                    //Find closest shop on google maps XXX
                                }
                            } else if (response.equals("no")){
                                result = "Don't drink any coffee, your sleep is in danger!";
                            } else if (response.equals("last")){
                                result = "Enjoy your last cup of coffee today. ";
                                if (area.isPersonal()) {
                                    result += "It seems you're in one of your coffee areas!";
                                } else {
                                    result += "The nearest coffee shop is "+area.getName();
                                    //Find closest shop on google maps XXX
                                }
                            }
                            notif.makeNotification(result,
                                    R.drawable.notif_coffee, R.drawable.notif_staminapp);
                        }
                        break;
                    }
                }
            }
            @Override
            public void onProviderDisabled(String provider){} //ask to reactivate GPS or disable functionalities XXX
            @Override
            public void onProviderEnabled(String provider){}
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras){}
        };
    }

    public void set_network(Networking net_ext){this.net = net_ext;}

    public void configure_permissions(View view_layout){
        final Activity activity_layout = (Activity) view_layout.getContext();
        boolean request_permissions = false;
        final String[] PermissionsLocation = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION};
        if ( Build.VERSION.SDK_INT < 23){ //before Android 5.0
            if ((ContextCompat.checkSelfPermission(activity_layout, PermissionsLocation[0]) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(activity_layout, PermissionsLocation[1]) != PackageManager.PERMISSION_GRANTED)){
                request_permissions = true;
            }
        } else { //Android 5.0 and more
            if ((this.context.checkSelfPermission(PermissionsLocation[0]) != PackageManager.PERMISSION_GRANTED) ||
                    (this.context.checkSelfPermission(PermissionsLocation[1]) != PackageManager.PERMISSION_GRANTED)) {
                request_permissions = true;
            }
        }
        if (request_permissions) {
            View.OnClickListener permission_ok = new View.OnClickListener() {
                int RequestLocationId = 26;
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT >= 23) {
                        activity_layout.requestPermissions(PermissionsLocation, RequestLocationId); //check with activity_layout ?
                    } else{
                        ActivityCompat.requestPermissions(activity_layout, PermissionsLocation, RequestLocationId);
                    }
                }
            };
            Snackbar.make(view_layout,
                    "Location access is required to indicate you where and when you could drink a coffee.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", permission_ok).show();
        } else{ //Permissions were already granted
            launch(3000,1); //each 3 seconds or each 3 meters
        }
    }

    public void launch(int min_milliseconds, int min_meters) {
        this.locManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
        try {
            this.locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, min_milliseconds, min_meters, locListener);
        }catch (SecurityException e){
            uti.log_it("GPS: Permissions for location are not granted. Launch aborted.");
            return;
        }
        uti.log_it("GPS: Location Updates started.");
    }

    public void add_coffee_area(Area new_area){
        coffee_areas.add(new_area);
    }

    public void set_default_coffee_areas(){
        coffee_areas.add(new Area(51.485997, -0.172544, "Home", true));
        coffee_areas.add(new Area(51.493782, -0.174287, "Starbucks (South Kensington)", false));
        coffee_areas.add(new Area(51.499131, -0.176300, "Imperial College EEE cafe", true));
    }









    public class Area {
        private int r2 = 655*655; //30 meters radius or 0.000655 in longitude/latitude of difference
        private int x0, y0;
        private boolean personal;
        private String name;
        private int time_in;

        Area(double x0_d, double y0_d, String name, boolean personal) {
            this.x0 = (int) (x0_d*1000000); //center of circle
            this.y0 = (int) (y0_d*1000000); //center of circle
            this.name = name;
            this.personal = personal;
        }

        public Boolean isPersonal() {
            return this.personal;
        }

        public String getName() {
            return this.name;
        }

        public void setTimein(int time_in) {
            this.time_in = time_in;
        }

        public int getTimein() {
            return this.time_in;
        }

        public Boolean isInArea(double x, double y) {
            if ((x - this.x0) * (x - this.x0) + (y - this.y0) * (y - this.y0) <= this.r2) {
                return true;
            }
            return false;
        }
    }

}
