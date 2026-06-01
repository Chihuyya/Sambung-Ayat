package com.example.sambungayat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class LeaderboardActivity extends AppCompatActivity {

    private LeaderboardAdapter adapter;
    private final List<LeaderboardUser> userList = new ArrayList<>();
    private TextView tvRank1Name, tvRank1Points, tvRank2Name, tvRank2Points, tvRank3Name, tvRank3Points;
    private TextView tvMyRank, tvMyPoints, tvMyName;
    private String username;
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        username = sharedPref.getString("USERNAME", "Anda");

        tvRank1Name = findViewById(R.id.tvRank1Name);
        tvRank1Points = findViewById(R.id.tvRank1Points);
        tvRank2Name = findViewById(R.id.tvRank2Name);
        tvRank2Points = findViewById(R.id.tvRank2Points);
        tvRank3Name = findViewById(R.id.tvRank3Name);
        tvRank3Points = findViewById(R.id.tvRank3Points);
        tvMyRank = findViewById(R.id.tvMyRank);
        tvMyPoints = findViewById(R.id.tvMyPoints);
        tvMyName = findViewById(R.id.tvMyName);

        if (tvMyName != null) tvMyName.setText(username);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLeaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(userList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        NavbarUtil.setupNavbar(this, R.id.navRanks);

        fetchLeaderboardData();
    }

    private void fetchLeaderboardData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_LEADERBOARD);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONArray jsonArray = new JSONArray(sb.toString());
                List<LeaderboardUser> tempTop3 = new ArrayList<>();
                List<LeaderboardUser> tempRemaining = new ArrayList<>();

                int myRankFound = -1;
                int myScoreFound = 0;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String pName = obj.optString("nama_pemain", "Unknown");
                    int pScore = obj.optInt("skor", 0);
                    
                    LeaderboardUser user = new LeaderboardUser(i + 1, pName, pScore, "Streak: " + obj.optInt("streak", 0));
                    
                    if (pName.equalsIgnoreCase(username)) {
                        myRankFound = i + 1;
                        myScoreFound = pScore;
                    }

                    if (i < 3) tempTop3.add(user);
                    else tempRemaining.add(user);
                }

                int finalRank = myRankFound;
                int finalScore = myScoreFound;

                runOnUiThread(() -> {
                    updatePodium(tempTop3);
                    userList.clear();
                    userList.addAll(tempRemaining);
                    adapter.notifyDataSetChanged();

                    if (finalRank != -1) {
                        tvMyRank.setText(String.valueOf(finalRank));
                        tvMyPoints.setText(String.valueOf(finalScore));
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Gagal memuat peringkat terbaru.")
                        .setConfirmText("OK")
                        .setConfirmButtonBackgroundColor(COLOR_PRIMARY) // Perbaikan warna tombol
                        .show());
            }
        });
    }

    private void updatePodium(List<LeaderboardUser> top3) {
        if (!top3.isEmpty()) {
            tvRank1Name.setText(top3.get(0).getName());
            tvRank1Points.setText(top3.get(0).getScore() + " pts");
        }
        if (top3.size() >= 2) {
            tvRank2Name.setText(top3.get(1).getName());
            tvRank2Points.setText(top3.get(1).getScore() + " pts");
        }
        if (top3.size() >= 3) {
            tvRank3Name.setText(top3.get(2).getName());
            tvRank3Points.setText(top3.get(2).getScore() + " pts");
        }
    }
}
