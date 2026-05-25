package com.example.sambungayat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
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
    private List<Surah> listSurahMaster = new ArrayList<>();
    private EditText etSearch;
    private TabLayout tabLayoutCategory;
    private ImageView btnBack;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_surah);

        // Binding UI
        etSearch = findViewById(R.id.etSearch);
        tabLayoutCategory = findViewById(R.id.tabLayoutCategory);
        rvSurah = findViewById(R.id.rvSurah);
        btnBack = findViewById(R.id.btnBack);

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);

        rvSurah.setLayoutManager(new LinearLayoutManager(this));
        surahAdapter = new SurahAdapter(listSurahMaster);
        rvSurah.setAdapter(surahAdapter);

        // Setup Custom Navbar
        NavbarUtil.setupNavbar(this, R.id.navPlay);

        loadSurahFromDatabase();

        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                surahAdapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        tabLayoutCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Filter logic can be implemented here
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadSurahFromDatabase() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Tambahkan timeout untuk mencegah hang
                URL url = new URL(Config.URL_GET_SURAHS + "?user_id=" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    JSONArray jsonArray = new JSONArray(sb.toString());
                    listSurahMaster.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        listSurahMaster.add(new Surah(
                                obj.getInt("id"),
                                obj.getInt("surah_number"),
                                obj.getString("name"),
                                obj.getInt("total_verses"),
                                obj.optString("status", "unlocked")
                        ));
                    }

                    runOnUiThread(() -> surahAdapter.notifyDataSetChanged());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Server error: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Koneksi terputus: Cek URL di Config.java", Toast.LENGTH_LONG).show());
            }
        });
    }
}