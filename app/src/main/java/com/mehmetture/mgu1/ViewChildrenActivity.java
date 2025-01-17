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

public class ViewChildrenActivity extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private ChildAdapter childAdapter;
    private List<Child> childList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_children);

        // RecyclerView yapılandırması
        childrenRecyclerView = findViewById(R.id.childrenRecyclerView);
        childList = new ArrayList<>();
        childAdapter = new ChildAdapter(childList, new ChildAdapter.OnChildActionListener() {
            @Override
            public void onSendNotification(Child child) {
                // Bildirim gönderme kodu
                NotificationHelper.sendNotification(ViewChildrenActivity.this,
                        "Bildirim Gönderildi",
                        child.getEmail() + " adresine bir bildirim gönderildi.");
            }

            @Override
            public void onViewLocation(Child child) {
                // Konum görme kodu
                Intent intent = new Intent(ViewChildrenActivity.this, ViewLocationActivity.class);
                intent.putExtra("childUID", child.getUid());
                startActivity(intent);
            }
        });
        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        childrenRecyclerView.setAdapter(childAdapter);

        // Firebase yapılandırması
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Firebase'den çocukları çek
        fetchChildren();
    }

    private void fetchChildren() {
        FirebaseUser parentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (parentUser != null) {
            String parentUID = parentUser.getUid();
            DatabaseReference childrenRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(parentUID)
                    .child("children");

            childrenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    childList.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String email = childSnapshot.child("email").getValue(String.class);
                            String role = childSnapshot.child("role").getValue(String.class);
                            String uid = childSnapshot.getKey(); // UID çekiliyor

                            if (email != null && role != null && uid != null) {
                                childList.add(new Child(email, role, uid));
                            }
                        }
                        childAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewChildrenActivity.this, "Hiç çocuk eklenmemiş.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ViewChildrenActivity.this, "Veritabanı hatası: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Oturum açılmamış. Lütfen tekrar giriş yapın.", Toast.LENGTH_SHORT).show();
        }
    }

}
