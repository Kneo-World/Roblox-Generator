package com.yourapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.app.WallpaperManager;

public class BackgroundService extends Service {

    private static final String CHANNEL_ID = "pank_channel";
    private static final int NOTIFICATION_ID = 1;
    private WallpaperManager wallpaperManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        wallpaperManager = WallpaperManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Запускаем в фореграунде с уведомлением
        startForeground(NOTIFICATION_ID, buildNotification());

        // Меняем обои на чёрный фон
        setBlackWallpaper();

        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("⚠️ СИСТЕМА ЗАБЛОКИРОВАНА")
                .setContentText("Нажмите, чтобы ввести код разблокировки")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // нельзя смахнуть
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Пранк",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Уведомления системы блокировки");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void setBlackWallpaper() {
        try {
            // Создаём чёрный Bitmap 2x2
            Bitmap blackBitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            blackBitmap.eraseColor(Color.BLACK);
            wallpaperManager.setBitmap(blackBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Останавливаем уведомление
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
              }
