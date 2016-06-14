package com.example.davidoyeku.custom_classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import com.example.davidoyeku.m_diary.MainActivity;
import com.example.davidoyeku.m_diary.R;

/**
 * Created by DavidOyeku on 23/04/15.
 */
public class NotifyService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        //get default notification sound uri
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Write to M-Diary")
                .setContentText("blah blah blah")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setSound(sound)
                .build();

        notificationManager.notify(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
