package com.stamina.staminapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Notifications {
    private Context context;
    static private NotificationManager mNotificationManager;
    public static List<Integer> active_notifications;
    private Utilities uti;

    Notifications(Context context){
        this.active_notifications = new ArrayList<Integer>();
        this.context = context;
        if (this.mNotificationManager == null) {
            this.mNotificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        uti = new Utilities(context);
    }

    public void finish_notif(int id){
        this.mNotificationManager.cancel(id);
        if (this.active_notifications != null) {
            if (this.active_notifications.contains(id)) {
                this.active_notifications.remove(id);
            }
        }
    }

    public void makeNotification(String message, int drawable_id_small, int drawable_id_large){
        int id = 0;
        while (this.active_notifications.contains(id)) {
            id++;
        }
        Context app_context = this.context.getApplicationContext();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.context);
        mBuilder.setSmallIcon(drawable_id_small);
        if (drawable_id_large != -1) {
            Bitmap bm = BitmapFactory.decodeResource(this.context.getResources(), drawable_id_large);
            mBuilder.setLargeIcon(bm);
        } else{
            mBuilder.setColor(0x663300);
        }
        mBuilder.setPriority(1); //from -2 to 2
        mBuilder.setContentTitle("Staminapp");
        mBuilder.setContentText(message);

        Intent onClickIntent = new Intent(app_context, MainActivity.class);
        onClickIntent.putExtra("source", "notification");
        onClickIntent.putExtra("id", id);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(app_context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(onClickIntent);

        PendingIntent pendingOnClickIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingOnClickIntent);

        Notification this_notif = mBuilder.build();
        this_notif.defaults |= Notification.DEFAULT_VIBRATE;
        this_notif.defaults |= Notification.DEFAULT_SOUND;
        this.mNotificationManager.notify(id, this_notif);
        this.active_notifications.add(id);
    }



    public void makeQuestionNotification(String option){
        int id = 0;
        while (this.active_notifications.contains(id)) {
            id++;
        }
        Context app_context = this.context.getApplicationContext();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.context);
        mBuilder.setSmallIcon(R.drawable.notif_coffee);
        mBuilder.setPriority(2);
        mBuilder.setContentTitle("Staminapp");

        Intent neutral = new Intent(this.context, MainActivity.class);
        //neutral.setAction("neutral");
        PendingIntent pendingIntent_neutral = PendingIntent.getActivity(this.context, 0, neutral, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent_neutral); //XXX to rmeove

        Intent positive = new Intent();
        positive.setAction("com.stamina.staminapp.positive");
        positive.putExtra("option", option);
        positive.putExtra("id", id);
        PendingIntent pendingIntent_positive = PendingIntent.getBroadcast(this.context, 0, positive, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent negative = new Intent();
        negative.setAction("com.stamina.staminapp.negative");
        negative.putExtra("option", option);
        negative.putExtra("id",id);
        PendingIntent pendingIntent_negative = PendingIntent.getBroadcast(this.context, 0, negative, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        String time_notif = "("+Integer.toString(hours) + ":" + Integer.toString(minutes)+")";
        NotificationCompat.Action action_yes = null, action_no = null;
        Bitmap bm = null;
        if (option.equals("coffee")) {
            bm = BitmapFactory.decodeResource(this.context.getResources(),
                    R.drawable.notif_question);
            mBuilder.setLargeIcon(bm);
            mBuilder.setContentText("Please confirm you're drinking a coffee "+time_notif);
            action_yes = new NotificationCompat.Action.Builder(R.drawable.notif_coffee_yes,
                    "YES", pendingIntent_positive).build();
            action_no = new NotificationCompat.Action.Builder(R.drawable.notif_coffee_no,
                    "NO", pendingIntent_negative).build();
        } else if (option.equals("physical")){
            bm = BitmapFactory.decodeResource(this.context.getResources(),
                    R.drawable.notif_physical);
            mBuilder.setContentText("Please confirm you're doing sports "+time_notif);
            action_yes = new NotificationCompat.Action.Builder(R.drawable.notif_physical_yes,
                    "YES", pendingIntent_positive).build();
            action_no = new NotificationCompat.Action.Builder(R.drawable.notif_physical_no,
                    "NO", pendingIntent_negative).build();
        }
        mBuilder.addAction(action_yes);
        mBuilder.addAction(action_no);
        mBuilder.setLargeIcon(bm);
        mBuilder.setAutoCancel(true);
        Notification this_notif = mBuilder.build();
        this_notif.defaults |= Notification.DEFAULT_VIBRATE;
        this_notif.defaults |= Notification.DEFAULT_SOUND;
        this_notif.flags |= Notification.FLAG_AUTO_CANCEL;
        this.mNotificationManager.notify(id, this_notif);
        this.active_notifications.add(id);
    }

}
