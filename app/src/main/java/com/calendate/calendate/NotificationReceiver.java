package com.calendate.calendate;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;
import java.util.Map;

public class NotificationReceiver extends BroadcastReceiver {

    SharedPreferences prefs;
    String prefsKey = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = "Calendate event";
        String text = "Touch here to see the event";
        int id = 0;
        int repeat = -1;
        int alarmKind = -1, alarmCount = -1;
        String kind = "";
        String timingTitle = "";
        String eventPrefs = "";
        prefs = context.getSharedPreferences("events", Context.MODE_PRIVATE);
        long millis = System.currentTimeMillis();

        if (intent.getExtras() != null) {
            title = intent.getExtras().getString("title");
            text = intent.getExtras().getString("text");
            id = intent.getExtras().getInt("id");
            repeat = intent.getExtras().getInt("repeat");
            alarmCount = intent.getExtras().getInt("beforeTime");
            alarmKind = intent.getExtras().getInt("before");
            eventPrefs = intent.getExtras().getString("prefs");
            prefsKey = intent.getExtras().getString("prefsTitle");
            millis = intent.getLongExtra("millis", System.currentTimeMillis());

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

        Intent activityIntent = new Intent(context, DetailedItemActivity.class);
        activityIntent.putExtra("eventKey", prefsKey.substring(0, prefsKey.length()-3));
        activityIntent.putExtra("btnTitle", "");
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
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text + "\n" + context.getString(R.string.touch_for_info)));

        if (notificationManagerCompat != null) {
            notificationManagerCompat.notify(id, builder.build());
            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                if (entry.getKey() != null) {
                    if (entry.getKey().equals(prefsKey)){
                        prefs.edit().remove(prefsKey).apply();
                    }
                }
            }
        }

        if (repeat > 0) {
            checkForRepeat(context, title, text, id, repeat, alarmKind, alarmCount, millis);
        }
    }

    private void checkForRepeat(Context context, String title, String text, int id, int repeat, int alarmKind, int alarmCount, long millis) {
        PendingIntent pendingIntent;Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        alarmIntent.putExtra("title", title);
        alarmIntent.putExtra("text", text);
        alarmIntent.putExtra("id", id);
        alarmIntent.putExtra("before", alarmKind);
        alarmIntent.putExtra("beforeTime", alarmCount);
        alarmIntent.putExtra("repeat", repeat);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);
            calendar.set(Calendar.SECOND, 0);
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                switch (repeat) {
                    case 1:
                        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        break;
                    case 2:
                        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_MONTH, 7);
                        }
                        break;
                    case 3:
                        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                            calendar.add(Calendar.MONTH, 1);
                        }
                        break;
                    case 4:
                        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                            calendar.add(Calendar.YEAR, 1);
                        }
                        break;
                }
            }

            String eventPrefs = title + "~!~" + text + "~!~" + repeat + "~!~" + id + "~!~" + alarmKind + "~!~" + alarmCount + "~!~" + calendar.getTimeInMillis();
            alarmIntent.putExtra("prefs",eventPrefs);
            alarmIntent.putExtra("prefsTitle",prefsKey);
            alarmIntent.putExtra("millis", calendar.getTimeInMillis());
            prefs.edit().putString(prefsKey, eventPrefs).apply();
            pendingIntent = PendingIntent.getBroadcast(context, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        }
    }
}
