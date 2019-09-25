package com.quebix.bunachat.Utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.quebix.bunachat.R;

import java.util.Random;

public class FirebaseMessagingService extends com.quebix.bunachat.FirebaseMessagingService {

    public static final String TAG = "FirebaseMessageService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        showNotification(remoteMessage.getNotification().getTitle(), 
                remoteMessage.getNotification().getBody());
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager)
                getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "com.quebix.bunachat";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Matchup");
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), NOTIFICATION_CHANNEL_ID);

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body);

        manager.notify(new Random().nextInt(), builder.build());
    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: " + s);
    }
}
