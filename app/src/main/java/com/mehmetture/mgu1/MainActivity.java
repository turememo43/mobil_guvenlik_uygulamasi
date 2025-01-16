package com.mehmetture.mgu1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private String currentUserRole = null; // Kullanıcının rolünü saklamak için değişken


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Firebase Auth ve Database Başlatma
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // View'ları Tanımlama
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);



        // Kullanıcı Zaten Giriş Yapmış mı Kontrol Et
        checkUserLoggedIn();

        // Kayıt Butonu İşlevi
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Giriş Butonu İşlevi
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void checkUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedRole = sharedPreferences.getString("role", null);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && savedRole != null) {
            // Rol bilgisi SharedPreferences'te varsa direkt yönlendir
            sendStoredTokenToServer(savedRole); // Token gönder
            if (savedRole.equals("Parent")) {
                startActivity(new Intent(MainActivity.this, ParentDashboardActivity.class));
            } else if (savedRole.equals("Controlled")) {
                startActivity(new Intent(MainActivity.this, ControlledDashboardActivity.class));
            }
            finish();
        } else if (user != null) {
            // Firebase'den rol kontrolü yap ve yönlendir
            checkUserRole(user.getUid(), new RoleCallback() {
                @Override
                public void onRoleReceived(String role) {
                    if (role != null) {
                        saveRoleToPreferences(role);
                        sendStoredTokenToServer(role); // Token gönder
                        if (role.equals("Parent")) {
                            startActivity(new Intent(MainActivity.this, ParentDashboardActivity.class));
                        } else if (role.equals("Controlled")) {
                            startActivity(new Intent(MainActivity.this, ControlledDashboardActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Rol bilgisi alınamadı, işlem yapılamadı.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void checkUserRole(String userId, RoleCallback callback) {
        DatabaseReference usersRef = databaseReference;

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                boolean roleFound = false;

                for (DataSnapshot parentSnapshot : task.getResult().getChildren()) {
                    if (parentSnapshot.child("children").hasChild(userId)) {
                        callback.onRoleReceived("Controlled");
                        roleFound = true;
                        return;
                    }
                }

                if (!roleFound) {
                    callback.onRoleReceived("Parent");
                }
            } else {
                Log.e("CheckUserRole", "Firebase rol kontrolünde hata oluştu: " + task.getException().getMessage());
                callback.onRoleReceived(null);
            }
        });
    }


    private void assignDefaultRole(String userId) {
        // Varsayılan olarak "Parent" rolünü ata ve SharedPreferences'a kaydet
        String defaultRole = "Parent";
        databaseReference.child(userId).child("role").setValue(defaultRole)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveRoleToPreferences(defaultRole); // Rolü SharedPreferences'a kaydet
                        startActivity(new Intent(MainActivity.this, ParentDashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Rol atanırken hata oluştu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveRoleToPreferences(String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("role", role);
        editor.apply();
    }


    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String defaultRole = "Parent"; // Varsayılan rol
                        saveUserToDatabase(user); // Kullanıcıyı Firebase'e kaydet
                        sendStoredTokenToServer(defaultRole); // Token'i role göre gönder
                        Toast.makeText(MainActivity.this, "Kayıt başarılı: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        // Doğrudan ebeveyn paneline yönlendirme
                        startActivity(new Intent(MainActivity.this, ParentDashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Kayıt başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid(), role -> {
                                if (role != null) {
                                    currentUserRole = role;
                                    saveRoleToPreferences(role);
                                    sendStoredTokenToServer(role); // Callback içinde çağırıldı

                                    if ("Parent".equals(role)) {
                                        startActivity(new Intent(MainActivity.this, ParentDashboardActivity.class));
                                    } else if ("Controlled".equals(role)) {
                                        startActivity(new Intent(MainActivity.this, ControlledDashboardActivity.class));
                                    }
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "Rol bilgisi alınamadı!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Giriş başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void saveUserToDatabase(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            String email = user.getEmail();

            // Varsayılan rol
            String defaultRole = "Parent";

            databaseReference.child(userId).child("email").setValue(email);
            databaseReference.child(userId).child("role").setValue(defaultRole)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveRoleToPreferences(defaultRole); // SharedPreferences'a kaydet
                            Toast.makeText(MainActivity.this, "Kullanıcı bilgileri kaydedildi.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Kullanıcı bilgileri kaydedilemedi!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void sendStoredTokenToServer(String role) {
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String tempToken = preferences.getString("tempToken", null);

        if (tempToken != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                if ("Parent".equals(role)) {
                    userRef.child("token").setValue(tempToken).addOnCompleteListener(tokenTask -> {
                        if (tokenTask.isSuccessful()) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove("tempToken");
                            editor.apply();
                            Log.d("Token", "Parent token başarıyla kaydedildi.");
                        } else {
                            Log.e("Token", "Parent token kaydedilemedi.");
                        }
                    });
                } else if ("Controlled".equals(role)) {
                    // Controlled için token kaydetme işlemi
                    DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference("Users");
                    parentRef.get().addOnCompleteListener(parentTask -> {
                        if (parentTask.isSuccessful() && parentTask.getResult().exists()) {
                            for (DataSnapshot parentSnapshot : parentTask.getResult().getChildren()) {
                                if (parentSnapshot.child("children").hasChild(userId)) {
                                    String parentUID = parentSnapshot.getKey();
                                    DatabaseReference tokenRef = parentRef.child(parentUID).child("children").child(userId).child("token");
                                    tokenRef.setValue(tempToken).addOnCompleteListener(childTokenTask -> {
                                        if (childTokenTask.isSuccessful()) {
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.remove("tempToken");
                                            editor.apply();
                                            Log.d("Token", "Controlled token başarıyla kaydedildi.");
                                        } else {
                                            Log.e("Token", "Controlled token kaydedilemedi.");
                                        }
                                    });
                                    break;
                                }
                            }
                        } else {
                            Log.e("Token", "Parent UID bulunamadı.");
                        }
                    });
                }
            } else {
                Log.e("Token", "Kullanıcı bilgisi alınamadı (currentUser null).");
            }
        } else {
            Log.e("Token", "Geçici token bulunamadı.");
        }
    }



    public interface RoleCallback {
        void onRoleReceived(String role);
    }




}

