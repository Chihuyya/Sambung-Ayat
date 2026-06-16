package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.google.android.material.button.MaterialButton;
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

    private TextInputEditText etFullName, etEmail, etPhone;
    private int userId;
    // Warna Coklat Logo Konsisten
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        MaterialButton btnSave = findViewById(R.id.btnSaveChanges);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfileChanges());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_GET_PROFILE + "?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject res = new JSONObject(sb.toString());
                if (res.getString("status").equals("success")) {
                    JSONObject data = res.getJSONObject("data");
                    runOnUiThread(() -> {
                        etFullName.setText(data.optString("username"));
                        etEmail.setText(data.optString("email"));
                        etPhone.setText(data.optString("phone")); // Menampilkan nomor HP jika ada
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void saveProfileChanges() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Nama dan Email harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(COLOR_PRIMARY);
        pDialog.setTitleText("Menyimpan...");
        pDialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_UPDATE_PROFILE);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Menambahkan mode=edit_profile agar sesuai dengan logika PHP Anda
                String postData = "user_id=" + userId +
                        "&mode=edit_profile" +
                        "&username=" + URLEncoder.encode(name, "UTF-8") +
                        "&email=" + URLEncoder.encode(email, "UTF-8") +
                        "&phone=" + URLEncoder.encode(phone, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush(); os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject res = new JSONObject(sb.toString());
                runOnUiThread(() -> {
                    pDialog.dismissWithAnimation();
                    if (res.optString("status").equals("success")) {
                        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Berhasil")
                                .setContentText("Profil Anda telah diperbarui.")
                                .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                                .setConfirmClickListener(sDialog -> {
                                    sDialog.dismissWithAnimation();
                                    finish();
                                }).show();
                    } else {
                        Toast.makeText(this, res.optString("message", "Gagal memperbarui profil"), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> { pDialog.dismissWithAnimation(); Toast.makeText(this, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show(); });
            }
        });
    }
}
