package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout menuEditProfile, menuAudioSettings, menuNotificationSettings, menuHelpCenter;
    private MaterialButton btnLogout;
    private TextView tvProfileName, tvStatSurah, tvStatPerfect, tvStatStreak, tvProfileLevel;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Binding UI
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileLevel = findViewById(R.id.tvProfileLevel);
        tvStatSurah = findViewById(R.id.tvStatSurah);
        tvStatPerfect = findViewById(R.id.tvStatPerfect);
        tvStatStreak = findViewById(R.id.tvStatStreak);
        
        menuEditProfile = findViewById(R.id.menuEditProfile);
        menuAudioSettings = findViewById(R.id.menuAudioSettings);
        menuNotificationSettings = findViewById(R.id.menuNotificationSettings);
        menuHelpCenter = findViewById(R.id.menuHelpCenter);
        btnLogout = findViewById(R.id.btnLogout);

        // Ambil data sesi
        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);
        String username = sharedPref.getString("USERNAME", "Pemain");
        
        if (tvProfileName != null) tvProfileName.setText(username);

        // Setup Custom Navbar
        NavbarUtil.setupNavbar(this, R.id.navProfile);

        btnLogout.setOnClickListener(v -> {
            LogoutBottomSheet logoutSheet = new LogoutBottomSheet();
            logoutSheet.show(getSupportFragmentManager(), "LogoutBottomSheet");
        });

        menuEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        
        menuAudioSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, AudioSettingsActivity.class));
        });

        menuNotificationSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationSettingsActivity.class));
        });

        menuHelpCenter.setOnClickListener(v -> {
            Toast.makeText(this, "Pusat Bantuan akan segera tersedia", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData(userId);
    }

    private void loadProfileData(int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_GET_PROFILE + "?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject res = new JSONObject(sb.toString());
                if (res.getString("status").equals("success")) {
                    JSONObject data = res.getJSONObject("data");
                    String username = data.optString("username", "Pemain");
                    String level = data.optString("level", "Penghafal Pemula");
                    int totalSurah = data.optInt("total_surah", 0);
                    int totalPerfect = data.optInt("total_perfect", 0);
                    int streak = data.optInt("streak", 0);

                    runOnUiThread(() -> {
                        if (tvProfileName != null) tvProfileName.setText(username);
                        if (tvProfileLevel != null) tvProfileLevel.setText(level);
                        if (tvStatSurah != null) tvStatSurah.setText(String.valueOf(totalSurah));
                        if (tvStatPerfect != null) tvStatPerfect.setText(String.valueOf(totalPerfect));
                        if (tvStatStreak != null) tvStatStreak.setText(streak + " Hari");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}