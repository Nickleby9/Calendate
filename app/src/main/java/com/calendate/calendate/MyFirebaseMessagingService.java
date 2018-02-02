package com.calendate.calendate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void handleIntent(Intent intent) {

        //get the payload from your notification
        String username = intent.getExtras().getString("username");
        String userUid = intent.getExtras().getString("userUid");
        String type = intent.getExtras().getString("type");
        //
        //super if the app is in the background:
        //send a push notification "DEFAULT" title and icon


        //if the app is in the foreground:
        //send the push to onMessageReceived
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(userUid) || userUid.equals("everyone")) {

                Intent contentIntent = new Intent(this, MainActivity.class);
                PendingIntent pi =
                        PendingIntent.getActivity(this, 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                switch (type) {
                    case "newEvent":
                        builder.setContentTitle(getString(R.string.note_new_share))
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(username + " " + getString(R.string.note_share_msg)))
                                .setSmallIcon(R.drawable.ic_stat_calendate_notification)
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setContentIntent(pi);
                        mgr.notify(1, builder.build());
                        break;
                    case "friendRequest":
                        builder.setContentTitle(getString(R.string.note_new_request))
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(username + " " + getString(R.string.note_friend_msg)))
                                .setSmallIcon(R.drawable.ic_stat_calendate_notification)
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setContentIntent(pi);
                        mgr.notify(2, builder.build());
                        break;
                    case "all":
                        builder.setContentTitle(intent.getStringExtra("title"))
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(intent.getStringExtra("msg")))
                                .setSmallIcon(R.drawable.ic_stat_calendate_notification)
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL);
                        mgr.notify(3, builder.build());
                        break;
                }
            }
        }
    }
}