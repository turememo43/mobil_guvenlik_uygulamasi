package com.mehmetture.mgu1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ParentDashboardActivity extends AppCompatActivity {

    private Button addChildButton, viewChildrenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        // View'ları Tanımlama
        addChildButton = findViewById(R.id.addChildButton);
        viewChildrenButton = findViewById(R.id.viewChildrenButton);

        // "Çocuk Ekle" Butonu İşlevselliği
        addChildButton.setOnClickListener(v -> {
            Intent intent = new Intent(ParentDashboardActivity.this, AddChildActivity.class);
            startActivity(intent);
        });

        // "Çocukları Gör" Butonu İşlevselliği
        viewChildrenButton.setOnClickListener(v -> {
            Intent intent = new Intent(ParentDashboardActivity.this, ViewChildrenActivity.class);
            startActivity(intent);
        });
    }
}
