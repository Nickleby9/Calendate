package com.calendate.calendate;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

/**
 * Created by Hilay on 24-ספטמבר-2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = "Calendate event";
        String text = "Touch here to see the event";
        int id = 0;
        String repeat = "";
        int alarmKind = -1, alarmCount = -1;
        String kind = "";
        String timingTitle = "";

        if (intent.getExtras() != null) {
            title = intent.getExtras().getString("title");
            text = intent.getExtras().getString("text");
            id = intent.getExtras().getInt("id");
            repeat = intent.getExtras().getString("repeat");
            alarmCount = intent.getExtras().getInt("beforeTime");
            alarmKind = intent.getExtras().getInt("before");

            if (alarmCount > 0 && alarmKind != -1) {
                switch (alarmKind) {
                    case 0:
                        if (alarmCount == 1)
                            kind = context.getString(R.string.note_minute);
                        else
                            kind = context.getString(R.string.note_minutes);
                        break;
                    case 1:
                        if (alarmCount == 1)
                            kind = context.getString(R.string.note_hour);
                        else
                            kind = context.getString(R.string.note_hours);
                        break;
                    case 2:
                        if (alarmCount == 1)
                            kind = context.getString(R.string.note_day);
                        else
                            kind = context.getString(R.string.note_days);
                        break;
                    case 3:
                        if (alarmCount == 1)
                            kind = context.getString(R.string.note_week);
                        else
                            kind = context.getString(R.string.note_weeks);
                        break;
                    case 4:
                        if (alarmCount == 1)
                            kind = context.getString(R.string.note_month);
                        else
                            kind = context.getString(R.string.note_months);
                        break;
                }
                timingTitle = " (" + context.getString(R.string.note_in) + " " + alarmCount + " " + kind + ")";
            }
        }

        NotificationManager notificationManagerCompat =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent activityIntent = new Intent(context, MainActivity.class);
//        activityIntent.putExtra("model");
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_calendate_notification)
                .setAutoCancel(true)
                .setColor(Color.argb(70, 2, 136, 209)) //colorPrimaryDark
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setContentTitle(context.getString(R.string.reminder) + " " + title + timingTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text/* + "\n" + context.getString(R.string.touch_for_info)*/));

        if (notificationManagerCompat != null) {
            notificationManagerCompat.notify(id, builder.build());
        }

        if (repeat != null && !repeat.equals("none")) {
            Intent alarmIntent = new Intent(context, NotificationReceiver.class);
            alarmIntent.putExtra("title", title);
            alarmIntent.putExtra("text", text);
            alarmIntent.putExtra("id", id);
            alarmIntent.putExtra("before", alarmKind);
            alarmIntent.putExtra("beforeTime", alarmCount);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.SECOND, 0);
                switch (repeat) {
                    case "daily":
                        alarmIntent.putExtra("repeat", "daily");
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        break;
                    case "weekly":
                        alarmIntent.putExtra("repeat", "weekly");
                        calendar.add(Calendar.DAY_OF_MONTH, 7);
                        break;
                    case "monthly":
                        alarmIntent.putExtra("repeat", "monthly");
                        calendar.add(Calendar.MONTH, 1);
                        break;
                    case "yearly":
                        alarmIntent.putExtra("repeat", "yearly");
                        calendar.add(Calendar.YEAR, 1);
                        break;
                }
                pendingIntent = PendingIntent.getBroadcast(context, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            }
        }
    }
}
