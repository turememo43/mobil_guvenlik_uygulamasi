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

import org.json.JSONException;
import org.json.JSONObject;

public class SendNotificationActivity extends AppCompatActivity {

    private EditText titleEditText, messageEditText;
    private Button sendButton;
    private String targetToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        // Intent ile gelen hedef token
        targetToken = getIntent().getStringExtra("targetToken");
        Log.d("DEBUG", "Target Token: " + targetToken);
        if (targetToken == null || targetToken.isEmpty()) {
            Toast.makeText(this, "Hedef token bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // View'ları bağlama
        titleEditText = findViewById(R.id.titleEditText);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Gönder butonu tıklama
        sendButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String message = messageEditText.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(SendNotificationActivity.this, "Başlık ve mesaj boş bırakılamaz!", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("DEBUG", "Target Token: " + targetToken);

            // Bildirim gönderme
            sendNotification(title, message, targetToken);
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
                        Log.d("Notification", "Bildirim başarıyla gönderildi: " + response.toString());
                        Toast.makeText(SendNotificationActivity.this, "Bildirim başarıyla gönderildi!", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Log.e("Notification", "Bildirim gönderilirken hata oluştu: " + error.getMessage());
                        Toast.makeText(SendNotificationActivity.this, "Hata: Bildirim gönderilemedi!", Toast.LENGTH_SHORT).show();
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
