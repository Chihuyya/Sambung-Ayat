package com.example.sambungayat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
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
    private final List<Badge> badgeList = new ArrayList<>();
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);

        progressStreak = findViewById(R.id.progressStreak);
        rvBadges = findViewById(R.id.rvBadges);
        tvStreakCount = findViewById(R.id.tvStreakCount);
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (rvBadges != null) {
            rvBadges.setLayoutManager(new GridLayoutManager(this, 2));
            adapter = new BadgeAdapter(badgeList);
            rvBadges.setAdapter(adapter);
        }

        NavbarUtil.setupNavbar(this, R.id.navRanks);
        fetchAchievementsData();
    }

    private void fetchAchievementsData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_GET_ACHIEVEMENTS + "?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject res = new JSONObject(sb.toString());
                if (res.getString("status").equals("success")) {
                    JSONObject data = res.getJSONObject("data");
                    int currentStreak = data.optInt("current_streak", 0);
                    int targetStreak = data.optInt("target_streak", 10);
                    JSONArray badgesArr = data.optJSONArray("badges");

                    badgeList.clear();
                    if (badgesArr != null) {
                        for (int i = 0; i < badgesArr.length(); i++) {
                            JSONObject b = badgesArr.getJSONObject(i);
                            
                            // Logika mengubah String Nama Ikon dari PHP menjadi Drawable Resource ID
                            String iconName = b.optString("icon_name", "ic_star");
                            int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                            if (resId == 0) resId = R.drawable.ic_star; // Fallback jika tidak ditemukan

                            badgeList.add(new Badge(
                                    b.getString("title"),
                                    b.getString("description"),
                                    resId,
                                    b.getInt("is_unlocked") == 1
                            ));
                        }
                    }

                    runOnUiThread(() -> {
                        if (progressStreak != null) {
                            int streakPercentage = (int) (((float) currentStreak / Math.max(1, targetStreak)) * 100);
                            progressStreak.setProgress(Math.min(streakPercentage, 100));
                        }
                        if (tvStreakCount != null) tvStreakCount.setText(String.valueOf(currentStreak));
                        if (adapter != null) adapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                Log.e("ACHIEVEMENTS", "Error: " + e.getMessage());
            }
        });
    }
}