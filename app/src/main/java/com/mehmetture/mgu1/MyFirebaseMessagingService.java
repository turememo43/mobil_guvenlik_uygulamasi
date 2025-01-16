package com.mehmetture.mgu1;

import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.app.NotificationChannel;

import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FirebaseToken", "Yeni token alındı: " + token);
        // Token'ı SharedPreferences'a kaydet
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("tempToken", token);
        editor.apply();
    }





    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Bildirim başlık ve mesajını al
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Başlık yok";
        String message = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "Mesaj yok";

        // Bildirimin data kısmını al (eğer varsa)
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Log.d("FCM", "Ekstra Veri: " + data.toString());
        }

        // Bildirimi göster
        showNotification(title, message);
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "default_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification) // Bildirim simgesi
                .setAutoCancel(true);

        notificationManager.notify(0, notificationBuilder.build());
    }

}
