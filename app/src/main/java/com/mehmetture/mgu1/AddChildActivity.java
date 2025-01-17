package com.mehmetture.mgu1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.app.AlertDialog;
import android.text.InputType;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class AddChildActivity extends AppCompatActivity {

    private EditText childEmailEditText, childPasswordEditText;
    private Button addChildButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String parentUID, currentParentEmail, currentParentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        // Firebase Başlatma
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Ebeveyn bilgilerini al
        FirebaseUser parentUser = mAuth.getCurrentUser();
        if (parentUser != null) {
            parentUID = parentUser.getUid();
            currentParentEmail = parentUser.getEmail();
            Log.d("DEBUG", "Ebeveyn UID'si: " + parentUID);
        }

        // View'ları Tanımlama
        childEmailEditText = findViewById(R.id.childEmailEditText);
        childPasswordEditText = findViewById(R.id.childPasswordEditText);
        addChildButton = findViewById(R.id.addChildButton);

        // Çocuk ekle butonu
        addChildButton.setOnClickListener(v -> showPasswordDialog());
    }

    // Şifre girişini gösteren AlertDialog
    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Şifre Girişi");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Onayla", (dialog, which) -> {
            currentParentPassword = input.getText().toString().trim();
            if (!currentParentPassword.isEmpty()) {
                Log.d("DEBUG", "Ebeveyn Şifre: " + currentParentPassword);
                addChild();
            } else {
                Toast.makeText(this, "Şifre boş bırakılamaz!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Çocuk hesabını oluştur
    private void addChild() {
        String childEmail = childEmailEditText.getText().toString().trim();
        String childPassword = childPasswordEditText.getText().toString().trim();

        if (childEmail.isEmpty() || childPassword.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(childEmail, childPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser childUser = task.getResult().getUser();
                        if (childUser != null) {
                            Log.d("DEBUG", "Çocuk kullanıcısı oluşturuldu: " + childUser.getUid());
                            saveChildToParent(childUser.getUid(), childEmail);
                        }
                    } else {
                        Toast.makeText(this, "Çocuk eklenirken hata oluştu!", Toast.LENGTH_SHORT).show();
                        Log.e("DEBUG", "Çocuk ekleme hatası: " + task.getException().getMessage());
                    }
                });
    }

    // Çocuğu ebeveynin altına kaydet
    private void saveChildToParent(String childUID, String childEmail) {
        DatabaseReference parentRef = databaseReference.child(parentUID).child("children").child(childUID);
        parentRef.child("email").setValue(childEmail);
        parentRef.child("role").setValue("Controlled").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("DEBUG", "Çocuk ebeveyn altına eklendi.");

                // Mevcut tokeni sil ve yeni bir token oluştur
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                String childToken = tokenTask.getResult();
                                parentRef.child("token").setValue(childToken).addOnCompleteListener(tokenSaveTask -> {
                                    if (tokenSaveTask.isSuccessful()) {
                                        Log.d("DEBUG", "Çocuk için yeni token başarıyla kaydedildi.");
                                    } else {
                                        Log.e("DEBUG", "Çocuk token kaydedilemedi: " + tokenSaveTask.getException());
                                    }
                                });
                            } else {
                                Log.e("DEBUG", "Çocuk için token oluşturulamadı: " + tokenTask.getException());
                            }
                        });
                    } else {
                        Log.e("DEBUG", "Token silinemedi: " + deleteTask.getException());
                    }
                });

                incrementChildCount();
                restoreParentSession();
            } else {
                Log.e("DEBUG", "Çocuk ebeveyn altına eklenemedi: " + task.getException());
            }
        });
    }


    // Çocuk sayısını artır
    private void incrementChildCount() {
        DatabaseReference childCountRef = databaseReference.child(parentUID).child("childCount");
        childCountRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long currentCount = task.getResult().exists() ? task.getResult().getValue(Long.class) : 0;
                childCountRef.setValue(currentCount + 1);
            }
        });
    }

    // Ebeveyn oturumunu geri yükle
    private void restoreParentSession() {
        if (currentParentEmail != null && currentParentPassword != null) {
            mAuth.signOut();
            mAuth.signInWithEmailAndPassword(currentParentEmail, currentParentPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG", "Ebeveyn oturumu başarıyla geri yüklendi.");
                            Intent intent = new Intent(AddChildActivity.this, ViewChildrenActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("DEBUG", "Ebeveyn oturumu geri yüklenemedi: " + task.getException().getMessage());
                            Toast.makeText(this, "Oturum geri yüklenemedi. Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("DEBUG", "Ebeveyn bilgileri eksik. Email veya Şifre null.");
        }
    }
}
