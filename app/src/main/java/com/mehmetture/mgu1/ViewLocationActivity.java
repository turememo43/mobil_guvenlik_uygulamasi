package com.mehmetture.mgu1;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewLocationActivity extends AppCompatActivity {

    private TextView locationTextView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);

        locationTextView = findViewById(R.id.locationTextView);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        String childUID = getIntent().getStringExtra("childUID");
        if (childUID != null) {
            fetchChildLocation(childUID);
        }
    }

    private void fetchChildLocation(String childUID) {
        DatabaseReference locationRef = databaseReference.child(childUID).child("location");
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);
                    locationTextView.setText("Konum: \nLat: " + latitude + "\nLong: " + longitude);
                } else {
                    locationTextView.setText("Konum bilgisi yok.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                locationTextView.setText("Konum alınamadı.");
            }
        });
    }
}
