package com.example.sambungayat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardUser> userList = new ArrayList<>();

    // Podium views
    private TextView tvRank1Name, tvRank1Points;
    private TextView tvRank2Name, tvRank2Points;
    private TextView tvRank3Name, tvRank3Points;

    // Tab views
    private TextView tabMingguan, tabBulanan, tabSemua;

    // Pinned User views
    private TextView tvMyRank, tvMyName, tvMyPoints, tvMyStatus;

    private int userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Session
        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);
        username = sharedPref.getString("USERNAME", "Anda");

        // Bind Podium
        tvRank1Name = findViewById(R.id.tvRank1Name);
        tvRank1Points = findViewById(R.id.tvRank1Points);
        tvRank2Name = findViewById(R.id.tvRank2Name);
        tvRank2Points = findViewById(R.id.tvRank2Points);
        tvRank3Name = findViewById(R.id.tvRank3Name);
        tvRank3Points = findViewById(R.id.tvRank3Points);

        // Bind Tabs
        tabMingguan = findViewById(R.id.tabMingguan);
        tabBulanan = findViewById(R.id.tabBulanan);
        tabSemua = findViewById(R.id.tabSemua);

        // Bind Pinned User
        tvMyRank = findViewById(R.id.tvMyRank);
        tvMyName = findViewById(R.id.tvMyName);
        tvMyPoints = findViewById(R.id.tvMyPoints);
        tvMyStatus = findViewById(R.id.tvMyStatus);

        tvMyName.setText(username);

        // Bind RecyclerView
        recyclerView = findViewById(R.id.recyclerViewLeaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(userList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Tab Listeners
        tabMingguan.setOnClickListener(v -> selectTab(tabMingguan));
        tabBulanan.setOnClickListener(v -> selectTab(tabBulanan));
        tabSemua.setOnClickListener(v -> selectTab(tabSemua));

        // Initial Load
        selectTab(tabSemua);
    }

    private void selectTab(TextView selectedTab) {
        // Reset styles
        tabMingguan.setBackground(null);
        tabMingguan.setTextColor(Color.parseColor("#3F4944"));
        tabBulanan.setBackground(null);
        tabBulanan.setTextColor(Color.parseColor("#3F4944"));
        tabSemua.setBackground(null);
        tabSemua.setTextColor(Color.parseColor("#3F4944"));

        // Set selected style
        selectedTab.setBackgroundResource(R.drawable.bg_tab_active);
        selectedTab.setTextColor(Color.WHITE);

        fetchLeaderboardData();
    }

    private void fetchLeaderboardData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_LEADERBOARD);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

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
                    String pName = obj.getString("nama_pemain");
                    int pScore = obj.getInt("skor");
                    
                    LeaderboardUser user = new LeaderboardUser(
                            i + 1,
                            pName,
                            pScore,
                            "Waktu: " + obj.optString("waktu_main", "-")
                    );
                    
                    // Cek jika ini adalah user yang sedang login (simulasi sederhana berdasarkan nama)
                    if (pName.equalsIgnoreCase(username)) {
                        myRankFound = i + 1;
                        myScoreFound = pScore;
                    }

                    if (i < 3) {
                        tempTop3.add(user);
                    } else {
                        tempRemaining.add(user);
                    }
                }

                int finalMyRankFound = myRankFound;
                int finalMyScoreFound = myScoreFound;

                runOnUiThread(() -> {
                    // Update Podium
                    updatePodium(tempTop3);

                    // Update List
                    userList.clear();
                    userList.addAll(tempRemaining);
                    adapter.notifyDataSetChanged();

                    // Update Pinned User
                    if (finalMyRankFound != -1) {
                        tvMyRank.setText(String.valueOf(finalMyRankFound));
                        tvMyPoints.setText(String.valueOf(finalMyScoreFound));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Gagal memuat leaderboard", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updatePodium(List<LeaderboardUser> top3) {
        // Reset
        tvRank1Name.setText("-"); tvRank1Points.setText("0 pts");
        tvRank2Name.setText("-"); tvRank2Points.setText("0 pts");
        tvRank3Name.setText("-"); tvRank3Points.setText("0 pts");

        if (top3.size() >= 1) {
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