package com.example.worktracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public class MyForegroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );



        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentText("Service is running")
                .setContentTitle("Service enabled")
                .setSmallIcon(R.drawable.ic_baseline_work_24)
                .setOnlyAlertOnce(true);

        startForeground(1001, notification.build());

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {

                            Log.e("Service", "Service is running...");
                            try {
                                //every 24 hours, send notification
                                TimeUnit.HOURS.sleep(24);
                                //Thread.sleep(3000);
                                stopForeground(true);
                                getSystemService(NotificationManager.class).createNotificationChannel(channel);
                                Notification.Builder notification = new Notification.Builder(getApplicationContext(), CHANNELID)
                                        .setContentText("Don't Forget Fill in a work!")
                                        .setContentTitle("Reminder!")
                                        .setSmallIcon(R.drawable.ic_baseline_work_24)
                                        .setOnlyAlertOnce(true);
                                startForeground(1002,notification.build());
                                //sleep for 10 seconds
                                Thread.sleep(10000);
                                stopForeground(true);



                                getSystemService(NotificationManager.class).createNotificationChannel(channel);
                                Notification.Builder notification1 = new Notification.Builder(getApplicationContext(), CHANNELID)
                                        .setContentText("Service is running")
                                        .setContentTitle("Service enabled")
                                        .setSmallIcon(R.drawable.ic_baseline_work_24)
                                        .setOnlyAlertOnce(true);

                                startForeground(1001, notification1.build());


                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();


        return super.onStartCommand(intent, flags, startId);
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
