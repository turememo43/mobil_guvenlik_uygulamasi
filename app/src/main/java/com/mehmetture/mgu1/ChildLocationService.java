package com.mehmetture.mgu1;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChildLocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();

        // İzin kontrolü
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf(); // İzin yoksa servisi durdur
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // locationCallback tanımlanıyor
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    sendLocationToFirebase(location.getLatitude(), location.getLongitude());
                } else {
                    Log.e("ChildLocationService", "Konum alınamadı.");
                }
            }
        };

        // Konum güncellemelerini başlat
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf(); // İzin yoksa servisi durdur
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(5000) // Interval millis
                .setMinUpdateIntervalMillis(2000) // Minimum güncelleme aralığı
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Hassasiyet
                .build();

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null // Looper
        );
    }

    private void sendLocationToFirebase(double latitude, double longitude) {
        // Parent UID ve Child UID'yi SharedPreferences'ten alıyoruz
        String parentUID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("parentUid", null);
        String childUID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("childUid", null);

        if (parentUID != null && childUID != null) {
            // Firebase veritabanı referansı
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(parentUID)
                    .child("children")
                    .child(childUID)
                    .child("location");

            // Konum bilgisi Firebase'e gönderiliyor
            databaseReference.setValue(new LocationData(latitude, longitude))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("ChildLocationService", "Konum başarıyla Firebase'e gönderildi.");
                        } else {
                            Log.e("ChildLocationService", "Konum gönderimi başarısız oldu.", task.getException());
                        }
                    });
        } else {
            Log.e("ChildLocationService", "Parent UID veya Child UID bulunamadı.");
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class LocationData {
        public double latitude;
        public double longitude;

        public LocationData() {}

        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
