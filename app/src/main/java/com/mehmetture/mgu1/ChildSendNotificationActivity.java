package com.mehmetture.mgu1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class ChildSendNotificationActivity extends AppCompatActivity {

    private EditText titleEditText, messageEditText;
    private Button sendButton;
    private String parentToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_send_notification);

        // Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturum açmamış!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String childUID = currentUser.getUid();
        getParentToken(childUID);

        // View'ları bağlama
        titleEditText = findViewById(R.id.titleEditText);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Gönder butonu tıklama
        sendButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String message = messageEditText.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(ChildSendNotificationActivity.this, "Başlık ve mesaj boş bırakılamaz!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (parentToken == null || parentToken.isEmpty()) {
                Toast.makeText(ChildSendNotificationActivity.this, "Ebeveyn tokeni bulunamadı!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bildirim gönderme
            sendNotification(title, message, parentToken);
        });
    }

    private void getParentToken(String childUID) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot parentSnapshot : task.getResult().getChildren()) {
                    if (parentSnapshot.child("children").hasChild(childUID)) {
                        parentToken = parentSnapshot.child("token").getValue(String.class);
                        Log.d("Child Notification", "Parent Token: " + parentToken);
                        return;
                    }
                }
                Log.e("Child Notification", "Ebeveyn tokeni bulunamadı!");
                Toast.makeText(this, "Ebeveyn tokeni alınamadı!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Child Notification", "Firebase'den ebeveyn tokeni alınamadı: " + task.getException());
                Toast.makeText(this, "Firebase hatası!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String title, String message, String targetToken) {
        String url = "http://10.0.2.2:3000/send-notification"; // Backend URL'si (emülatör için)

        JSONObject notificationData = new JSONObject();
        try {
            notificationData.put("title", title);
            notificationData.put("body", message);
            notificationData.put("targetToken", targetToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    notificationData,
                    response -> {
                        Log.d("Child Notification", "Bildirim başarıyla gönderildi: " + response.toString());
                        Toast.makeText(ChildSendNotificationActivity.this, "Bildirim başarıyla gönderildi!", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Log.e("Child Notification", "Bildirim gönderilirken hata oluştu: " + error.getMessage());
                        Toast.makeText(ChildSendNotificationActivity.this, "Hata: Bildirim gönderilemedi!", Toast.LENGTH_SHORT).show();
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Hata: Bildirim verileri oluşturulamadı!", Toast.LENGTH_SHORT).show();
        }
    }
}
