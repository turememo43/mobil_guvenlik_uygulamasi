package com.mehmetture.mgu1;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewLocationActivity extends AppCompatActivity {

    private TextView locationTextView;
    private DatabaseReference databaseReference;
    private static final String TAG = "ViewLocationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);

        locationTextView = findViewById(R.id.locationTextView);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Intent'ten childUID'yi al
        String childUID = getIntent().getStringExtra("childUID");
        if (childUID == null || childUID.isEmpty()) {
            Toast.makeText(this, "Child UID eksik.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Child UID alınamadı. Intent doğru yapılandırılmamış.");
            finish();
            return;
        }

        Log.d(TAG, "Alınan Child UID: " + childUID);
        fetchChildLocation(childUID);
    }

    private void fetchChildLocation(String childUID) {
        // Firebase hiyerarşisini kontrol et
        String parentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (parentUID == null || parentUID.isEmpty()) {
            Toast.makeText(this, "Parent UID eksik.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Parent UID SharedPreferences'te bulunamadı.");
            finish();
            return;
        }

        Log.d(TAG, "Parent UID: " + parentUID);

        DatabaseReference locationRef = databaseReference.child(parentUID).child("children").child(childUID).child("location");
        Log.d(TAG, "Firebase hiyerarşisi: Users/" + parentUID + "/children/" + childUID + "/location");

        locationTextView.setText("Konum bilgisi yükleniyor...");

        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double latitude = snapshot.child("latitude").getValue(Double.class);
                    Double longitude = snapshot.child("longitude").getValue(Double.class);

                    if (latitude != null && longitude != null) {
                        locationTextView.setText("Konum: \nLat: " + latitude + "\nLong: " + longitude);
                        Log.d(TAG, "Konum başarıyla alındı: Lat=" + latitude + ", Lng=" + longitude);
                    } else {
                        locationTextView.setText("Konum bilgisi eksik.");
                        Log.e(TAG, "Konum bilgisi eksik.");
                    }
                } else {
                    locationTextView.setText("Konum bilgisi yok.");
                    Log.e(TAG, "Konum bilgisi Firebase'de yok.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                locationTextView.setText("Konum alınamadı.");
                Log.e(TAG, "Konum alınırken hata oluştu: " + error.getMessage());
            }
        });
    }
}