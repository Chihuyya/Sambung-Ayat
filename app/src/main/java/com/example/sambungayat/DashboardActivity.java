package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private MaterialCardView btnMulaiHafalan, cardLastSurah;
    private TextView tvUserName, tvUserPointsTop, tvUserStreak, tvJuzPercent, tvLastSurahNum, tvLastSurahName, tvLastSurahInfo, tvLastSurahPercent, btnLihatSemua;
    private ProgressBar pbJuzProgress, pbLastSurah;
    private ImageView imgAvatar;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);
        String username = sharedPref.getString("USERNAME", "Pemain");

        // Binding UI
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPointsTop = findViewById(R.id.tvUserPointsTop);
        tvUserStreak = findViewById(R.id.tvUserStreak);
        tvJuzPercent = findViewById(R.id.tvJuzPercent);
        pbJuzProgress = findViewById(R.id.pbJuzProgress);
        tvLastSurahNum = findViewById(R.id.tvLastSurahNum);
        tvLastSurahName = findViewById(R.id.tvLastSurahName);
        tvLastSurahInfo = findViewById(R.id.tvLastSurahInfo);
        tvLastSurahPercent = findViewById(R.id.tvLastSurahPercent);
        pbLastSurah = findViewById(R.id.pbLastSurah);
        btnMulaiHafalan = findViewById(R.id.btnMulaiHafalan);
        btnLihatSemua = findViewById(R.id.btnLihatSemua);
        cardLastSurah = findViewById(R.id.cardLastSurah);
        imgAvatar = findViewById(R.id.imgAvatar);

        tvUserName.setText(username + "!");
        
        // Setup Unified Navbar
        NavbarUtil.setupNavbar(this, R.id.navHome);
        
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserStats();
    }

    private void setupNavigation() {
        imgAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnMulaiHafalan.setOnClickListener(v -> startActivity(new Intent(this, PilihSurahActivity.class)));
        btnLihatSemua.setOnClickListener(v -> startActivity(new Intent(this, PilihSurahActivity.class)));
    }

    private void loadUserStats() {
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
                    int totalScore = data.optInt("total_score", 0);
                    int streak = data.optInt("streak", 0);
                    int juzProgress = data.optInt("juz_progress", 0);
                    JSONObject lastSurah = data.optJSONObject("last_surah");

                    runOnUiThread(() -> {
                        tvUserName.setText(username + "!");
                        tvUserPointsTop.setText(totalScore + " pts");
                        tvUserStreak.setText("🔥 " + streak + " Hari Streak");
                        tvJuzPercent.setText(juzProgress + "%");
                        pbJuzProgress.setProgress(juzProgress);

                        if (lastSurah != null) {
                            cardLastSurah.setVisibility(View.VISIBLE);
                            tvLastSurahNum.setText(String.valueOf(lastSurah.optInt("surah_number")));
                            tvLastSurahName.setText(lastSurah.optString("name"));
                            tvLastSurahInfo.setText(lastSurah.optInt("total_verses") + " Ayat");
                            int prog = lastSurah.optInt("progress_percent", 0);
                            pbLastSurah.setProgress(prog);
                            tvLastSurahPercent.setText(prog + "%");
                            
                            cardLastSurah.setOnClickListener(v -> {
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.putExtra("SURAH_ID", lastSurah.optInt("id"));
                                intent.putExtra("SURAH_NAME", lastSurah.optString("name"));
                                startActivity(intent);
                            });
                        } else {
                            cardLastSurah.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
}