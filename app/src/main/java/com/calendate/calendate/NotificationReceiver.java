package com.calendate.calendate;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.calendate.calendate.models.Alert;
import com.calendate.calendate.models.Event;
import com.calendate.calendate.utils.MyUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Hilay on 24-ספטמבר-2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Event event;
        String title = "Calendate event";
        String text = "Touch here to see the event";

        if (intent.getExtras() != null) {
//            event = intent.getExtras().getParcelable("event");
            title = intent.getExtras().getString("title");
            text = intent.getExtras().getString("text");
//            if (event != null)
//                title = event.getTitle();
        }
/*
        if (action != null && action.equals("ALARM")) {
            if (intent.getExtras() != null) {
//                event = (Event) intent.getExtras().get("event");
                event = intent.getExtras().getParcelable("event");
                if (event != null)
                    title = event.getTitle();
            }
        }
*/
        NotificationManager notificationManagerCompat =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeatingIntent = new Intent(context, MainActivity.class);
        repeatingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, repeatingIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.calendate_notification)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setContentTitle("Reminder - " + title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text + "\nTouch for more info"));

        notificationManagerCompat.notify(0, builder.build());

        //if (intent.getIntExtra("num", 0) > 0)
        //   checkForAnother(event, context);
    }

    private void checkForAnother(Event event, Context context) {
        int repeat = event.getRepeatPos();
        long repeatInMillis = 0;
        switch (repeat) {
            case 0: //none

                break;
            case 1: //daily
                repeatInMillis = 24 * 60 * 60 * 1000;
                break;
            case 2: //weekly
                repeatInMillis = 7 * 24 * 60 * 60 * 1000;
                break;
            case 3: //monthly
                repeatInMillis = 30 * 7 * 24 * 60 * 60 * 1000;
                break;
            case 4: //yearly
                repeatInMillis = 217728000000L;
                break;
        }

        ArrayList<Alert> alerts = event.getAlerts();
        int count = alerts.get(0).getCount();
        int kind = alerts.get(0).getKind();

        long newDateInMillis = 0;
        long time = 0;

        switch (kind) {
            case 0: //minutes
                time = 60 * 1000;
                break;
            case 1: //hours
                time = 60 * 60 * 1000;
                break;
            case 2: //days
                time = 24 * 60 * 60 * 1000;
                break;
            case 3: //weeks
                time = 7 * 24 * 60 * 60 * 1000;
                break;
            case 4: //months
                time = 604800000;
                break;
        }
        time = time * count;
        Calendar date = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MyUtils.dateForamt);
        try {
            date.setTime(simpleDateFormat.parse(event.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        newDateInMillis = date.getTimeInMillis() - time;

        Intent intent = new Intent(context, NotificationReceiver.class);
        alerts.remove(0);
        intent.putExtra("event", event);
        intent.putExtra("num", alerts.size());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, newDateInMillis, pendingIntent);
    }
}
