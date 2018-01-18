package com.calendate.calendate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Map;


public class BootCompletedReceiver extends BroadcastReceiver {
    String prefsKey, prefsValue;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("events", Context.MODE_PRIVATE);
        Map<String, ?> keys = prefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            if (entry.getKey() != null) {
                prefsValue = entry.getValue().toString();
                prefsKey = entry.getKey();
                String[] alerts = prefsValue.split("~!~");
                String eventTitle = alerts[0];
                String eventText = alerts[1];
                String repeatStr = alerts[2];
                String alertId = alerts[3];
                String eventKind = alerts[4];
                String eventCount = alerts[5];
                String millis = alerts[6];
                int repeat = Integer.valueOf(repeatStr);
                long eventMillis = Long.valueOf(millis);
                Calendar now = Calendar.getInstance();
                Calendar date = Calendar.getInstance();
                Calendar eventDate = Calendar.getInstance();
                date.setTimeInMillis(eventMillis);
                eventDate.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH),
                        date.get(Calendar.HOUR), date.get(Calendar.MINUTE), 0);
                if (eventDate.getTimeInMillis() > now.getTimeInMillis())
                    createNotification(eventDate.getTimeInMillis(), alertId, repeat, eventKind, eventCount, eventTitle, eventText, context);
                else if (Integer.valueOf(repeat) == 0)
                    prefs.edit().remove(prefsKey).apply();
                else {
                    switch (repeat) {
                        case 1:
                            while (eventDate.getTimeInMillis() < now.getTimeInMillis()) {
                                eventDate.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            break;
                        case 2:
                            while (eventDate.getTimeInMillis() < now.getTimeInMillis()) {
                                eventDate.add(Calendar.DAY_OF_MONTH, 7);
                            }
                            break;
                        case 3:
                            while (eventDate.getTimeInMillis() < now.getTimeInMillis()) {
                                eventDate.add(Calendar.MONTH, 1);
                            }
                            break;
                        case 4:
                            while (eventDate.getTimeInMillis() < now.getTimeInMillis()) {
                                eventDate.add(Calendar.YEAR, 1);
                            }
                            break;
                    }
                    createNotification(eventDate.getTimeInMillis(), alertId, repeat, eventKind, eventCount, eventTitle, eventText, context);
                }
            }
        }
    }

    private void createNotification(long millis, String alertId, int repeat, String alarmKind, String alarmCount,
                                    String eventTitle, String eventText, Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) {
            Intent alarmIntent = new Intent(context, NotificationReceiver.class);
            alarmIntent.putExtra("title", eventTitle);
            alarmIntent.putExtra("text", eventText);
            alarmIntent.putExtra("repeat", repeat);

            PendingIntent pendingIntent;

            int id = Integer.valueOf(alertId);
            int kind = Integer.valueOf(alarmKind);
            int count = Integer.valueOf(alarmCount);

            alarmIntent.putExtra("id", id);
            alarmIntent.putExtra("before", kind);
            alarmIntent.putExtra("beforeTime", count);
            alarmIntent.putExtra("prefs", prefsValue);
            alarmIntent.putExtra("prefsTitle", prefsKey);
            alarmIntent.putExtra("millis", millis);

            pendingIntent = PendingIntent.getBroadcast(
                    context, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
        } else
            Toast.makeText(context, R.string.no_alarm_service, Toast.LENGTH_SHORT).show();
    }
}
