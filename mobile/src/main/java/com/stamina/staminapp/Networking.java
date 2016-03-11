package com.stamina.staminapp;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

public class Networking {
    private String server_url;
    //private CookieManager cookieManager = new CookieManager();
    private String g_cookie = new String();

    public Networking(String ip){
        set_url(ip);
    }

    public void set_url(String ip){
        this.server_url = build_url(ip);
    }

    private String build_url(String ip){
        return "http://"+ip+":3000";
    }

    public String get_cookie(){
        return this.g_cookie;
    }

    public void set_cookie(String cookie){ this.g_cookie = cookie;}

    private boolean string_ok(String var){
        if (var != null && var.length() > 0){
            return true;
        }
        return false;
    }

    private String map2url(Map<String,String> map_parameters){
        String urlparameters = "";
        for (Map.Entry<String,String> entry : map_parameters.entrySet()){
            urlparameters += entry.getKey()+"="+(String)entry.getValue()+"&";
        }
        if (!urlparameters.equals("")) {
            urlparameters = urlparameters.substring(0,urlparameters.length()-1);
        }
        return urlparameters;
    }

    public String post(String page, Map<String,String> map_parameters)
    {
        String targetURL = this.server_url + "/" + page;
        String urlParameters = map2url(map_parameters);
        HttpURLConnection connection = null;
        try {
            //Creates connection
            Log.d("com.stamina.staminapp"," -> "+urlParameters);
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            if (string_ok(this.g_cookie)) {
                connection.setRequestProperty("Cookie", this.g_cookie);
            }
            connection.setConnectTimeout(300); //XXX Maybe to low...
            //connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Sends request
            OutputStream os = connection.getOutputStream();
            DataOutputStream output = new DataOutputStream(os);
            output.writeBytes(urlParameters);
            output.flush();
            output.close();

            //Get Response
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader input = new BufferedReader(isr);
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = input.readLine()) != null) {
                response.append(line + '\n');
            }
            input.close();
            String temp_cookie = connection.getHeaderField("set-cookie");
            if (string_ok(temp_cookie)) {
                this.g_cookie = temp_cookie;
            }
            String response_str = response.toString();
            response_str = response_str.substring(0,response_str.length()-1);
            Log.d("com.stamina.staminapp"," <- "+response_str+" (size "+Integer.toString(response_str.length())+")");
            return response_str;
        } catch (SocketTimeoutException e){
            Log.e("com.stamina.staminapp","Connection with the server could not be established");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}


