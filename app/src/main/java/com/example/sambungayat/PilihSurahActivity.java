package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import cn.pedant.SweetAlert.SweetAlertDialog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PilihSurahActivity extends AppCompatActivity {

    private RecyclerView rvSurah;
    private SurahAdapter surahAdapter;
    private TabLayout tabLayoutJuz;
    private TextView tvQuestionCount;
    private MaterialButton btnStartMuraajah;
    private TextView btnSelectAll;
    private int userId;
    private int currentQuestionCount = 5;
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_surah);

        tabLayoutJuz = findViewById(R.id.tabLayoutJuz);
        rvSurah = findViewById(R.id.rvSurah);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        MaterialButton btnPlus = findViewById(R.id.btnPlus);
        MaterialButton btnMinus = findViewById(R.id.btnMinus);
        btnStartMuraajah = findViewById(R.id.btnStartMuraajah);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        ImageView btnBack = findViewById(R.id.btnBack);

        tabLayoutJuz.setSelectedTabIndicatorColor(COLOR_PRIMARY);
        tabLayoutJuz.setTabTextColors(Color.GRAY, COLOR_PRIMARY);

        if (tabLayoutJuz.getTabCount() == 0) {
            for (int i = 1; i <= 30; i++) {
                tabLayoutJuz.addTab(tabLayoutJuz.newTab().setText("Juz " + i));
            }
        }

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);

        rvSurah.setLayoutManager(new LinearLayoutManager(this));
        surahAdapter = new SurahAdapter(new ArrayList<>());
        rvSurah.setAdapter(surahAdapter);

        NavbarUtil.setupNavbar(this, R.id.navPlay);

        int initialJuz = getIntent().getIntExtra("JUZ", 1);
        
        if (initialJuz > 0 && initialJuz <= 30) {
            TabLayout.Tab tab = tabLayoutJuz.getTabAt(initialJuz - 1);
            if (tab != null) {
                tab.select();
            }
        }
        
        loadSurahByJuz(initialJuz);

        btnBack.setOnClickListener(v -> finish());

        tabLayoutJuz.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadSurahByJuz(tab.getPosition() + 1);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        tvQuestionCount.setText(String.valueOf(currentQuestionCount));
        btnPlus.setOnClickListener(v -> { if (currentQuestionCount < 10) { currentQuestionCount++; tvQuestionCount.setText(String.valueOf(currentQuestionCount)); } });
        btnMinus.setOnClickListener(v -> { if (currentQuestionCount > 1) { currentQuestionCount--; tvQuestionCount.setText(String.valueOf(currentQuestionCount)); } });

        if (btnSelectAll != null) {
            btnSelectAll.setOnClickListener(v -> {
                if (surahAdapter.isAllSelected()) { surahAdapter.deselectAll(); btnSelectAll.setText("Pilih Semua"); }
                else { surahAdapter.selectAll(); btnSelectAll.setText("Batal Semua"); }
            });
        }

        btnStartMuraajah.setOnClickListener(v -> {
            List<Integer> selectedIds = surahAdapter.getSelectedSurahIds();
            if (selectedIds.isEmpty()) {
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Perhatian")
                        .setContentText("Pilih minimal satu surah untuk memulai")
                        .setConfirmText("OK")
                        .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                        .show();
                return;
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.putIntegerArrayListExtra("SELECTED_SURAH_IDS", new ArrayList<>(selectedIds));
            intent.putExtra("LIMIT", currentQuestionCount);
            intent.putExtra("SURAH_NAME", surahAdapter.getSelectedSurahName());
            // Kirim JUZ_ID agar progres tersimpan spesifik per Juz
            intent.putExtra("JUZ_ID", tabLayoutJuz.getSelectedTabPosition() + 1);
            startActivity(intent);
        });
    }

    private void loadSurahByJuz(int juz) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_GET_SURAHS + "?juz=" + juz + "&user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000); // Diperpanjang ke 15 detik
                conn.setReadTimeout(15000);
                
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                String response = sb.toString();
                
                JSONArray arr;
                if (response.trim().startsWith("{")) {
                    JSONObject obj = new JSONObject(response);
                    arr = obj.optJSONArray("data");
                } else {
                    arr = new JSONArray(response);
                }

                List<Surah> fetched = new ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        fetched.add(new Surah(
                            obj.optInt("id", 0),
                            obj.optInt("surah_number", 0),
                            obj.optString("name", "Surah"),
                            obj.optString("name_arabic", ""),
                            obj.optInt("total_verses", 0),
                            obj.optString("status", "unlocked"),
                            obj.optInt("progress", 0)
                        ));
                    }
                }
                runOnUiThread(() -> {
                    surahAdapter.updateData(fetched);
                    btnStartMuraajah.setVisibility(fetched.isEmpty() ? View.GONE : View.VISIBLE);
                });
            } catch (Exception e) {
                Log.e("PilihSurah", "Error: " + e.getMessage());
                runOnUiThread(() -> {
                    new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Gagal memuat daftar surah. Periksa koneksi server.")
                        .setConfirmText("OK")
                        .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                        .show();
                });
            }
        });
    }
}
