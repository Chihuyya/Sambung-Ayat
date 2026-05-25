package com.example.sambungayat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ShapeableImageView imgProfilePicture;
    private MaterialCardView btnEditPhoto;
    private TextInputEditText etUsername, etEmail;
    private MaterialButton btnSaveChanges;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Ambil data sesi
        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);
        String currentUsername = sharedPref.getString("USERNAME", "");
        String currentEmail = sharedPref.getString("EMAIL", ""); // Pastikan disimpan saat login

        // Inisialisasi UI
        btnBack = findViewById(R.id.btnBack);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        etUsername = findViewById(R.id.etFullName); // ID tetap etFullName agar tidak error XML
        etEmail = findViewById(R.id.etEmail);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        // Set data saat ini
        etUsername.setText(currentUsername);
        etEmail.setText(currentEmail);

        btnBack.setOnClickListener(v -> finish());
        btnEditPhoto.setOnClickListener(v -> Toast.makeText(this, "Fitur ubah foto segera hadir", Toast.LENGTH_SHORT).show());
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void saveProfileChanges() {
        String newUsername = etUsername.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newEmail)) {
            Toast.makeText(this, "Harap isi semua bidang", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Endpoint baru: update_profile.php
                URL url = new URL(Config.BASE_URL + "update_profile.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String postData = "user_id=" + userId +
                        "&username=" + URLEncoder.encode(newUsername, "UTF-8") +
                        "&email=" + URLEncoder.encode(newEmail, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject res = new JSONObject(sb.toString());
                runOnUiThread(() -> {
                    try {
                        if (res.getString("status").equals("success")) {
                            // Update SharedPreferences
                            SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("USERNAME", newUsername);
                            editor.putString("EMAIL", newEmail);
                            editor.apply();

                            Toast.makeText(this, "Profil diperbarui!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, res.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show());
            }
        });
    }
}