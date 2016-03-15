package com.stamina.staminapp;

import android.content.Context;
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
    private String g_cookie;
    private Utilities uti;

    public Networking(Context context, String ip){
        set_url(ip);
        this.uti = new Utilities(context);
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
        if ((var != null) && (var.length() > 0)){
            return true;
        }
        return false;
    }

    private String map2url(Map<String,String> map_parameters){
        String url_parameters_string = "";
        for (Map.Entry<String,String> entry : map_parameters.entrySet()){
            url_parameters_string += entry.getKey()+"="+(String)entry.getValue()+"&";
        }
        if (!url_parameters_string.equals("")) {
            url_parameters_string = url_parameters_string.substring(0,url_parameters_string.length()-1);
        }
        return url_parameters_string;
    }

    public String post(String page, Map<String,String> map_parameters)
    {
        String targetURL = this.server_url + "/" + page;
        String urlParameters = map2url(map_parameters);
        HttpURLConnection connection = null;
        try {
            //Creates connection
            uti.log_it(" <- "+urlParameters);
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            if (string_ok(this.g_cookie)) {
                connection.setRequestProperty("Cookie", this.g_cookie);
            }
            connection.setConnectTimeout(300); //XXX Maybe to low...
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
            uti.log_it(" <- "+response_str);
            if (response_str.equals("false")){
                uti.toast_it("Upload failed", 500);
                uti.log_it("Upload to server failed (bad login or something...)");
                //XXX eventually retry? logout?
                return null;
            }
            return response_str;
        } catch (SocketTimeoutException e){
            uti.toast_it("Server can't be reached (timeout)", 400);
            uti.log_it("Networking: Server can't be reached (timeout)");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            uti.toast_it("Network error", 400);
            uti.log_it("Networking: Exception from HTTP post");
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}


