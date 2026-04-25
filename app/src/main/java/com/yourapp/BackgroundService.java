package com.yourapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class BackgroundService extends Service {

    private static final String CHANNEL_ID = "pank_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification());
        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("⚠️ СИСТЕМА ЗАБЛОКИРОВАНА")
                .setContentText("ЗДЕСЬ НЕТ ВЫХОДА. Нажмите, чтобы вернуться")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)   // встроенный значок
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setOngoing(true)       // нельзя смахнуть
                .setAutoCancel(false)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Система блокировки",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Уведомления о блокировке устройства");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(NOTIFICATION_ID);
    }
}
