package com.mehmetture.mgu1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationHelper {

    private static final String CHANNEL_ID = "FCM_CHANNEL";
    private static final String CHANNEL_NAME = "Firebase Notification";
    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "key=AIzaSyCeNWm37HvS-5yX1kd0eFs6J-LDf9u5do4"; // Sunucu anahtarını buraya ekle

    public static void sendNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android Oreo (API 26) ve üstü için NotificationChannel oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Bildirim oluştur
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.contact_parent_icon) // Bildirim ikonu
                .setContentTitle(title) // Başlık
                .setContentText(message) // İçerik
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Öncelik
                .setAutoCancel(true); // Tıklanınca bildirimi kapat

        // Bildirimi göster
        notificationManager.notify(0, builder.build());
    }

    // Token kullanarak bildirim gönderme
    public static void sendNotificationToToken(String targetToken, String title, String message) {
        OkHttpClient client = new OkHttpClient();

        JSONObject payload = new JSONObject();
        try {
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", message);

            payload.put("to", targetToken); // Hedef token
            payload.put("notification", notification);
        } catch (JSONException e) {
            Log.e("NotificationHelper", "JSON oluşturulurken hata: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(FCM_API_URL)
                .post(body)
                .addHeader("Authorization", SERVER_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("NotificationHelper", "FCM bildirimi gönderilemedi: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("NotificationHelper", "Bildirim başarıyla gönderildi.");
                } else {
                    Log.e("NotificationHelper", "Bildirim gönderilirken hata oluştu: " + response.body().string());
                }
            }
        });
    }
}
