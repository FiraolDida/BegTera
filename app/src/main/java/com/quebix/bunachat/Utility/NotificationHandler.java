package com.quebix.bunachat.Utility;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationHandler extends Application {

    public static final String MATCHED_CHANNEL_ID = "matchedChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel matchChannel = new NotificationChannel(
                    MATCHED_CHANNEL_ID,
                    "Matched",
                    NotificationManager.IMPORTANCE_HIGH
            );
            matchChannel.setDescription("You have a match");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(matchChannel);
        }
    }
}
