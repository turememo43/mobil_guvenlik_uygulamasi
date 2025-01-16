package com.mehmetture.mgu1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class ViewChildrenActivity extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private ChildAdapter childAdapter;
    private List<Child> childList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String parentUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_children);

        // RecyclerView yapılandırması
        childrenRecyclerView = findViewById(R.id.childrenRecyclerView);
        childList = new ArrayList<>();
        childAdapter = new ChildAdapter(childList, this::onChildSelected); // Adapter'a tıklama olayı eklendi
        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        childrenRecyclerView.setAdapter(childAdapter);

        // Firebase yapılandırması
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Firebase'den çocukları çek
        fetchChildren();
    }

    private void fetchChildren() {
        FirebaseUser parentUser = mAuth.getCurrentUser();
        if (parentUser != null) {
            String parentUID = parentUser.getUid();
            Log.d("DEBUG", "Parent UID: " + parentUID);

            DatabaseReference childrenRef = databaseReference.child(parentUID).child("children");

            childrenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("DEBUG", "DataSnapshot exists: " + dataSnapshot.exists());
                    childList.clear();

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Log.d("DEBUG", "Child Snapshot: " + childSnapshot.getValue());

                            String email = childSnapshot.child("email").getValue(String.class);
                            String role = childSnapshot.child("role").getValue(String.class);
                            String token = childSnapshot.child("token").getValue(String.class); // Token'i al

                            Log.d("DEBUG", "Child Token: " + token); // Token bilgisini logla

                            if (email != null && role != null) {
                                Log.d("DEBUG", "Child Email: " + email + ", Role: " + role);
                                Child child = new Child(email, role, token); // Token ile yeni Child nesnesi
                                childList.add(child);
                            }
                        }
                        Log.d("DEBUG", "Total Children Fetched: " + childList.size());
                        childAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewChildrenActivity.this, "Hiç çocuk eklenmemiş.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("DEBUG", "Database Error: " + databaseError.getMessage());
                    Toast.makeText(ViewChildrenActivity.this, "Veritabanı hatası: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Oturum açılmamış. Lütfen tekrar giriş yapın.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onChildSelected(Child child) {
        Log.d("DEBUG", "Seçilen Çocuk Token: " + child.getToken());
        Intent intent = new Intent(this, SendNotificationActivity.class);
        intent.putExtra("targetToken", child.getToken()); // Hedef token'ı aktar
        startActivity(intent);
    }


    private void fetchChildDetails(String childUID) {
        FirebaseUser parentUser = mAuth.getCurrentUser();
        if (parentUser != null) {
            String parentUID = parentUser.getUid(); // Mevcut ebeveyn UID'si
            DatabaseReference childRef = databaseReference.child(parentUID).child("children").child(childUID);

            childRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String role = dataSnapshot.child("role").getValue(String.class);
                        String token = dataSnapshot.child("token").getValue(String.class);

                        if (email != null && role != null && token != null) {
                            Child child = new Child(email, role, token);
                            childList.add(child);
                            childAdapter.notifyDataSetChanged(); // Listeyi güncelle
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ViewChildrenActivity.this, "Çocuk detayları çekilemedi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}