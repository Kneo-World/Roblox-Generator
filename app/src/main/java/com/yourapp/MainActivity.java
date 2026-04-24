package com.yourapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Random;

public class MainActivity extends Activity {

    private WebView webView;
    private Vibrator vibrator;

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
        public void hideApp() {
            // Запускаем фоновый сервис с уведомлением
            Intent serviceIntent = new Intent(mActivity, BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mActivity.startForegroundService(serviceIntent);
            } else {
                mActivity.startService(serviceIntent);
            }
            // Сворачиваем приложение (исчезает)
            mActivity.moveTaskToBack(true);
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
            // Останавливаем сервис
            Intent serviceIntent = new Intent(mActivity, BackgroundService.class);
            mActivity.stopService(serviceIntent);
            // Показываем системное уведомление "Доступ восстановлен"
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivity);
            builder.setTitle("Доступ восстановлен");
            builder.setMessage("Система разблокирована");
            builder.setPositiveButton("OK", (dialog, which) -> mActivity.finish());
            builder.show();
        }

        @JavascriptInterface
        public void restartServiceIfNeeded() {
            // Проверка, что сервис жив (используется при открытии приложения)
            Intent serviceIntent = new Intent(mActivity, BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mActivity.startForegroundService(serviceIntent);
            } else {
                mActivity.startService(serviceIntent);
            }
        }
    }
}
