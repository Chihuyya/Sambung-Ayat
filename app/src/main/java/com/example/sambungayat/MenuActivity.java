package com.example.sambungayat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class MenuActivity extends AppCompatActivity {

    private ListView lvSurah;
    private Button btnBukaLeaderboard;
    private ArrayList<String> listSurahDisplay = new ArrayList<>();
    private ArrayList<Integer> listSurahId = new ArrayList<>();
    private ArrayList<String> listSurahName = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private final String API_URL = "http://10.0.2.2/API_sambung_ayat/get_surah.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        lvSurah = findViewById(R.id.lvSurah);
        btnBukaLeaderboard = findViewById(R.id.btnBukaLeaderboard);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listSurahDisplay);
        lvSurah.setAdapter(adapter);

        // AKSI: Klik Tombol Leaderboard untuk pindah halaman
        btnBukaLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        // AKSI: Klik salah satu Surah untuk mulai bermain game
        lvSurah.setOnItemClickListener((parent, view, position, id) -> {
            int selectedId = listSurahId.get(position);
            String selectedName = listSurahName.get(position);

            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.putExtra("SURAH_ID", selectedId);
            intent.putExtra("SURAH_NAME", selectedName);
            startActivity(intent);
        });

        // Memuat data dari database lokal XAMPP
        loadSurahFromDatabase();
    }

    private void loadSurahFromDatabase() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(sb.toString());

                listSurahDisplay.clear();
                listSurahId.clear();
                listSurahName.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    int id = obj.getInt("id");
                    int surahNumber = obj.getInt("surah_number");
                    String name = obj.getString("name");
                    int totalVerses = obj.getInt("total_verses");

                    listSurahId.add(id);
                    listSurahName.add(name);
                    listSurahDisplay.add(surahNumber + ". " + name + " (" + totalVerses + " Ayat)");
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MenuActivity.this, "Gagal memuat daftar surah dari database XAMPP!", Toast.LENGTH_SHORT).show());
            }
        });
    }
}