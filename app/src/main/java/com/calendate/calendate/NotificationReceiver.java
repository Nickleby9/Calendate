package com.calendate.calendate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Hilay on 24-ספטמבר-2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = "Calendate event";
        String text = "Touch here to see the event";
        int id = 0;

        if (intent.getExtras() != null) {
            title = intent.getExtras().getString("title");
            text = intent.getExtras().getString("text");
            id = intent.getExtras().getInt("id");
        }

        NotificationManager notificationManagerCompat =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeatingIntent = new Intent(context, MainActivity.class);
        repeatingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.calendate_notification)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setContentTitle(context.getString(R.string.reminder) + title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text/* + "\n" + context.getString(R.string.touch_for_info)*/));

        if (notificationManagerCompat != null) {
            notificationManagerCompat.notify(id, builder.build());
        }
    }
}
