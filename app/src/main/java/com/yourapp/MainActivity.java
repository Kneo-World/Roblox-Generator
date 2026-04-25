package com.yourapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

public class MainActivity extends Activity {

    private WebView webView;
    private Vibrator vibrator;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 100;
    private boolean blocked = false; // флаг, что пранк активен

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.loadUrl("file:///android_asset/index.html");
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Запрашиваем разрешение на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }
    }

    // Возвращаемся на передний план, если пользователь попытался свернуть
    @Override
    protected void onUserLeaveHint() {
        if (blocked) {
            // Возвращаем активность немедленно
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        super.onUserLeaveHint();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Если пранк активен, но экран блокировки не показывается (напр., после возврата), перезагружаем WebView
        if (blocked && webView != null) {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    public class WebAppInterface {
        Activity mActivity;

        WebAppInterface(Activity activity) {
            mActivity = activity;
        }

        @JavascriptInterface
        public void finish() {
            mActivity.finish();
        }

        @JavascriptInterface
        public void startBlockService() {
            blocked = true;
            // Запускаем сервис уведомления
            Intent serviceIntent = new Intent(mActivity, BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mActivity.startForegroundService(serviceIntent);
            } else {
                mActivity.startService(serviceIntent);
            }
        }

        @JavascriptInterface
        public void vibrate(int ms) {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(ms);
                }
            }
        }

        @JavascriptInterface
        public void unlockAndExit() {
            blocked = false;
            // Останавливаем сервис
            Intent serviceIntent = new Intent(mActivity, BackgroundService.class);
            mActivity.stopService(serviceIntent);
            // Показываем диалог и закрываем
            new android.app.AlertDialog.Builder(mActivity)
                .setTitle("Доступ восстановлен")
                .setMessage("Система разблокирована")
                .setPositiveButton("OK", (dialog, which) -> mActivity.finish())
                .show();
        }

        @JavascriptInterface
        public void restartServiceIfNeeded() {
            // Проверка, что сервис жив (если вдруг умер)
            if (blocked) {
                Intent serviceIntent = new Intent(mActivity, BackgroundService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mActivity.startForegroundService(serviceIntent);
                } else {
                    mActivity.startService(serviceIntent);
                }
            }
        }
    }
                }
