package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AchievementsActivity extends AppCompatActivity {

    private CircularProgressIndicator progressStreak;
    private RecyclerView rvBadges;
    private TextView tvStreakCount;
    private int userId;
    private BadgeAdapter adapter;
    private List<Badge> badgeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        // Ambil User ID dari sesi SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);

        // Binding UI
        progressStreak = findViewById(R.id.progressStreak);
        rvBadges = findViewById(R.id.rvBadges);
        tvStreakCount = findViewById(R.id.tvStreakCount);

        // Setup RecyclerView untuk Lencana (2 kolom)
        rvBadges.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BadgeAdapter(badgeList);
        rvBadges.setAdapter(adapter);

        // Setup Custom Navbar (Highlight Ranks/Achievements as part of stats)
        NavbarUtil.setupNavbar(this, R.id.navRanks);

        // Load data pencapaian dari server secara real-time
        fetchAchievementsData();
    }

    private void fetchAchievementsData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_GET_ACHIEVEMENTS + "?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject res = new JSONObject(sb.toString());
                if (res.getString("status").equals("success")) {
                    JSONObject data = res.getJSONObject("data");
                    int currentStreak = data.optInt("current_streak", 0);
                    int targetStreak = data.optInt("target_streak", 7);
                    JSONArray badgesArr = data.optJSONArray("badges");

                    badgeList.clear();
                    if (badgesArr != null) {
                        for (int i = 0; i < badgesArr.length(); i++) {
                            JSONObject b = badgesArr.getJSONObject(i);
                            badgeList.add(new Badge(
                                    b.getString("title"),
                                    b.getString("description"),
                                    getResources().getIdentifier(b.getString("icon_name"), "drawable", getPackageName()),
                                    b.getInt("is_unlocked") == 1
                            ));
                        }
                    }

                    runOnUiThread(() -> {
                        int streakPercentage = (int) (((float) currentStreak / Math.max(1, targetStreak)) * 100);
                        progressStreak.setProgress(streakPercentage);
                        if (tvStreakCount != null) {
                            tvStreakCount.setText(String.valueOf(currentStreak));
                        }
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Gagal memuat data pencapaian", Toast.LENGTH_SHORT).show());
            }
        });
    }
}