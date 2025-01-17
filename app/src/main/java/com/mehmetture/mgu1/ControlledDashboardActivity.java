package com.mehmetture.mgu1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

public class ControlledDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlled_dashboard);

        // FAB öğesini tanımla
        FloatingActionButton fab = findViewById(R.id.fab_send_notification);

        // FAB tıklama olayını ekle
        fab.setOnClickListener(v -> {
            // SendNotificationActivity'ye yönlendir
            Intent notificationIntent = new Intent(this, ChildSendNotificationActivity.class);
            startActivity(notificationIntent);

            // ChildLocationService'i başlat
            Intent serviceIntent = new Intent(this, ChildLocationService.class);
            startService(serviceIntent);

            Log.d("ControlledDashboard", "FAB tıklandı, NotificationActivity ve LocationService başlatıldı.");
        });


    }
}
