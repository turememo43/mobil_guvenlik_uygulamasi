package com.mehmetture.mgu1;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChildLocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private DatabaseReference databaseReference;
    private static final String TAG = "ChildLocationService";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service onCreate called.");

        // Dinamik olarak Parent ve Child UID'leri Firebase'den al
        fetchParentUidFromFirebase();
        fetchChildUidFromFirebase();

        // İzin kontrolü ve konum güncellemelerini başlat
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "ACCESS_FINE_LOCATION izni verilmemiş. Servis durduruluyor.");
            stopSelf();
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        Log.d(TAG, "Firebase ve FusedLocationProviderClient başlatıldı.");

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d(TAG, "Konum alındı: Lat=" + location.getLatitude() + ", Lng=" + location.getLongitude());
                    sendLocationToFirebase(location.getLatitude(), location.getLongitude());
                } else {
                    Log.e(TAG, "Konum alınamadı.");
                }
            }
        };

        requestLocationUpdates();
    }


    private void requestLocationUpdates() {
        Log.d(TAG, "requestLocationUpdates çağrıldı.");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "ACCESS_FINE_LOCATION izni verilmemiş. Servis durduruluyor.");
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

        Log.d(TAG, "Konum güncellemeleri başlatıldı.");
    }

    private void sendLocationToFirebase(double latitude, double longitude) {
        Log.d(TAG, "sendLocationToFirebase çağrıldı. Lat=" + latitude + ", Lng=" + longitude);

        // Parent UID ve Child UID'yi SharedPreferences'ten alıyoruz
        String parentUID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("parentUid", null);
        String childUID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("childUid", null);

        if (parentUID != null && childUID != null) {
            // Firebase veritabanı referansı
            DatabaseReference locationRef = databaseReference.child(parentUID)
                    .child("children")
                    .child(childUID)
                    .child("location");

            // Konum bilgisi Firebase'e gönderiliyor
            locationRef.setValue(new LocationData(latitude, longitude))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Konum başarıyla Firebase'e gönderildi.");
                        } else {
                            Log.e(TAG, "Konum gönderimi başarısız oldu.", task.getException());
                        }
                    });
        } else {
            Log.e(TAG, "Parent UID veya Child UID bulunamadı.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy called. Konum güncellemeleri durduruluyor.");
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

    private void fetchChildUidFromFirebase() {
        Log.d(TAG, "fetchChildUidFromFirebase çağrıldı.");

        String parentUID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("parentUid", null);

        if (parentUID == null) {
            Log.e(TAG, "Parent UID SharedPreferences'te bulunamadı. Child UID alınamayacak.");
            fetchParentUidFromFirebase(); // Parent UID'yi bulmaya çalış
            return;
        }

        DatabaseReference childrenRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(parentUID)
                .child("children");

        childrenRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                    String childUID = childSnapshot.getKey();
                    Log.d(TAG, "Firebase'den alınan Child UID: " + childUID);

                    // SharedPreferences'e kaydet
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putString("childUid", childUID);
                    editor.apply();

                    Log.d(TAG, "Child UID SharedPreferences'e kaydedildi: " + childUID);
                    return;
                }
                Log.e(TAG, "Child UID Firebase'de bulunamadı.");
            } else {
                Log.e(TAG, "Firebase'den Child UID alınamadı.", task.getException());
            }
        });
    }

    private void fetchParentUidFromFirebase() {
        Log.d(TAG, "fetchParentUidFromFirebase çağrıldı.");

        String childUID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("childUid", null);

        if (childUID == null) {
            Log.e(TAG, "Child UID SharedPreferences'te bulunamadı. Parent UID alınamayacak.");
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot parentSnapshot : task.getResult().getChildren()) {
                    if (parentSnapshot.child("children").hasChild(childUID)) {
                        String parentUID = parentSnapshot.getKey();
                        Log.d(TAG, "Firebase'den Parent UID bulundu: " + parentUID);

                        // SharedPreferences'e kaydet
                        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                        editor.putString("parentUid", parentUID);
                        editor.apply();

                        Log.d(TAG, "Parent UID SharedPreferences'e kaydedildi: " + parentUID);
                        return;
                    }
                }
                Log.e(TAG, "Parent UID Firebase'de bulunamadı.");
            } else {
                Log.e(TAG, "Firebase'den Parent UID alınamadı.", task.getException());
            }
        });
    }







}