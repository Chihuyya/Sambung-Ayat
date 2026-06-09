package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserPointsTop, tvUserStreak, tvTotalPercent, tvCompletedSurahs, tvJuz30Percent, tvLastSurahNum, tvLastSurahName, tvLastSurahInfo, tvLastSurahPercent;
    private ProgressBar pbTotalProgress, pbJuz30Progress, pbLastSurah;
    private MaterialCardView cardLastSurah, cardTotalProgress;
    private ImageView imgAvatar;
    private int userId;
    private int lastSurahId = -1;
    private String lastSurahName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);
        String username = sharedPref.getString("USERNAME", "Pemain");
        String photoUrl = sharedPref.getString("PHOTO_URL", "");

        tvUserName = findViewById(R.id.tvUserName);
        tvUserPointsTop = findViewById(R.id.tvUserPointsTop);
        tvUserStreak = findViewById(R.id.tvUserStreak);
        
        tvTotalPercent = findViewById(R.id.tvTotalPercent);
        tvCompletedSurahs = findViewById(R.id.tvCompletedSurahs);
        pbTotalProgress = findViewById(R.id.pbTotalProgress);
        cardTotalProgress = findViewById(R.id.cardTotalProgress);
        
        tvJuz30Percent = findViewById(R.id.tvJuz30Percent);
        pbJuz30Progress = findViewById(R.id.pbJuz30Progress);

        tvLastSurahNum = findViewById(R.id.tvLastSurahNum);
        tvLastSurahName = findViewById(R.id.tvLastSurahName);
        tvLastSurahInfo = findViewById(R.id.tvLastSurahInfo);
        tvLastSurahPercent = findViewById(R.id.tvLastSurahPercent);
        pbLastSurah = findViewById(R.id.pbLastSurah);
        cardLastSurah = findViewById(R.id.cardLastSurah);
        
        imgAvatar = findViewById(R.id.imgAvatar);

        tvUserName.setText(username);

        String finalPhotoUrl = photoUrl.isEmpty() 
                ? "https://ui-avatars.com/api/?name=" + username + "&background=5D4037&color=fff&size=128" 
                : photoUrl;

        if (imgAvatar != null) {
            Glide.with(this)
                    .load(finalPhotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.sample_avatar)
                    .into(imgAvatar);
        }

        NavbarUtil.setupNavbar(this, R.id.navHome);
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserStats();
    }

    private void setupNavigation() {
        if (imgAvatar != null) imgAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnMulaiHafalan).setOnClickListener(v -> startActivity(new Intent(this, PilihSurahActivity.class)));
        findViewById(R.id.btnLihatSemua).setOnClickListener(v -> startActivity(new Intent(this, PilihSurahActivity.class)));

        if (cardTotalProgress != null) {
            cardTotalProgress.setOnClickListener(v -> startActivity(new Intent(this, PilihSurahActivity.class)));
        }

        if (cardLastSurah != null) {
            cardLastSurah.setOnClickListener(v -> {
                if (lastSurahId != -1) {
                    Intent intent = new Intent(this, MainActivity.class);
                    java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();
                    ids.add(lastSurahId);
                    intent.putIntegerArrayListExtra("SELECTED_SURAH_IDS", ids);
                    intent.putExtra("SURAH_NAME", lastSurahName);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadUserStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_GET_PROFILE + "?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000); // Batas tunggu 15 detik
                conn.setReadTimeout(15000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);

                    JSONObject res = new JSONObject(sb.toString());
                    if (res.getString("status").equals("success")) {
                        JSONObject data = res.getJSONObject("data");
                        
                        int totalScore = data.optInt("total_score", 0);
                        int dailyStreak = data.optInt("daily_streak", 0);
                        int completedSurahs = data.optInt("completed_surahs_count", 0);
                        
                        int overallPercent = (int) Math.ceil((completedSurahs * 100.0) / 114.0);
                        int juz30Percent = data.optInt("juz_30_progress", 0);
                        
                        JSONObject lastSurah = data.optJSONObject("last_surah");

                        runOnUiThread(() -> {
                            tvUserPointsTop.setText(totalScore + " pts");
                            tvUserStreak.setText(dailyStreak + " Hari" + (dailyStreak >= 3 ? " 🔥" : ""));
                            
                            tvCompletedSurahs.setText(completedSurahs + " / 114 Surat Selesai");
                            tvTotalPercent.setText(overallPercent + "%");
                            pbTotalProgress.setProgress(overallPercent);
                            
                            tvJuz30Percent.setText(juz30Percent + "%");
                            pbJuz30Progress.setProgress(juz30Percent);

                            if (lastSurah != null && lastSurah.optInt("id", 0) != 0) {
                                lastSurahId = lastSurah.optInt("id");
                                lastSurahName = lastSurah.optString("name");
                                cardLastSurah.setVisibility(View.VISIBLE);
                                tvLastSurahNum.setText(String.valueOf(lastSurah.optInt("surah_number")));
                                tvLastSurahName.setText(lastSurahName);
                                tvLastSurahInfo.setText(lastSurah.optInt("total_verses") + " Ayat");
                                int prog = lastSurah.optInt("progress_percent", 0);
                                pbLastSurah.setProgress(prog);
                                tvLastSurahPercent.setText(prog + "%");
                            } else {
                                cardLastSurah.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("Dashboard", "Error: " + e.getMessage());
            }
        });
    }
}