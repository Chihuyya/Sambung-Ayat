package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvStatSurah, tvStatPerfect, tvStatStreak, tvProfileLevel;
    private ImageView ivProfileAvatar;
    private int userId;
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileLevel = findViewById(R.id.tvProfileLevel);
        tvStatSurah = findViewById(R.id.tvStatSurah);
        tvStatPerfect = findViewById(R.id.tvStatPerfect);
        tvStatStreak = findViewById(R.id.tvStatStreak);

        LinearLayout menuEditProfile = findViewById(R.id.menuEditProfile);
        LinearLayout menuAudioSettings = findViewById(R.id.menuAudioSettings);
        LinearLayout menuNotificationSettings = findViewById(R.id.menuNotificationSettings);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);
        String username = sharedPref.getString("USERNAME", "Pemain");
        String email = sharedPref.getString("EMAIL", "");
        String photoUrl = sharedPref.getString("PHOTO_URL", "");

        if (tvProfileName != null) tvProfileName.setText(username);
        if (tvProfileEmail != null) tvProfileEmail.setText(email);

        if (ivProfileAvatar != null && !photoUrl.isEmpty()) {
            Glide.with(this).load(photoUrl).circleCrop().into(ivProfileAvatar);
        }

        NavbarUtil.setupNavbar(this, R.id.navProfile);

        if (menuEditProfile != null) {
            menuEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        }

        if (menuAudioSettings != null) {
            menuAudioSettings.setOnClickListener(v -> startActivity(new Intent(this, AudioSettingsActivity.class)));
        }

        if (menuNotificationSettings != null) {
            menuNotificationSettings.setOnClickListener(v ->
                    startActivity(new Intent(this, NotificationSettingsActivity.class))
            );
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                LogoutBottomSheet logoutSheet = new LogoutBottomSheet();
                logoutSheet.show(getSupportFragmentManager(), "LogoutBottomSheet");
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData(userId);
    }

    private void loadProfileData(int userId) {
        if (userId == 0) return;
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
                        try {
                            if (tvProfileName != null) tvProfileName.setText(data.optString("username", "Pemain"));
                            if (tvProfileLevel != null) tvProfileLevel.setText(data.optString("level", "Penghafal Pemula"));
                            if (tvStatSurah != null) tvStatSurah.setText(String.valueOf(data.optInt("total_surah", 0)));
                            if (tvStatPerfect != null) tvStatPerfect.setText(String.valueOf(data.optInt("total_perfect", 0)));
                            if (tvStatStreak != null) tvStatStreak.setText(data.optInt("streak", 0) + " Hari");

                            String serverPhoto = data.optString("photo_url", "");
                            if (!serverPhoto.isEmpty() && ivProfileAvatar != null) {
                                Glide.with(ProfileActivity.this).load(serverPhoto).circleCrop().into(ivProfileAvatar);
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    });
                }
            } catch (Exception e) {
                Log.e("Profile", "Error loading profile: " + e.getMessage());
            }
        });
    }
}