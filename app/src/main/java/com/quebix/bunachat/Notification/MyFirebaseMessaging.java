package com.quebix.bunachat.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;
import com.quebix.bunachat.Activity.MessageActivity;
import com.quebix.bunachat.FirebaseMessagingService;
import com.quebix.bunachat.Fragment.UsersFragment;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: ");
        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = preferences.getString("currentUser", "none");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sent.equals(firebaseUser.getUid())){
            if (!currentUser.equals(user)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    sendOreoNotification(remoteMessage);
                } else {
                    sendNotification(remoteMessage);
                }
            }
        }
    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        if (title.equals("Match Request")){
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
            Intent intent = new Intent(this, UsersFragment.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            OreoNotification oreoNotification = new OreoNotification(this);
            Notification.Builder builder = oreoNotification.getOreoNotification(title, body,
                    pendingIntent, defaultSound, icon);

            int i = 0;
            if (j > 0){
                i = j;
            }

            oreoNotification.getManager().notify(i, builder.build());

        } else {
            String[] name = body.split(":", 2);

            RemoteMessage.Notification notification = remoteMessage.getNotification();
            int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra("userID", user);
            intent.putExtra("profileImage", "default");
            intent.putExtra("name", name[0]);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            OreoNotification oreoNotification = new OreoNotification(this);
            Notification.Builder builder = oreoNotification.getOreoNotification(title, body,
                    pendingIntent, defaultSound, icon);

            int i = 0;
            if (j > 0){
                i = j;
            }

            oreoNotification.getManager().notify(i, builder.build());
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        if (title.equals("Match Request")){
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
            Intent intent = new Intent(this, UsersFragment.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(Integer.parseInt(icon))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSound)
                    .setContentIntent(pendingIntent);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int i = 0;
            if (j > 0){
                i = j;
            }

            manager.notify(i, builder.build());
        }else {
            String[] name = body.split(":", 2);

            RemoteMessage.Notification notification = remoteMessage.getNotification();
            int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra("userID", user);
            intent.putExtra("profileImage", "default");
            intent.putExtra("name", name[0]);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(Integer.parseInt(icon))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSound)
                    .setContentIntent(pendingIntent);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int i = 0;
            if (j > 0){
                i = j;
            }

            manager.notify(i, builder.build());
        }

    }
}
